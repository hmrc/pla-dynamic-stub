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
import uk.gov.hmrc.pla.stub.model.Protections

case class ReadProtectionsResponse(
    pensionSchemeAdministratorCheckReference: String,
    protectionRecordsList: Seq[ProtectionRecordsList]
)

object ReadProtectionsResponse {

  def apply(protectionsObj: Protections): ReadProtectionsResponse = {

    val psaCheckReference: String = protectionsObj.pensionSchemeAdministratorCheckReference
      .getOrElse(throw new IllegalArgumentException("PSA Check Reference required for Protection model transformation"))

    val protectionRecordsList: Seq[ProtectionRecordsList] =
      protectionsObj.protections.map(x => ProtectionRecordsList(ProtectionRecord.apply(x), None))

    ReadProtectionsResponse(
      psaCheckReference,
      protectionRecordsList
    )
  }

  implicit val format: Format[ReadProtectionsResponse] = Json.format[ReadProtectionsResponse]
}

case class ProtectionRecordsList(
    protectionRecord: ProtectionRecord,
    historicaldetailsList: Option[Seq[ProtectionRecord]]
)

object ProtectionRecordsList {
  implicit val format: Format[ProtectionRecordsList] = Json.format[ProtectionRecordsList]
}

case class ProtectionRecord(
    identifier: Long,
    sequenceNumber: Int,
    `type`: ProtectionType,
    certificateDate: String,
    certificateTime: String,
    status: ProtectionStatus,
    protectionReference: Option[String],
    relevantAmount: Option[Int],
    preADayPensionInPaymentAmount: Option[Int],
    postADayBenefitCrystallisationEventAmount: Option[Int],
    uncrystallisedRightsAmount: Option[Int],
    nonUKRightsAmount: Option[Int],
    pensionDebitAmount: Option[Int],
    pensionDebitEnteredAmount: Option[Int],
    protectedAmount: Option[Int],
    pensionDebitStartDate: Option[String],
    pensionDebitTotalAmount: Option[Int],
    lumpSumAmount: Option[Int],
    lumpSumPercentage: Option[Int],
    enhancementFactor: Option[Double]
)

object ProtectionRecord {

  def apply(protection: Protection): ProtectionRecord =
    ProtectionRecord(
      identifier = protection.id,
      sequenceNumber = protection.sequence,
      `type` = protection.`type`,
      certificateDate = "2000-01-01",
      certificateTime = "000000",
      status = protection.status,
      protectionReference = protection.protectionReference,
      relevantAmount = Some(protection.relevantAmount),
      preADayPensionInPaymentAmount = Some(protection.preADayPensionInPaymentAmount),
      postADayBenefitCrystallisationEventAmount = Some(protection.postADayBenefitCrystallisationEventAmount),
      uncrystallisedRightsAmount = Some(protection.uncrystallisedRightsAmount),
      nonUKRightsAmount = Some(protection.nonUKRightsAmount),
      pensionDebitAmount = None,
      pensionDebitEnteredAmount = None,
      protectedAmount = protection.protectedAmount,
      pensionDebitStartDate = None,
      pensionDebitTotalAmount = protection.pensionDebitTotalAmount,
      lumpSumAmount = None,
      lumpSumPercentage = None,
      enhancementFactor = None
    )

  implicit val format: Format[ProtectionRecord] = Json.format[ProtectionRecord]
}
