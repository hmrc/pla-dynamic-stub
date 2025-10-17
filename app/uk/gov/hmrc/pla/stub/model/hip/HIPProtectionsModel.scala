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
import uk.gov.hmrc.pla.stub.model.{Protection, Protections}

case class HIPProtectionsModel(
    pensionSchemeAdministratorCheckReference: String,
    protectionRecordsList: Seq[ProtectionRecordsList]
)

object HIPProtectionsModel {

  def apply(protectionsObj: Protections): HIPProtectionsModel = {

    val psaCheckReference: String = protectionsObj.pensionSchemeAdministratorCheckReference
      .getOrElse(throw new IllegalArgumentException("PSA Check Reference required for HIP model transformation"))

    val protectionRecordsList: Seq[ProtectionRecordsList] =
      protectionsObj.protections.map(x => ProtectionRecordsList(ProtectionRecord(x), None))

    HIPProtectionsModel(
      psaCheckReference,
      protectionRecordsList
    )
  }

  implicit val format: Format[HIPProtectionsModel] = Json.format[HIPProtectionsModel]
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
      protection.id,
      protection.version,
      toRecordType(protection),
      protection.certificateDate.getOrElse("2000-01-01"),              // TODO: defaults for date and time?
      protection.certificateTime.getOrElse("000000").replace(":", ""), // TODO: defaults for date and time?
      toRecordStatus(protection),
      protection.protectionReference,
      protection.relevantAmount.map(_.toInt),
      protection.preADayPensionInPayment.map(_.toInt),
      protection.postADayBCE.map(_.toInt),
      protection.uncrystallisedRights.map(_.toInt),
      protection.nonUKRights.map(_.toInt),
      None, // TODO: Check pensionDebitAmount
      protection.pensionDebiitEnteredAmount.map(_.toInt),
      protection.protectedAmount.map(_.toInt),
      protection.pensionDebitStartDate,
      protection.pensionDebitTotalAmount.map(_.toInt),
      None,
      None,
      None
    )

  private def toRecordType(protection: Protection): ProtectionType =
    ProtectionType.fromPlaId(protection.`type`).getOrElse(ProtectionType.IndividualProtection2014)

  private def toRecordStatus(protection: Protection): ProtectionStatus =
    ProtectionStatus.fromPlaId(protection.status).getOrElse(ProtectionStatus.Rejected)

  implicit val format: Format[ProtectionRecord] = Json.format[ProtectionRecord]
}
