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

import util.{Enumerable, EnumerableInstance}

sealed abstract class AmendProtectionResponseStatus(value: String, status: ProtectionStatus)
    extends EnumerableInstance(value) {
  def toProtectionStatus: ProtectionStatus = status
}

object AmendProtectionResponseStatus extends Enumerable.Implicits {

  case object Open      extends AmendProtectionResponseStatus("OPEN", ProtectionStatus.Open)
  case object Dormant   extends AmendProtectionResponseStatus("DORMANT", ProtectionStatus.Dormant)
  case object Withdrawn extends AmendProtectionResponseStatus("WITHDRAWN", ProtectionStatus.Withdrawn)

  private val allValues: Seq[AmendProtectionResponseStatus] =
    Seq(Open, Dormant, Withdrawn)

  implicit val toEnumerable: Enumerable[AmendProtectionResponseStatus] =
    Enumerable(allValues.map(v => v.toString -> v): _*)

}
