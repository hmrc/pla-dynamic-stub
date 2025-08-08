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

import _root_.util.{Enumerable, EnumerableInstance}

sealed abstract class AmendProtectionRequestType(
    value: String,
    lifetimeAllowanceType: ProtectionType
) extends EnumerableInstance(value) {

  def toProtectionType: ProtectionType = lifetimeAllowanceType
}

object AmendProtectionRequestType extends Enumerable.Implicits {

  case object IndividualProtection2014
      extends AmendProtectionRequestType(
        "INDIVIDUAL PROTECTION 2014",
        ProtectionType.IndividualProtection2014
      )

  case object IndividualProtection2014Lta
    extends AmendProtectionRequestType(
      "INDIVIDUAL PROTECTION 2014 LTA",
      ProtectionType.IndividualProtection2014Lta
    )

  case object IndividualProtection2016
      extends AmendProtectionRequestType(
        "INDIVIDUAL PROTECTION 2016",
        ProtectionType.IndividualProtection2016
      )

  case object IndividualProtection2016Lta
    extends AmendProtectionRequestType(
      "INDIVIDUAL PROTECTION 2016 LTA",
      ProtectionType.IndividualProtection2016Lta
    )

  private val allValues: Seq[AmendProtectionRequestType] = Seq(
    IndividualProtection2014,
    IndividualProtection2016,
    IndividualProtection2014Lta,
    IndividualProtection2016Lta
  )

  implicit val toEnumerable: Enumerable[AmendProtectionRequestType] =
    Enumerable(allValues.map(v => v.toString -> v): _*)

}
