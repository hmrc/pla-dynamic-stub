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

sealed abstract class AmendProtectionRequestLifetimeAllowanceType(
    value: String,
    lifetimeAllowanceType: LifetimeAllowanceType
) extends EnumerableInstance(value) {

  def toLifetimeAllowanceType: LifetimeAllowanceType = lifetimeAllowanceType
}

object AmendProtectionRequestLifetimeAllowanceType extends Enumerable.Implicits {

  case object IndividualProtection2014
      extends AmendProtectionRequestLifetimeAllowanceType(
        "INDIVIDUAL PROTECTION 2014",
        LifetimeAllowanceType.IndividualProtection2014
      )

  case object IndividualProtection2016
      extends AmendProtectionRequestLifetimeAllowanceType(
        "INDIVIDUAL PROTECTION 2016",
        LifetimeAllowanceType.IndividualProtection2016
      )

  case object IndividualProtection2014Lta
      extends AmendProtectionRequestLifetimeAllowanceType(
        "INDIVIDUAL PROTECTION 2014 LTA",
        LifetimeAllowanceType.IndividualProtection2014Lta
      )

  case object IndividualProtection2016Lta
      extends AmendProtectionRequestLifetimeAllowanceType(
        "INDIVIDUAL PROTECTION 2016 LTA",
        LifetimeAllowanceType.IndividualProtection2016Lta
      )

  private val allValues: Seq[AmendProtectionRequestLifetimeAllowanceType] = Seq(
    IndividualProtection2014,
    IndividualProtection2016,
    IndividualProtection2014Lta,
    IndividualProtection2016Lta
  )

  implicit val toEnumerable: Enumerable[AmendProtectionRequestLifetimeAllowanceType] =
    Enumerable(allValues.map(v => v.toString -> v): _*)

}
