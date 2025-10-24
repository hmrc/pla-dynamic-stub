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

package uk.gov.hmrc.pla.stub.validation

import AmendRequestValidationError._
import uk.gov.hmrc.pla.stub.model.hip.{HipProtection, LifetimeAllowanceProtectionRecord}

import java.time.{LocalDate, LocalTime}
import java.time.format.{DateTimeFormatter, DateTimeParseException}

object AmendRequestValidation {

  def validateRequest(
      lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord
  ): Either[AmendRequestValidationError, LifetimeAllowanceProtectionRecord] = {
    val calculatedRelevantAmount = calculateRelevantAmount(lifetimeAllowanceProtectionRecord)

    if (calculatedRelevantAmount != lifetimeAllowanceProtectionRecord.relevantAmount) {
      Left(IncorrectRelevantAmount(lifetimeAllowanceProtectionRecord.relevantAmount, calculatedRelevantAmount))
    } else if (
      lifetimeAllowanceProtectionRecord.pensionDebitStartDate.isDefined != lifetimeAllowanceProtectionRecord.pensionDebitEnteredAmount.isDefined
    ) {
      Left(IncompletePensionDebit)
    } else if (lifetimeAllowanceProtectionRecord.relevantAmount < 0) {
      Left(RelevantAmountNotPositive)
    } else if (lifetimeAllowanceProtectionRecord.nonUKRightsAmount < 0) {
      Left(NonUKRightsAmountNotPositive)
    } else if (lifetimeAllowanceProtectionRecord.postADayBenefitCrystallisationEventAmount < 0) {
      Left(PostADayBenefitCrystallisationEventAmountNotPositive)
    } else if (lifetimeAllowanceProtectionRecord.preADayPensionInPaymentAmount < 0) {
      Left(PreADayPensionInPaymentAmountNotPostive)
    } else if (lifetimeAllowanceProtectionRecord.uncrystallisedRightsAmount < 0) {
      Left(UncrystallisedRightsAmountNotPositive)
    } else if (lifetimeAllowanceProtectionRecord.pensionDebitEnteredAmount.exists(_ < 0)) {
      Left(PensionDebitEnteredAmountNotPositive)
    } else if (lifetimeAllowanceProtectionRecord.certificateDate.map(parseDate).contains(None)) {
      Left(CertificateDateInvalid)
    } else if (!lifetimeAllowanceProtectionRecord.certificateTime.forall(isTimeValid)) {
      Left(CertificateTimeInvalid)
    } else if (lifetimeAllowanceProtectionRecord.pensionDebitStartDate.map(parseDate).contains(None)) {
      Left(PensionDebitStartDateInvalid)
    } else {
      Right(lifetimeAllowanceProtectionRecord)
    }
  }

  def validateRequestAgainstTarget(
      lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord,
      amendmentTarget: HipProtection,
      sequence: Int
  ): Either[AmendRequestValidationError, HipProtection] =
    if (amendmentTarget.`type` != lifetimeAllowanceProtectionRecord.`type`.toProtectionType) {
      Left(ProtectionTypeDoesNotMatch)
    } else if (amendmentTarget.sequence != sequence) {
      Left(ProtectionSequenceDoesNotMatch)
    } else if (amendmentTarget.pensionDebitTotalAmount != lifetimeAllowanceProtectionRecord.pensionDebitTotalAmount) {
      Left(PensionDebitTotalAmountDoesNotMatch)
    } else {
      Right(amendmentTarget)
    }

  private def calculateRelevantAmount(lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord): Int =
    lifetimeAllowanceProtectionRecord.nonUKRightsAmount +
      lifetimeAllowanceProtectionRecord.postADayBenefitCrystallisationEventAmount +
      lifetimeAllowanceProtectionRecord.preADayPensionInPaymentAmount +
      lifetimeAllowanceProtectionRecord.uncrystallisedRightsAmount -
      lifetimeAllowanceProtectionRecord.pensionDebitTotalAmount.getOrElse(0)

  private val CertificateTimeLength: Int = 6
  private val timeFormat                 = DateTimeFormatter.ofPattern("HHmmss")

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

  private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

  def parseDate(dateString: String): Option[LocalDate] =
    try
      Some(LocalDate.parse(dateString, dateFormat))
    catch {
      case _: DateTimeParseException => None
    }

}
