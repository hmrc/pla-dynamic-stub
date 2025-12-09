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

import uk.gov.hmrc.pla.stub.model.hip.{LifetimeAllowanceProtectionRecord, Protection}
import uk.gov.hmrc.pla.stub.validation.AmendRequestValidationError._

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
      Left(PreADayPensionInPaymentAmountNotPositive)
    } else if (lifetimeAllowanceProtectionRecord.uncrystallisedRightsAmount < 0) {
      Left(UncrystallisedRightsAmountNotPositive)
    } else if (lifetimeAllowanceProtectionRecord.pensionDebitEnteredAmount.exists(_ < 0)) {
      Left(PensionDebitEnteredAmountNotPositive)
    } else {
      Right(lifetimeAllowanceProtectionRecord)
    }
  }

  def validateRequestAgainstTarget(
      lifetimeAllowanceProtectionRecord: LifetimeAllowanceProtectionRecord,
      amendmentTarget: Protection,
      sequence: Int
  ): Either[AmendRequestValidationError, Protection] =
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

}
