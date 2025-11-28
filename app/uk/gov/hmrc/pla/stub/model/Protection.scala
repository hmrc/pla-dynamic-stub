/*
 * Copyright 2024 HM Revenue & Customs
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

import java.time.LocalDateTime

import play.api.libs.json.{JsValue, Json}
import play.api.libs.json.{Format, Reads, Writes}

case class Protection(
    nino: String,
    id: Long,
    version: Int,
    `type`: Int,
    status: Int,
    notificationID: Option[Short],
    notificationMsg: Option[String], // this field is stored in the DB but excluded from API responses
    protectionReference: Option[String],
    certificateDate: Option[String] = None,
    certificateTime: Option[String] = None,
    relevantAmount: Option[Double] = None,
    protectedAmount: Option[Double] = None,
    preADayPensionInPayment: Option[Double] = None,
    postADayBCE: Option[Double] = None,
    uncrystallisedRights: Option[Double] = None,
    nonUKRights: Option[Double] = None,
    pensionDebiitEnteredAmount: Option[Double] = None,
    pensionDebitStartDate: Option[String] = None,
    pensionDebitTotalAmount: Option[Double] = None,
    pensionDebits: Option[List[PensionDebit]] = None,
    previousVersions: Option[List[Version]] = None, /* not stored on DB - dynamically generated and added to response */
    withdrawnDate: Option[String] = None
)

object Protection {

  object Status extends Enumeration {
    val Unknown, Open, Dormant, Withdrawn, Expired, Unsuccessful, Rejected = Value
    implicit val format: Format[Value]                                     = EnumUtils.enumFormat(Status)
  }

  def extractedStatus(pStatus: Status.Value): Int =
    pStatus match {
      case Status.Open         => 1
      case Status.Dormant      => 2
      case Status.Withdrawn    => 3
      case Status.Expired      => 4
      case Status.Unsuccessful => 5
      case Status.Rejected     => 6
    }

  object Type extends Enumeration {
    val Primary, Enhanced, Fixed, FP2014, FP2016, IP2014, IP2016 = Value
    implicit val format: Format[Value]                           = EnumUtils.enumFormat(Type)
  }

  def extractedType(pType: Type.Value): Int =
    pType match {
      case Type.FP2016   => 1
      case Type.IP2014   => 2
      case Type.IP2016   => 3
      case Type.Primary  => 4
      case Type.Enhanced => 5
      case Type.Fixed    => 6
      case Type.FP2014   => 7
    }

  implicit val localDateTimeReads: Reads[LocalDateTime] =
    Reads[LocalDateTime](js => js.validate[String].map[LocalDateTime](dtString => LocalDateTime.parse(dtString)))

  implicit val localDateTimeWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

    def writes(ldt: LocalDateTime): JsValue = Json.toJson(ldt.format(formatter))
  }

  implicit val localDateTimeFormat: Format[LocalDateTime] = Format(localDateTimeReads, localDateTimeWrites)

  implicit lazy val protectionFormat: Format[Protection] = Json.format[Protection]

}
