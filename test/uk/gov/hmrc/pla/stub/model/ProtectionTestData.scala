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

package uk.gov.hmrc.pla.stub.model

import uk.gov.hmrc.pla.stub.model.hip.ProtectionType.{
  EnhancedProtection,
  FixedProtection,
  FixedProtection2014,
  FixedProtection2016,
  IndividualProtection2014,
  IndividualProtection2016,
  PrimaryProtection
}
import uk.gov.hmrc.pla.stub.model.hip.{Protection, ProtectionStatus}

import java.time.{LocalDate, LocalTime}

object ProtectionTestData {

  import Generator._

  val currentDate: String = LocalDate.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
  val currentTime: String = LocalTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)

  val openFP2016 = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Open,
    `type` = FixedProtection2016,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = refGenFP16
  )

  val openFP2016WithPensionDebits = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Open,
    `type` = FixedProtection2016,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = refGenFP16
  )

  val openIP2016 = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Open,
    `type` = IndividualProtection2016,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = refGenIP16
  )

  val openFP2014 = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Open,
    `type` = FixedProtection2014,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = refGenFP16
  )

  val openIP2014 = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Open,
    `type` = IndividualProtection2014,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = refGenIP14
  )

  val openPrimary = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Open,
    `type` = PrimaryProtection,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = randomOlderProtectionReference
  )

  val openFixed = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Open,
    `type` = FixedProtection,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = randomOlderProtectionReference
  )

  val openEnhanced = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Open,
    `type` = EnhancedProtection,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = randomOlderProtectionReference
  )

  val dormantPrimary = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Dormant,
    `type` = PrimaryProtection,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = randomOlderProtectionReference
  )

  val dormantEnhanced = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Dormant,
    `type` = EnhancedProtection,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = randomOlderProtectionReference
  )

  val withdrawnPrimary = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Withdrawn,
    `type` = PrimaryProtection,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = randomOlderProtectionReference
  )

  val rejected = Protection(
    nino = randomNino,
    id = randomProtectionID,
    sequence = 1,
    status = ProtectionStatus.Rejected,
    `type` = IndividualProtection2016,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = None
  )

}
