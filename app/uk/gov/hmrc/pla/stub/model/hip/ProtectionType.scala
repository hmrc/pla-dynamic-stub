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
import uk.gov.hmrc.pla.stub.model.Protection
import uk.gov.hmrc.pla.stub.model.Protection.Type

sealed abstract class ProtectionType(value: String, protectionType: Type.Value) extends EnumerableInstance(value) {
  def toId: Int = Protection.extractedType(protectionType)
}

object ProtectionType extends Enumerable.Implicits {

  case object EnhancedProtection           extends ProtectionType("ENHANCED PROTECTION", Type.Enhanced)
  case object EnhancedProtectionLta        extends ProtectionType("ENHANCED PROTECTION LTA", Type.Enhanced)
  case object FixedProtection              extends ProtectionType("FIXED PROTECTION", Type.Fixed)
  case object FixedProtection2014          extends ProtectionType("FIXED PROTECTION 2014", Type.FP2014)
  case object FixedProtection2014Lta       extends ProtectionType("FIXED PROTECTION 2014 LTA", Type.FP2014)
  case object FixedProtection2016          extends ProtectionType("FIXED PROTECTION 2016", Type.FP2016)
  case object FixedProtection2016Lta       extends ProtectionType("FIXED PROTECTION 2016 LTA", Type.FP2016)
  case object FixedProtectionLta           extends ProtectionType("FIXED PROTECTION LTA", Type.Fixed)
  case object IndividualProtection2014     extends ProtectionType("INDIVIDUAL PROTECTION 2014", Type.IP2014)
  case object IndividualProtection2014Lta  extends ProtectionType("INDIVIDUAL PROTECTION 2014 LTA", Type.IP2014)
  case object IndividualProtection2016     extends ProtectionType("INDIVIDUAL PROTECTION 2016", Type.IP2016)
  case object IndividualProtection2016Lta  extends ProtectionType("INDIVIDUAL PROTECTION 2016 LTA", Type.IP2016)
  case object InternationalEnhancementS221 extends ProtectionType("INTERNATIONAL ENHANCEMENT (S221)", Type.Enhanced) // placeholder
  case object InternationalEnhancementS224 extends ProtectionType("INTERNATIONAL ENHANCEMENT (S224)", Type.Enhanced) // placeholder
  case object PensionCreditRights          extends ProtectionType("PENSION CREDIT RIGHTS", Type.Enhanced) // placeholder
  case object PrimaryProtection            extends ProtectionType("PRIMARY PROTECTION", Type.Primary)
  case object PrimaryProtectionLta         extends ProtectionType("PRIMARY PROTECTION LTA", Type.Primary)

  val values: Seq[ProtectionType] = Seq(
    FixedProtection2016,
    IndividualProtection2014,
    IndividualProtection2016,
    PrimaryProtection,
    EnhancedProtection,
    FixedProtection,
    FixedProtection2014,
    PensionCreditRights,
    InternationalEnhancementS221,
    InternationalEnhancementS224,
    FixedProtection2016LTA,
    IndividualProtection2014LTA,
    IndividualProtection2016LTA,
    PrimaryProtectionLTA,
    EnhancedProtectionLTA,
    FixedProtectionLTA,
    FixedProtection2014LTA
  )

  implicit val enumerable: Enumerable[ProtectionType] =
    Enumerable(values.map(v => v.toString -> v): _*)

  def fromPlaId(id: Int): Option[ProtectionType] = id match {
    case 1 => Some(FixedProtection2016)
    case 2 => Some(IndividualProtection2014)
    case 3 => Some(IndividualProtection2016)
    case 4 => Some(PrimaryProtection)
    case 5 => Some(EnhancedProtection)
    case 6 => Some(FixedProtection)
    case 7 => Some(FixedProtection2014)
    case _ => None

  }

}
