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

package uk.gov.hmrc.pla.stub.model.hip

import play.api.libs.json.{Format, Json}

case class HipAmendProtectionResponse(
    updatedLifetimeAllowanceProtectionRecord: UpdatedLifetimeAllowanceProtectionRecord
)

object HipAmendProtectionResponse {
  implicit val format: Format[HipAmendProtectionResponse] = Json.format[HipAmendProtectionResponse]

  def from(
      hipProtection: HipProtection,
      status: AmendProtectionResponseStatus,
      notificationIdentifier: Option[Int]
  ): HipAmendProtectionResponse = HipAmendProtectionResponse(
    UpdatedLifetimeAllowanceProtectionRecord(
      identifier = hipProtection.id,
      sequenceNumber = hipProtection.sequence,
      `type` = AmendProtectionLifetimeAllowanceType.from(hipProtection.`type`),
      certificateDate = hipProtection.certificateDate,
      certificateTime = hipProtection.certificateTime.map(_.replaceAll(":", "")),
      status = status,
      protectionReference = hipProtection.protectionReference,
      relevantAmount = hipProtection.relevantAmount,
      preADayPensionInPaymentAmount = hipProtection.preADayPensionInPaymentAmount,
      postADayBenefitCrystallisationEventAmount = hipProtection.postADayBenefitCrystallisationEventAmount,
      uncrystallisedRightsAmount = hipProtection.uncrystallisedRightsAmount,
      nonUKRightsAmount = hipProtection.nonUKRightsAmount,
      pensionDebitAmount = hipProtection.pensionDebitAmount,
      pensionDebitEnteredAmount = hipProtection.pensionDebitEnteredAmount,
      notificationIdentifier = notificationIdentifier,
      protectedAmount = hipProtection.protectedAmount,
      pensionDebitStartDate = hipProtection.pensionDebitStartDate,
      pensionDebitTotalAmount = hipProtection.pensionDebitTotalAmount
    )
  )

}
