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

case class AmendProtectionResponse(
    updatedLifetimeAllowanceProtectionRecord: UpdatedLifetimeAllowanceProtectionRecord
)

object AmendProtectionResponse {
  implicit val format: Format[AmendProtectionResponse] = Json.format[AmendProtectionResponse]

  def from(
      protection: Protection,
      status: AmendProtectionResponseStatus,
      notificationIdentifier: Option[Int]
  ): AmendProtectionResponse = AmendProtectionResponse(
    UpdatedLifetimeAllowanceProtectionRecord(
      identifier = protection.id,
      sequenceNumber = protection.sequence,
      `type` = AmendProtectionLifetimeAllowanceType.from(protection.`type`),
      certificateDate = protection.certificateDate,
      certificateTime = protection.certificateTime.map(_.replace(":", "")),
      status = status,
      protectionReference = protection.protectionReference,
      relevantAmount = protection.relevantAmount,
      preADayPensionInPaymentAmount = protection.preADayPensionInPaymentAmount,
      postADayBenefitCrystallisationEventAmount = protection.postADayBenefitCrystallisationEventAmount,
      uncrystallisedRightsAmount = protection.uncrystallisedRightsAmount,
      nonUKRightsAmount = protection.nonUKRightsAmount,
      pensionDebitAmount = protection.pensionDebitAmount,
      pensionDebitEnteredAmount = protection.pensionDebitEnteredAmount,
      notificationIdentifier = notificationIdentifier,
      protectedAmount = protection.protectedAmount,
      pensionDebitStartDate = protection.pensionDebitStartDate,
      pensionDebitTotalAmount = protection.pensionDebitTotalAmount
    )
  )

}
