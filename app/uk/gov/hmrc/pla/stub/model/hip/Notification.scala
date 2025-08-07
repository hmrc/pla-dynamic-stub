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

import play.api.libs.json.{Format, JsError, JsNumber, JsSuccess, Reads, Writes}
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType.{IndividualProtection2014, IndividualProtection2016}
import uk.gov.hmrc.pla.stub.model.hip.AmendProtectionResponseStatus.{Dormant, Open, Withdrawn}

sealed abstract class Notification(_id: Int, `_type`: ProtectionType, _status: AmendProtectionResponseStatus) {

  val id: Int                               = _id
  val `type`: ProtectionType         = `_type`
  val status: AmendProtectionResponseStatus = _status

}

object Notification {

  implicit val writes: Writes[Notification] = Writes(notification => JsNumber(notification.id))

  implicit val reads: Reads[Notification] = Reads {
    case JsNumber(num) =>
      num.toInt match {
        case 1  => JsSuccess(Notification1)
        case 2  => JsSuccess(Notification2)
        case 3  => JsSuccess(Notification3)
        case 4  => JsSuccess(Notification4)
        case 5  => JsSuccess(Notification5)
        case 6  => JsSuccess(Notification6)
        case 7  => JsSuccess(Notification7)
        case 8  => JsSuccess(Notification8)
        case 9  => JsSuccess(Notification9)
        case 10 => JsSuccess(Notification10)
        case 11 => JsSuccess(Notification11)
        case 12 => JsSuccess(Notification12)
        case 13 => JsSuccess(Notification13)
        case 14 => JsSuccess(Notification14)
        case n  => JsError(s"Unknown notification id '$n'. Expected number in range 1-14 inclusive.'")
      }
    case v => JsError(s"Unknown notification id '$v'. Expected number in range 1-14 inclusive.'")
  }

  implicit val format: Format[Notification] = Format(Notification.reads, Notification.writes)

  case object Notification1 extends Notification(1, IndividualProtection2014, Open)
  case object Notification2 extends Notification(1, IndividualProtection2014, Dormant)
  case object Notification3 extends Notification(3, IndividualProtection2014, Dormant)
  case object Notification4 extends Notification(4, IndividualProtection2014, Dormant)
  case object Notification5 extends Notification(5, IndividualProtection2014, Open)
  case object Notification6 extends Notification(6, IndividualProtection2014, Withdrawn)
  case object Notification7 extends Notification(7, IndividualProtection2014, Withdrawn)

  case object Notification8  extends Notification(8, IndividualProtection2016, Open)
  case object Notification9  extends Notification(9, IndividualProtection2016, Dormant)
  case object Notification10 extends Notification(10, IndividualProtection2016, Dormant)
  case object Notification11 extends Notification(11, IndividualProtection2016, Dormant)
  case object Notification12 extends Notification(12, IndividualProtection2016, Dormant)
  case object Notification13 extends Notification(13, IndividualProtection2016, Withdrawn)
  case object Notification14 extends Notification(14, IndividualProtection2016, Withdrawn)

}
