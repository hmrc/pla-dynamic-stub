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

    val protectionRecordsList: Seq[ProtectionRecordsList] = protectionsObj.protections map { x =>
      ProtectionRecordsList(ProtectionRecord(x), None)
    }

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
                             identifier: Int,
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

  def apply(protection: Protection): ProtectionRecord = {
    ProtectionRecord(
      protection.id.toInt,
      protection.version,
      toRecordType(protection),
      protection.certificateDate.getOrElse("2000-01-01"), //TODO: defaults for date and time?
      protection.certificateTime.getOrElse("000000"), //TODO: defaults for date and time?
      toRecordStatus(protection),
      protection.protectionReference,
      protection.relevantAmount.map(_.toInt),
      protection.preADayPensionInPayment.map(_.toInt),
      protection.postADayBCE.map(_.toInt),
      protection.uncrystallisedRights.map(_.toInt),
      protection.nonUKRights.map(_.toInt),
      None, //TODO: Check pensionDebitAmount
      protection.pensionDebiitEnteredAmount.map(_.toInt),
      protection.protectedAmount.map(_.toInt),
      protection.pensionDebitStartDate,
      protection.pensionDebitTotalAmount.map(_.toInt),
      None,
      None,
      None
    )
  }

  private def toRecordType(protection: Protection): ProtectionType = protection.`type` match {
    case 1 => ProtectionType.FixedProtection2016
    case 2 => ProtectionType.IndividualProtection2014
    case 3 => ProtectionType.IndividualProtection2016
    case 4 => ProtectionType.PrimaryProtection
    case 5 => ProtectionType.EnhancedProtection
    case 6 => ProtectionType.FixedProtection
    case 7 => ProtectionType.FixedProtection2014
    case _ => ProtectionType.FixedProtection2014 //TODO: Change this match to accommodate other protection types
  }

  private def toRecordStatus(protection: Protection): ProtectionStatus = protection.status match {
    case 1 => ProtectionStatus.Open
    case 2 => ProtectionStatus.Dormant
    case 3 => ProtectionStatus.Withdrawn
    case 4 => ProtectionStatus.Expired
    case 5 => ProtectionStatus.Unsuccessful
    case 6 => ProtectionStatus.Rejected
    case _ => ProtectionStatus.Rejected //TODO: Change this match to accommodate other protection statuses
  }

  implicit val format: Format[ProtectionRecord] = Json.format[ProtectionRecord]
}
