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

import uk.gov.hmrc.pla.stub.utils.{Enumerable, EnumerableInstance}

sealed abstract class AmendProtectionLifetimeAllowanceType(val value: String, lifetimeAllowanceType: ProtectionType)
    extends EnumerableInstance(value) {

  def toProtectionType: ProtectionType = lifetimeAllowanceType
}

object AmendProtectionLifetimeAllowanceType extends Enumerable.Implicits {

  case object IndividualProtection2014
      extends AmendProtectionLifetimeAllowanceType(
        "INDIVIDUAL PROTECTION 2014",
        ProtectionType.IndividualProtection2014
      )

  case object IndividualProtection2016
      extends AmendProtectionLifetimeAllowanceType(
        "INDIVIDUAL PROTECTION 2016",
        ProtectionType.IndividualProtection2016
      )

  case object IndividualProtection2014LTA
      extends AmendProtectionLifetimeAllowanceType(
        "INDIVIDUAL PROTECTION 2014 LTA",
        ProtectionType.IndividualProtection2014LTA
      )

  case object IndividualProtection2016LTA
      extends AmendProtectionLifetimeAllowanceType(
        "INDIVIDUAL PROTECTION 2016 LTA",
        ProtectionType.IndividualProtection2016LTA
      )

  val values: Seq[AmendProtectionLifetimeAllowanceType] = Seq(
    IndividualProtection2014,
    IndividualProtection2016,
    IndividualProtection2014LTA,
    IndividualProtection2016LTA
  )

  implicit val toEnumerable: Enumerable[AmendProtectionLifetimeAllowanceType] =
    Enumerable(values.map(v => v.toString -> v): _*)

  def from(protectionType: ProtectionType): AmendProtectionLifetimeAllowanceType = protectionType match {
    case ProtectionType.IndividualProtection2014    => AmendProtectionLifetimeAllowanceType.IndividualProtection2014
    case ProtectionType.IndividualProtection2016    => AmendProtectionLifetimeAllowanceType.IndividualProtection2016
    case ProtectionType.IndividualProtection2014LTA => AmendProtectionLifetimeAllowanceType.IndividualProtection2014LTA
    case ProtectionType.IndividualProtection2016LTA => AmendProtectionLifetimeAllowanceType.IndividualProtection2016LTA
    case _ =>
      throw new IllegalArgumentException(
        s"Cannot convert ProtectionType: ${protectionType.value} into AmendProtectionLifetimeAllowanceType. Possible protection types are: ${values.map(_.value).mkString("[", ", ", "]")}"
      )
  }

}
