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

sealed abstract class ProtectionType(val value: String, val toPlaId: Int) extends EnumerableInstance(value) {}

object ProtectionType extends Enumerable.Implicits {

  case object EnhancedProtection     extends ProtectionType("ENHANCED PROTECTION", PlaId.EnhancedProtection)
  case object EnhancedProtectionLTA  extends ProtectionType("ENHANCED PROTECTION LTA", PlaId.EnhancedProtectionLTA)
  case object FixedProtection        extends ProtectionType("FIXED PROTECTION", PlaId.FixedProtection)
  case object FixedProtection2014    extends ProtectionType("FIXED PROTECTION 2014", PlaId.FixedProtection2014)
  case object FixedProtection2014LTA extends ProtectionType("FIXED PROTECTION 2014 LTA", PlaId.FixedProtection2014LTA)
  case object FixedProtection2016    extends ProtectionType("FIXED PROTECTION 2016", PlaId.FixedProtection2016)
  case object FixedProtection2016LTA extends ProtectionType("FIXED PROTECTION 2016 LTA", PlaId.FixedProtection2016LTA)
  case object FixedProtectionLTA     extends ProtectionType("FIXED PROTECTION LTA", PlaId.FixedProtectionLTA)

  case object IndividualProtection2014
      extends ProtectionType("INDIVIDUAL PROTECTION 2014", PlaId.IndividualProtection2014)

  case object IndividualProtection2014LTA
      extends ProtectionType("INDIVIDUAL PROTECTION 2014 LTA", PlaId.IndividualProtection2014LTA)

  case object IndividualProtection2016
      extends ProtectionType("INDIVIDUAL PROTECTION 2016", PlaId.IndividualProtection2016)

  case object IndividualProtection2016LTA
      extends ProtectionType("INDIVIDUAL PROTECTION 2016 LTA", PlaId.IndividualProtection2016LTA)

  case object InternationalEnhancementS221
      extends ProtectionType("INTERNATIONAL ENHANCEMENT (S221)", PlaId.InternationalEnhancementS221)

  case object InternationalEnhancementS224
      extends ProtectionType("INTERNATIONAL ENHANCEMENT (S224)", PlaId.InternationalEnhancementS224)

  case object PensionCreditRights  extends ProtectionType("PENSION CREDIT RIGHTS", PlaId.PensionCreditRights)
  case object PrimaryProtection    extends ProtectionType("PRIMARY PROTECTION", PlaId.PrimaryProtection)
  case object PrimaryProtectionLTA extends ProtectionType("PRIMARY PROTECTION LTA", PlaId.PrimaryProtectionLTA)

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
    case PlaId.FixedProtection2016          => Some(FixedProtection2016)
    case PlaId.IndividualProtection2014     => Some(IndividualProtection2014)
    case PlaId.IndividualProtection2016     => Some(IndividualProtection2016)
    case PlaId.PrimaryProtection            => Some(PrimaryProtection)
    case PlaId.EnhancedProtection           => Some(EnhancedProtection)
    case PlaId.FixedProtection              => Some(FixedProtection)
    case PlaId.FixedProtection2014          => Some(FixedProtection2014)
    case PlaId.FixedProtection2016LTA       => Some(FixedProtection2016LTA)
    case PlaId.IndividualProtection2014LTA  => Some(IndividualProtection2014LTA)
    case PlaId.IndividualProtection2016LTA  => Some(IndividualProtection2016LTA)
    case PlaId.PrimaryProtectionLTA         => Some(PrimaryProtectionLTA)
    case PlaId.EnhancedProtectionLTA        => Some(EnhancedProtectionLTA)
    case PlaId.FixedProtectionLTA           => Some(FixedProtectionLTA)
    case PlaId.FixedProtection2014LTA       => Some(FixedProtection2014LTA)
    case PlaId.InternationalEnhancementS221 => Some(InternationalEnhancementS221)
    case PlaId.InternationalEnhancementS224 => Some(InternationalEnhancementS224)
    case PlaId.PensionCreditRights          => Some(PensionCreditRights)
    case _                                  => None
  }

  private object PlaId {
    val FixedProtection2016          = 1
    val IndividualProtection2014     = 2
    val IndividualProtection2016     = 3
    val PrimaryProtection            = 4
    val EnhancedProtection           = 5
    val FixedProtection              = 6
    val FixedProtection2014          = 7
    val FixedProtection2016LTA       = 8
    val IndividualProtection2014LTA  = 9
    val IndividualProtection2016LTA  = 10
    val PrimaryProtectionLTA         = 11
    val EnhancedProtectionLTA        = 12
    val FixedProtectionLTA           = 13
    val FixedProtection2014LTA       = 14
    val InternationalEnhancementS221 = 15
    val InternationalEnhancementS224 = 16
    val PensionCreditRights          = 17
  }

}
