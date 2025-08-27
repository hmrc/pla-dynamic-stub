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
import uk.gov.hmrc.pla.stub.model.hip.ProtectionStatus.{Dormant, Open, Withdrawn}
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType.{IndividualProtection2014, IndividualProtection2016}

sealed abstract class HipNotification(val id: Int, val `type`: ProtectionType, val status: ProtectionStatus)

object HipNotification {

  implicit val writes: Writes[HipNotification] = Writes(notification => JsNumber(notification.id))

  case object HipNotification1 extends HipNotification(1, IndividualProtection2014, Open)
  case object HipNotification2 extends HipNotification(2, IndividualProtection2014, Dormant)
  case object HipNotification3 extends HipNotification(3, IndividualProtection2014, Dormant)
  case object HipNotification4 extends HipNotification(4, IndividualProtection2014, Dormant)
  case object HipNotification5 extends HipNotification(5, IndividualProtection2014, Open)
  case object HipNotification6 extends HipNotification(6, IndividualProtection2014, Withdrawn)
  case object HipNotification7 extends HipNotification(7, IndividualProtection2014, Withdrawn)

  case object HipNotification8  extends HipNotification(8, IndividualProtection2016, Open)
  case object HipNotification9  extends HipNotification(9, IndividualProtection2016, Dormant)
  case object HipNotification10 extends HipNotification(10, IndividualProtection2016, Dormant)
  case object HipNotification11 extends HipNotification(11, IndividualProtection2016, Dormant)
  case object HipNotification12 extends HipNotification(12, IndividualProtection2016, Dormant)
  case object HipNotification13 extends HipNotification(13, IndividualProtection2016, Withdrawn)
  case object HipNotification14 extends HipNotification(14, IndividualProtection2016, Withdrawn)

}
