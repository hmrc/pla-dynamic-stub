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

import play.api.libs.json.{JsNumber, Writes}
import uk.gov.hmrc.pla.stub.model.hip.AmendProtectionResponseStatus.{Dormant, Open, Withdrawn}
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType.{IndividualProtection2014, IndividualProtection2016}

enum Notification(
    val id: Int,
    val `type`: ProtectionType,
    val status: AmendProtectionResponseStatus
) {

  case Notification1 extends Notification(1, IndividualProtection2014, Open)
  case Notification2 extends Notification(2, IndividualProtection2014, Dormant)
  case Notification3 extends Notification(3, IndividualProtection2014, Dormant)
  case Notification4 extends Notification(4, IndividualProtection2014, Dormant)
  case Notification5 extends Notification(5, IndividualProtection2014, Open)
  case Notification6 extends Notification(6, IndividualProtection2014, Withdrawn)
  case Notification7 extends Notification(7, IndividualProtection2014, Withdrawn)

  case Notification8  extends Notification(8, IndividualProtection2016, Open)
  case Notification9  extends Notification(9, IndividualProtection2016, Dormant)
  case Notification10 extends Notification(10, IndividualProtection2016, Dormant)
  case Notification11 extends Notification(11, IndividualProtection2016, Dormant)
  case Notification12 extends Notification(12, IndividualProtection2016, Dormant)
  case Notification13 extends Notification(13, IndividualProtection2016, Withdrawn)
  case Notification14 extends Notification(14, IndividualProtection2016, Withdrawn)

}

object Notification {

  given Writes[Notification] = Writes(notification => JsNumber(notification.id))

}
