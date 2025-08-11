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

import play.api.libs.json._
import uk.gov.hmrc.pla.stub.model.Protection

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class HipProtection(
    nino: String,
    id: Int,
    sequence: Int,
    status: ProtectionStatus,
    `type`: ProtectionType,
    relevantAmount: Int,
    preADayPensionInPaymentAmount: Int,
    postADayBenefitCrystallisationEventAmount: Int,
    uncrystallisedRightsAmount: Int,
    nonUKRightsAmount: Int,
    certificateDate: Option[String] = None,
    certificateTime: Option[String] = None,
    protectionReference: Option[String],
    pensionDebitAmount: Option[Int] = None,
    pensionDebitEnteredAmount: Option[Int] = None,
    protectedAmount: Option[Int] = None,
    pensionDebitStartDate: Option[String] = None,
    pensionDebitTotalAmount: Option[Int] = None
) {

  def toProtection: Protection =
    Protection(
      nino = nino,
      id = id.toLong,
      version = sequence,
      `type` = `type`.toPlaId,
      status = status.toPlaId,
      notificationID = None,
      notificationMsg = None,
      protectionReference = protectionReference,
      certificateDate = certificateDate,
      certificateTime = certificateTime,
      relevantAmount = Some(relevantAmount.toDouble),
      protectedAmount = protectedAmount.map(_.toDouble),
      preADayPensionInPayment = Some(preADayPensionInPaymentAmount.toDouble),
      postADayBCE = Some(postADayBenefitCrystallisationEventAmount.toDouble),
      uncrystallisedRights = Some(uncrystallisedRightsAmount.toDouble),
      nonUKRights = Some(nonUKRightsAmount.toDouble),
      pensionDebiitEnteredAmount = pensionDebitEnteredAmount.map(_.toDouble),
      pensionDebitStartDate = pensionDebitStartDate,
      pensionDebitTotalAmount = pensionDebitTotalAmount.map(_.toDouble),
      pensionDebits = None,
      previousVersions = None,
      withdrawnDate = None
    )

}

object HipProtection {

  implicit val localDateTimeReads: Reads[LocalDateTime] =
    Reads[LocalDateTime](js => js.validate[String].map[LocalDateTime](dtString => LocalDateTime.parse(dtString)))

  implicit val localDateTimeWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    val formatter: DateTimeFormatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

    def writes(ldt: LocalDateTime): JsValue = Json.toJson(ldt.format(formatter))
  }

  implicit val localDateTimeFormat: Format[LocalDateTime] = Format(localDateTimeReads, localDateTimeWrites)

  implicit lazy val protectionFormat: Format[HipProtection] = Json.format[HipProtection]

  def fromProtection(protection: Protection): Option[HipProtection] =
    for {
      protectionType <- ProtectionType.fromPlaId(protection.`type`)
      status         <- ProtectionStatus.fromPlaId(protection.status)
    } yield HipProtection(
      nino = protection.nino,
      id = protection.id.toInt,
      sequence = protection.version,
      status = status,
      `type` = protectionType,
      preADayPensionInPaymentAmount = protection.preADayPensionInPayment.map(_.toInt).getOrElse(0),
      postADayBenefitCrystallisationEventAmount = protection.postADayBCE.map(_.toInt).getOrElse(0),
      uncrystallisedRightsAmount = protection.uncrystallisedRights.map(_.toInt).getOrElse(0),
      nonUKRightsAmount = protection.nonUKRights.map(_.toInt).getOrElse(0),
      certificateDate = protection.certificateDate,
      certificateTime = protection.certificateTime,
      protectionReference = protection.protectionReference,
      pensionDebitAmount = None,
      pensionDebitEnteredAmount = protection.pensionDebiitEnteredAmount.map(_.toInt),
      protectedAmount = protection.protectedAmount.map(_.toInt),
      pensionDebitStartDate = protection.pensionDebitStartDate,
      pensionDebitTotalAmount = protection.pensionDebitTotalAmount.map(_.toInt),
      relevantAmount = protection.relevantAmount.map(_.toInt).getOrElse(0)
    )

}
