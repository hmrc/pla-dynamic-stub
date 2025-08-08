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

import uk.gov.hmrc.pla.stub.model.Protection
import uk.gov.hmrc.pla.stub.model.Protection.Status
import uk.gov.hmrc.pla.stub.notifications.{CertificateStatus, Notifications}
import util.{Enumerable, EnumerableInstance}
import uk.gov.hmrc.pla.stub.utils.{Enumerable, EnumerableInstance}

sealed abstract class ProtectionStatus(value: String, status: Status.Value) extends EnumerableInstance(value) {
  def toPlaId: Int = Protection.extractedStatus(status)
}

object ProtectionStatus extends Enumerable.Implicits {

  case object Open         extends ProtectionStatus("OPEN", Status.Open)
  case object Dormant      extends ProtectionStatus("DORMANT", Status.Dormant)
  case object Withdrawn    extends ProtectionStatus("WITHDRAWN", Status.Withdrawn)
  case object Expired      extends ProtectionStatus("EXPIRED", Status.Expired)
  case object Unsuccessful extends ProtectionStatus("UNSUCCESSFUL", Status.Unsuccessful)
  case object Rejected     extends ProtectionStatus("REJECTED", Status.Rejected)

  val values: Seq[ProtectionStatus] = Seq(
    Open,
    Dormant,
    Withdrawn,
    Expired,
    Unsuccessful,
    Rejected
  )

  implicit val enumerable: Enumerable[ProtectionStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)

  def fromCertificateStatus(certificateStatus: CertificateStatus.Value): Option[ProtectionStatus] = fromPlaId(
    Notifications.extractedStatus(certificateStatus)
  )

  def fromPlaId(id: Int): Option[ProtectionStatus] = id match {
    case 1 => Some(Open)
    case 2 => Some(Dormant)
    case 3 => Some(Withdrawn)
    case 4 => Some(Expired)
    case 5 => Some(Unsuccessful)
    case 6 => Some(Rejected)
    case _ => None
  }

}
