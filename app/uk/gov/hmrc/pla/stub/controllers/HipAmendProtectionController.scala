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
import uk.gov.hmrc.pla.stub.model.Error
import uk.gov.hmrc.pla.stub.rules.HipAmendmentRules.{
  IndividualProtection2014AmendmentRules,
  IndividualProtection2016AmendmentRules
}
import uk.gov.hmrc.pla.stub.rules._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.smartstub.{Generator => _}

import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.temporal.ChronoUnit
import java.time.{Clock, LocalDate, LocalTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HipAmendProtectionController @Inject() (
    val mcc: ControllerComponents,
    val protectionService: PLAProtectionService,
    playBodyParsers: PlayBodyParsers
)(implicit val ec: ExecutionContext, clock: Clock)
    extends BackendController(mcc)
    with Logging {

  def amendProtection(nino: String, protectionId: Long, sequence: Int): Action[JsValue] =
    Action.async(playBodyParsers.json) { implicit request =>
      val ninoWithoutSuffix = nino.dropRight(1) // Remove when NPS code is removed and stub data is updated to use suffixes

      request.body.validate[HipAmendProtectionRequest].map(_.lifetimeAllowanceProtectionRecord).asEither match {
        case Left(errors) =>
          Future.successful(BadRequest(Json.toJson(Error(message = "failed validation with errors: " + errors))))
        case Right(lifetimeAllowanceProtectionRecord) =>
          amendLifetimeAllowanceProtectionRecord(
            lifetimeAllowanceProtectionRecord,
            ninoWithoutSuffix,
            protectionId,
            sequence
          )
      }
    }

  private def amendLifetimeAllowanceProtectionRecord(
      lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord,
      nino: String,
      protectionId: Long,
      sequence: Int
  ): Future[Result] = {
    val pensionDebitTotalAmount = lifetimeAllowanceProtectionRecord.pensionDebitTotalAmount.getOrElse(0)

    val calculatedRelevantAmount =
      lifetimeAllowanceProtectionRecord.nonUKRightsAmount +
        lifetimeAllowanceProtectionRecord.postADayBenefitCrystallisationEventAmount +
        lifetimeAllowanceProtectionRecord.preADayPensionInPaymentAmount +
        lifetimeAllowanceProtectionRecord.uncrystallisedRightsAmount -
        pensionDebitTotalAmount

    if (calculatedRelevantAmount != lifetimeAllowanceProtectionRecord.relevantAmount) {
      error(
        UnprocessableEntity,
        s"The specified Relevant Amount ${lifetimeAllowanceProtectionRecord.relevantAmount} is not the sum of the specified breakdown amounts $calculatedRelevantAmount (non UK Rights + Post A Day BCE + Pre A Day Pensions In Payment + Uncrystallised Rights - pensionDebitTotalAmount)"
      )
    } else if (
      lifetimeAllowanceProtectionRecord.pensionDebitStartDate.isDefined != lifetimeAllowanceProtectionRecord.pensionDebitEnteredAmount.isDefined
    ) {
      error(
        UnprocessableEntity,
        "incomplete pension debits information - require either both, or neither of pension debit start date and pension debit entered amount"
      )
    } else if (lifetimeAllowanceProtectionRecord.relevantAmount < 0) {
      error(BadRequest, "relevant amount must be positive")
    } else if (lifetimeAllowanceProtectionRecord.nonUKRightsAmount < 0) {
      error(BadRequest, "non UK rights amount must be positive")
    } else if (lifetimeAllowanceProtectionRecord.postADayBenefitCrystallisationEventAmount < 0) {
      error(BadRequest, "post A day benefit crystallisation event amount must be positive")
    } else if (lifetimeAllowanceProtectionRecord.preADayPensionInPaymentAmount < 0) {
      error(BadRequest, "pre A day pension in payment amount must be positive")
    } else if (lifetimeAllowanceProtectionRecord.uncrystallisedRightsAmount < 0) {
      error(BadRequest, "uncrystallised rights amount must be positive")
    } else if (lifetimeAllowanceProtectionRecord.pensionDebitEnteredAmount.exists(_ < 0)) {
      error(BadRequest, "pension debit entered amount must be positive")
    } else if (lifetimeAllowanceProtectionRecord.certificateDate.map(parseDate).contains(None)) {
      error(BadRequest, "invalid certificate date")
    } else if (!lifetimeAllowanceProtectionRecord.certificateTime.forall(isTimeValid)) {
      error(BadRequest, "invalid certificate time")
    } else if (lifetimeAllowanceProtectionRecord.pensionDebitStartDate.map(parseDate).contains(None)) {
      error(BadRequest, "invalid pension debit start date")
    } else {
      protectionService.findHipProtectionByNinoAndId(nino, protectionId).flatMap[Result] {
        case None =>
          error(NotFound, "protection to amend not found")
        case Some(amendmentTarget)
            if amendmentTarget.`type` != lifetimeAllowanceProtectionRecord.`type`.toProtectionType =>
          error(BadRequest, "specified protection type does not match that of the protection to be amended")
        case Some(amendmentTarget) if amendmentTarget.sequence != sequence =>
          error(BadRequest, "specified protection sequence does not match that of the protection to be amended")
        case Some(amendmentTarget) if pensionDebitTotalAmount != amendmentTarget.pensionDebitTotalAmount.getOrElse(0) =>
          error(BadRequest, "specified pension debit total amount does not match that of protection to be amended")
        case Some(amendmentTarget) =>
          protectionService
            .findAllHipProtectionsByNino(nino)
            .flatMap { hipProtections =>
              val updatedRecord =
                updatedLifetimeAllowanceProtectionRecord(lifetimeAllowanceProtectionRecord)

              val rules: HipAmendmentRules = getHipAmendmentRules(lifetimeAllowanceProtectionRecord.`type`)

              val hipNotification =
                rules.calculateNotificationId(updatedRecord.relevantAmount, hipProtections)

              val amendedProtection = createAmendedHipProtection(
                nino,
                amendmentTarget,
                updatedRecord,
                hipNotification
              )

              val okResponse =
                HipAmendProtectionResponse.from(amendedProtection, hipNotification.status, Some(hipNotification.id))
              val okResponseBody = Json.toJson(okResponse)
              val result         = Ok(okResponseBody)

              val updateRepoFut = for {
                _             <- protectionService.insertOrUpdateHipProtection(amendedProtection)
                updateRepoFut <- openDormantFixedProtection2016(hipNotification, nino)
              } yield updateRepoFut

              updateRepoFut.map(_ => result).recover { case x => InternalServerError(x.toString) }
            }
      }
    }
  }

  private def error(statusConstructor: Status, message: String): Future[Result] =
    Future(statusConstructor(Json.toJson(Error(message))))

  private[controllers] def updatedLifetimeAllowanceProtectionRecord(
      lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord
  ): LifetimeAllowanceProtectionRecord = {
    val adjustedEnteredAmount = calculateAdjustedEnteredAmount(lifetimeAllowanceProtectionRecord)

    val calculatedRelevantAmountMinusPSO = lifetimeAllowanceProtectionRecord.relevantAmount - adjustedEnteredAmount
    val adjustedPensionDebitTotalAmount =
      lifetimeAllowanceProtectionRecord.pensionDebitTotalAmount.getOrElse(0) + adjustedEnteredAmount

    val certificateDate = LocalDate.now(clock).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
    val certificateTime = LocalTime.now(clock).format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)

    val maxProtectedAmount = calculateMaxProtectedAmount(lifetimeAllowanceProtectionRecord.`type`)

    lifetimeAllowanceProtectionRecord.copy(
      pensionDebitTotalAmount = Some(adjustedPensionDebitTotalAmount).filter(_ != 0),
      relevantAmount = calculatedRelevantAmountMinusPSO,
      certificateDate = Some(certificateDate),
      certificateTime = Some(certificateTime),
      protectedAmount = Some(calculatedRelevantAmountMinusPSO.min(maxProtectedAmount))
    )
  }

  private def getHipAmendmentRules(
      protectionType: AmendProtectionLifetimeAllowanceType
  ): HipAmendmentRules =
    protectionType match {
      case AmendProtectionLifetimeAllowanceType.IndividualProtection2014 |
          AmendProtectionLifetimeAllowanceType.IndividualProtection2014LTA =>
        IndividualProtection2014AmendmentRules
      case AmendProtectionLifetimeAllowanceType.IndividualProtection2016 |
          AmendProtectionLifetimeAllowanceType.IndividualProtection2016LTA =>
        IndividualProtection2016AmendmentRules
    }

  private[controllers] def calculateMaxProtectedAmount(protectionType: AmendProtectionLifetimeAllowanceType): Int =
    protectionType match {
      case AmendProtectionLifetimeAllowanceType.IndividualProtection2014 |
          AmendProtectionLifetimeAllowanceType.IndividualProtection2014LTA =>
        1_500_000
      case AmendProtectionLifetimeAllowanceType.IndividualProtection2016 |
          AmendProtectionLifetimeAllowanceType.IndividualProtection2016LTA =>
        1_250_000
    }

  private[controllers] def opensDormantFixedProtection2016(hipNotification: HipNotification): Boolean = {
    import HipNotification._

    hipNotification match {
      case HipNotification7 | HipNotification14 => true
      case _                                    => false
    }
  }

  private def openDormantFixedProtection2016(
      hipNotification: HipNotification,
      nino: String
  ): Future[Unit] =
    if (opensDormantFixedProtection2016(hipNotification)) {
      protectionService.updateDormantProtectionStatusAsOpen(nino)
    } else
      Future.unit

  private def calculateAdjustedEnteredAmount(
      lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord
  ): Int =
    lifetimeAllowanceProtectionRecord.pensionDebitEnteredAmount
      .zip(lifetimeAllowanceProtectionRecord.pensionDebitStartDate)
      .flatMap { case (enteredAmount, startDateString) =>
        parseDate(startDateString).map(startDate => calculateAdjustedEnteredAmount(enteredAmount, startDate))
      }
      .getOrElse(0)

  private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE
  private val timeFormat = DateTimeFormatter.ofPattern("HHmmss")

  private def parseDate(dateString: String): Option[LocalDate] =
    try
      Some(LocalDate.parse(dateString, dateFormat))
    catch {
      case _: DateTimeParseException => None
    }

  private val CertificateTimeLength: Int = 6

  private def isTimeValid(timeString: String): Boolean = {
    def padCertificateTime(certificateTime: String): String = {
      val paddedChars = (CertificateTimeLength - certificateTime.length).max(0)

      val padding = "0".repeat(paddedChars)

      s"$padding$certificateTime"
    }

    try {
      LocalTime.parse(padCertificateTime(timeString), timeFormat)
      true
    } catch {
      case _: DateTimeParseException => false
    }
  }

  private val startOfTaxYear2016               = LocalDate.of(2016, 4, 6)
  private val enteredAmountDeductionPerTaxYear = 0.05
  private val maxElapsedFullTaxYears           = 20

  private[controllers] def calculateAdjustedEnteredAmount(enteredAmount: Int, startDate: LocalDate): Int = {
    val fullTaxYearsElapsedSinceTaxYear2016 =
      ChronoUnit.YEARS.between(startOfTaxYear2016, startDate).max(0).min(maxElapsedFullTaxYears)

    val deductionPerFullTaxYear = enteredAmountDeductionPerTaxYear * enteredAmount

    val totalDeduction = deductionPerFullTaxYear * fullTaxYearsElapsedSinceTaxYear2016

    val adjustedEnteredAmount = enteredAmount - totalDeduction

    adjustedEnteredAmount.toInt
  }

  private def createAmendedHipProtection(
      nino: String,
      current: HipProtection,
      lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord,
      hipNotification: HipNotification
  ): HipProtection =
    HipProtection(
      nino = nino,
      sequence = current.sequence + 1,
      id = current.id,
      `type` = current.`type`,
      protectionReference = current.protectionReference,
      status = hipNotification.status.toProtectionStatus,
      certificateDate = lifetimeAllowanceProtectionRecord.certificateDate,
      certificateTime = lifetimeAllowanceProtectionRecord.certificateTime,
      relevantAmount = lifetimeAllowanceProtectionRecord.relevantAmount,
      protectedAmount = lifetimeAllowanceProtectionRecord.protectedAmount,
      preADayPensionInPaymentAmount = lifetimeAllowanceProtectionRecord.preADayPensionInPaymentAmount,
      postADayBenefitCrystallisationEventAmount =
        lifetimeAllowanceProtectionRecord.postADayBenefitCrystallisationEventAmount,
      uncrystallisedRightsAmount = lifetimeAllowanceProtectionRecord.uncrystallisedRightsAmount,
      nonUKRightsAmount = lifetimeAllowanceProtectionRecord.nonUKRightsAmount,
      pensionDebitTotalAmount = lifetimeAllowanceProtectionRecord.pensionDebitTotalAmount
    )

}
