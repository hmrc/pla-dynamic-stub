/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pla.stub.controllers.hip

import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, PlayBodyParsers, Result}
import uk.gov.hmrc.pla.stub.model.Error
import uk.gov.hmrc.pla.stub.model.hip.AmendProtectionRequestType._
import uk.gov.hmrc.pla.stub.model.hip._
import uk.gov.hmrc.pla.stub.rules._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.smartstub.{Generator => _}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HIPStubController @Inject() (
    val mcc: ControllerComponents,
    val protectionService: PLAProtectionService,
    playBodyParsers: PlayBodyParsers,
    implicit val ec: ExecutionContext
) extends BackendController(mcc)
    with Logging {

  def amendProtection(nino: String, protectionId: Int, sequence: Int): Action[JsValue] =
    Action.async(playBodyParsers.json) { implicit request =>
      val amendProtectionRequest = request.body.validate[AmendProtectionRequest]

      amendProtectionRequest.fold(
        errors =>
          Future.successful(BadRequest(Json.toJson(Error(message = "failed validation with errors: " + errors)))),
        amendProtectionRequest => {
          // first cross-check relevant amount against total of the breakdown fields, reject if discrepancy found
          val calculatedRelevantAmount =
            amendProtectionRequest.nonUKRightsAmount +
              amendProtectionRequest.postADayBenefitCrystallisationEventAmount +
              amendProtectionRequest.preADayPensionInPaymentAmount +
              amendProtectionRequest.uncrystallisedRightsAmount -
              amendProtectionRequest.pensionDebitTotalAmount.getOrElse(0)
          if (calculatedRelevantAmount != amendProtectionRequest.relevantAmount) {
            Future.successful(
              BadRequest(
                Json.toJson(
                  Error(message =
                    "The specified Relevant Amount is not the sum of the specified breakdown amounts " +
                      "(non UK Rights + Post A Day BCE + Pre A Day Pensions In Payment + Uncrystallised Rights)"
                  )
                )
              )
            )
          }

          val calculatedRelevantAmountMinusPSO =
            calculatedRelevantAmount - amendProtectionRequest.pensionDebitEnteredAmount.getOrElse(0)

          // val amendmentTargetFutureOption = protectionRepository.findLatestVersionOfProtectionByNinoAndId(nino, protectionId)
          val amendmentTargetOption = protectionService.findHipProtectionByNinoAndId(nino, protectionId)
          amendmentTargetOption.flatMap[Result] {
            case None =>
              Future(NotFound(Json.toJson(Error(message = "protection to amend not found"))))
            case Some(amendmentTarget)
                if amendmentTarget.`type` != amendProtectionRequest.`type`.toLifetimeAllowanceType =>
              val error = Error("specified protection type does not match that of the protection to be amended")
              Future(BadRequest(Json.toJson(error)))
            case Some(amendmentTarget) if amendmentTarget.sequence != sequence =>
              val error = Error("specified protection sequence does not match that of the protection to be amended")
              Future(BadRequest(Json.toJson(error)))
            case Some(amendmentTarget) =>
              val existingProtections = protectionService.findAllHipProtectionsByNino(nino).map {
                case Some(protections) => protections
                case _                 => List()
              }
              val rules: HipAmendmentRules = amendProtectionRequest.`type` match {
//                case IndividualProtection2014Lta => IndividualProtection2014LtaAmendmentRules
//                case IndividualProtection2016Lta => IndividualProtection2016LtaAmendmentRules
                case IndividualProtection2014 => IndividualProtection2014AmendmentRules
                case IndividualProtection2016 => IndividualProtection2016AmendmentRules
              }
              existingProtections.flatMap { protections =>
                val notificationId =
                  rules.calculateNotificationId(calculatedRelevantAmountMinusPSO, protections)
                processAmendment(nino, amendmentTarget, amendProtectionRequest, notificationId)
              }
          }
        }
      )
    }

  /** Process an amendment request for which we have determined the appropriate notification ID. The new version of the
    * protection will be created and stored in the repository. This may also result in a change to another protection,
    * if the amendment requires the current certificate to be withdrawn and therefore another protection to become
    * active.
    *
    * @param nino
    *   individuals NINO
    * @param current
    *   The current version of the protection to be amended
    * @param amendmentRequest
    *   Details of the requested amendment, as parsed from the request body.
    * @param notification
    *   The notification resulting from the business rule checks
    * @return
    *   Updated protection result
    */
  private def processAmendment(
      nino: String,
      current: HipProtection,
      amendmentRequest: AmendProtectionRequest,
      notification: Notification
  ): Future[Result] = {

    val amendedProtection = notification.status match {

      case AmendProtectionResponseStatus.Withdrawn =>
        protectionService.updateDormantProtectionStatusAsOpen(nino)
        current.copy(
          sequence = current.sequence + 1,
          status = ProtectionStatus.Withdrawn
        )

      case _ =>
        val currDate = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val currTime = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)

        val amendmentPsoAmount = amendmentRequest.pensionDebitEnteredAmount.getOrElse(0)
        val updatedPensionDebitTotalAmount =
          amendmentRequest.pensionDebitTotalAmount.getOrElse(0) + amendmentPsoAmount
        val relevantAmountMinusPSO = amendmentRequest.relevantAmount - amendmentPsoAmount

        val maxProtectedAmount = amendmentRequest.`type` match {
          case IndividualProtection2014 => 1_500_000
          case IndividualProtection2016 => 1_250_000
          case _                        => 0
        }

        HipProtection(
          nino = nino,
          sequence = current.sequence + 1,
          id = current.id,
          `type` = current.`type`,
          protectionReference = current.protectionReference,
          status = notification.status.toProtectionStatus,
          certificateDate = Some(currDate),
          certificateTime = Some(currTime),
          relevantAmount = relevantAmountMinusPSO,
          protectedAmount = Some(relevantAmountMinusPSO.min(maxProtectedAmount)),
          preADayPensionInPaymentAmount = amendmentRequest.preADayPensionInPaymentAmount,
          postADayBenefitCrystallisationEventAmount = amendmentRequest.postADayBenefitCrystallisationEventAmount,
          uncrystallisedRightsAmount = amendmentRequest.uncrystallisedRightsAmount,
          nonUKRightsAmount = amendmentRequest.nonUKRightsAmount,
          pensionDebitTotalAmount = Some(updatedPensionDebitTotalAmount)
        )
    }

    val okResponse = AmendProtectionResponse(
      identifier = amendedProtection.id,
      sequenceNumber = amendedProtection.sequence,
      `type` = amendedProtection.`type`,
      certificateDate = amendedProtection.certificateDate,
      certificateTime = amendedProtection.certificateTime,
      status = notification.status,
      protectionReference = amendedProtection.protectionReference,
      relevantAmount = amendedProtection.relevantAmount,
      preADayPensionInPaymentAmount = amendedProtection.preADayPensionInPaymentAmount,
      postADayBenefitCrystallisationEventAmount = amendedProtection.postADayBenefitCrystallisationEventAmount,
      uncrystallisedRightsAmount = amendedProtection.uncrystallisedRightsAmount,
      nonUKRightsAmount = amendedProtection.nonUKRightsAmount,
      pensionDebitAmount = amendedProtection.pensionDebitAmount,
      pensionDebitEnteredAmount = amendedProtection.pensionDebitEnteredAmount,
      notificationIdentifier = Some(notification),
      protectedAmount = amendedProtection.protectedAmount,
      pensionDebitStartDate = amendedProtection.pensionDebitStartDate,
      pensionDebitTotalAmount = amendedProtection.pensionDebitTotalAmount
    )
    val okResponseBody = Json.toJson(okResponse)
    val result         = Ok(okResponseBody)

    val doAmendProtectionFut = protectionService.insertOrUpdateHipProtection(amendedProtection)

    val updateRepoFut = for {
      done <- doAmendProtectionFut
    } yield done

    updateRepoFut.map(_ => result).recover { case x => InternalServerError(x.toString) }
  }

}
