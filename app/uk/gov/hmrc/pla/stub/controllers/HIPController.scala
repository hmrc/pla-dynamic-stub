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

package uk.gov.hmrc.pla.stub.controllers

import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.pla.stub.model.hip._
import uk.gov.hmrc.pla.stub.model.{Error, Protections}
import uk.gov.hmrc.pla.stub.rules.HipAmendmentRules.{
  IndividualProtection2014AmendmentRules,
  IndividualProtection2016AmendmentRules
}
import uk.gov.hmrc.pla.stub.rules._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.smartstub.{Generator => _}

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HIPController @Inject() (
    val mcc: ControllerComponents,
    val protectionService: PLAProtectionService,
    playBodyParsers: PlayBodyParsers
)(implicit val ec: ExecutionContext, clock: Clock)
    extends BackendController(mcc)
    with Logging {

  def readProtections(nino: String): Action[AnyContent] = Action.async { _ =>
    protectionService.retrieveHIPProtections(nino).map {
      case Some(protections) =>
        Ok(Json.toJson(protections))
      case None =>
        logger.info("No protections set for given Nino, returning empty protections list")
        Ok(Json.toJson(HIPProtectionsModel(Protections(nino, Some("stubPSACheckRef"), List.empty))))
    }
  }

  def amendProtection(nino: String, protectionId: Int, sequence: Int): Action[JsValue] =
    Action.async(playBodyParsers.json) { implicit request =>
      val lifetimeAllowanceProtectionRecordResult =
        request.body.validate[HipAmendProtectionRequest].map(_.lifetimeAllowanceProtectionRecord)

      lifetimeAllowanceProtectionRecordResult.fold(
        errors =>
          Future.successful(BadRequest(Json.toJson(Error(message = "failed validation with errors: " + errors)))),
        lifetimeAllowanceProtectionRecord => {
          // first cross-check relevant amount against total of the breakdown fields, reject if discrepancy found
          val calculatedRelevantAmount =
            lifetimeAllowanceProtectionRecord.nonUKRightsAmount +
              lifetimeAllowanceProtectionRecord.postADayBenefitCrystallisationEventAmount +
              lifetimeAllowanceProtectionRecord.preADayPensionInPaymentAmount +
              lifetimeAllowanceProtectionRecord.uncrystallisedRightsAmount -
              lifetimeAllowanceProtectionRecord.pensionDebitTotalAmount.getOrElse(0)

          if (calculatedRelevantAmount != lifetimeAllowanceProtectionRecord.relevantAmount) {
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
            calculatedRelevantAmount - lifetimeAllowanceProtectionRecord.pensionDebitEnteredAmount.getOrElse(0)

          val amendmentTargetOption = protectionService.findHipProtectionByNinoAndId(nino, protectionId)
          amendmentTargetOption.flatMap[Result] {
            case None =>
              Future(NotFound(Json.toJson(Error(message = "protection to amend not found"))))
            case Some(amendmentTarget)
                if amendmentTarget.`type` != lifetimeAllowanceProtectionRecord.`type`.toProtectionType =>
              val error = Error("specified protection type does not match that of the protection to be amended")
              Future(BadRequest(Json.toJson(error)))
            case Some(amendmentTarget) if amendmentTarget.sequence != sequence =>
              val error = Error("specified protection sequence does not match that of the protection to be amended")
              Future(BadRequest(Json.toJson(error)))
            case Some(amendmentTarget) =>
              val existingHipProtections = protectionService.findAllProtectionsByNino(nino).map {
                case Some(protections) => protections.flatMap(HipProtection.fromProtection)
                case _                 => List()
              }
              val rules: Option[HipAmendmentRules] = lifetimeAllowanceProtectionRecord.`type` match {
                case AmendProtectionLifetimeAllowanceType.IndividualProtection2014 =>
                  Some(IndividualProtection2014AmendmentRules)
                case AmendProtectionLifetimeAllowanceType.IndividualProtection2016 =>
                  Some(IndividualProtection2016AmendmentRules)
                case _ => None
              }
              rules
                .map { rules: HipAmendmentRules =>
                  existingHipProtections.flatMap { hipProtections =>
                    val hipNotification =
                      rules.calculateNotificationId(calculatedRelevantAmountMinusPSO, hipProtections)
                    processAmendment(nino, amendmentTarget, lifetimeAllowanceProtectionRecord, hipNotification)
                  }
                }
                .getOrElse {
                  val error = Error(
                    "invalid protection type specified, expected IndividualProtection2014 or IndividualProtection2016"
                  )
                  Future.successful(BadRequest(Json.toJson(error)))
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
    * @param lifetimeAllowanceProtectionRecord
    *   Details of the requested amendment, as parsed from the request body.
    * @param notificationId
    *   The notification id resulting from the business rule checks
    * @return
    *   Updated protection result
    */
  private def processAmendment(
      nino: String,
      current: HipProtection,
      lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord,
      hipNotification: HipNotification
  ): Future[Result] =
    AmendProtectionResponseStatus
      .fromProtectionStatus(hipNotification.status)
      .map(responseStatus =>
        processAmendment(nino, current, lifetimeAllowanceProtectionRecord, hipNotification, responseStatus)
      )
      .getOrElse {
        val error =
          Error(
            s"Rules yielded a status of ${hipNotification.status} for notification ID ${hipNotification.id}, but this is not a valid response status for the HIP API"
          )
        Future.successful(BadRequest(Json.toJson(error)))
      }

  private def processAmendment(
      nino: String,
      current: HipProtection,
      lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord,
      hipNotification: HipNotification,
      responseStatus: AmendProtectionResponseStatus
  ): Future[Result] = {

    val amendedProtection = hipNotification.status match {
      case ProtectionStatus.Withdrawn =>
        protectionService.updateDormantProtectionStatusAsOpen(nino)
        current.copy(
          sequence = current.sequence + 1,
          status = ProtectionStatus.Withdrawn
        )

      case _ =>
        val currDate = LocalDateTime.now(clock).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val currTime = LocalDateTime.now(clock).format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)

        val amendmentPsoAmount = lifetimeAllowanceProtectionRecord.pensionDebitEnteredAmount.getOrElse(0)
        val updatedPensionDebitTotalAmount =
          lifetimeAllowanceProtectionRecord.pensionDebitTotalAmount.getOrElse(0) + amendmentPsoAmount
        val relevantAmountMinusPSO = lifetimeAllowanceProtectionRecord.relevantAmount - amendmentPsoAmount

        val maxProtectedAmount = lifetimeAllowanceProtectionRecord.`type` match {
          case AmendProtectionLifetimeAllowanceType.IndividualProtection2014 => 1_500_000
          case AmendProtectionLifetimeAllowanceType.IndividualProtection2016 => 1_250_000
          case _                                                             => 0
        }

        HipProtection(
          nino = nino,
          sequence = current.sequence + 1,
          id = current.id,
          `type` = current.`type`,
          protectionReference = current.protectionReference,
          status = hipNotification.status,
          certificateDate = Some(currDate),
          certificateTime = Some(currTime),
          relevantAmount = relevantAmountMinusPSO,
          protectedAmount = Some(relevantAmountMinusPSO.min(maxProtectedAmount)),
          preADayPensionInPaymentAmount = lifetimeAllowanceProtectionRecord.preADayPensionInPaymentAmount,
          postADayBenefitCrystallisationEventAmount =
            lifetimeAllowanceProtectionRecord.postADayBenefitCrystallisationEventAmount,
          uncrystallisedRightsAmount = lifetimeAllowanceProtectionRecord.uncrystallisedRightsAmount,
          nonUKRightsAmount = lifetimeAllowanceProtectionRecord.nonUKRightsAmount,
          pensionDebitTotalAmount = Some(updatedPensionDebitTotalAmount)
        )
    }

    val okResponse     = HipAmendProtectionResponse.from(amendedProtection, responseStatus, Some(hipNotification.id))
    val okResponseBody = Json.toJson(okResponse)
    val result         = Ok(okResponseBody)

    val doAmendProtectionFut = protectionService.insertOrUpdateHipProtection(amendedProtection)

    val updateRepoFut = doAmendProtectionFut

    updateRepoFut.map(_ => result).recover { case x => InternalServerError(x.toString) }
  }

}
