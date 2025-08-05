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

sealed abstract class LifetimeAllowanceType(value: String) extends EnumerableInstance(value)

object LifetimeAllowanceType extends Enumerable.Implicits {

  case object EnhancedProtection           extends LifetimeAllowanceType("ENHANCED PROTECTION")
  case object EnhancedProtectionLta        extends LifetimeAllowanceType("ENHANCED PROTECTION LTA")
  case object FixedProtection              extends LifetimeAllowanceType("FIXED PROTECTION")
  case object FixedProtection2014          extends LifetimeAllowanceType("FIXED PROTECTION 2014")
  case object FixedProtection2014Lta       extends LifetimeAllowanceType("FIXED PROTECTION 2014 LTA")
  case object FixedProtection2016Lta       extends LifetimeAllowanceType("FIXED PROTECTION 2016 LTA")
  case object FixedProtectionLta           extends LifetimeAllowanceType("FIXED PROTECTION LTA")
  case object IndividualProtection2014     extends LifetimeAllowanceType("INDIVIDUAL PROTECTION 2014")
  case object IndividualProtection2014Lta  extends LifetimeAllowanceType("INDIVIDUAL PROTECTION 2014 LTA")
  case object IndividualProtection2016     extends LifetimeAllowanceType("INDIVIDUAL PROTECTION 2016")
  case object IndividualProtection2016Lta  extends LifetimeAllowanceType("INDIVIDUAL PROTECTION 2016 LTA")
  case object InternationalEnhancementS221 extends LifetimeAllowanceType("INTERNATIONAL ENHANCEMENT (S221)")
  case object InternationalEnhancementS224 extends LifetimeAllowanceType("INTERNATIONAL ENHANCEMENT (S224)")
  case object PensionCreditRights          extends LifetimeAllowanceType("PENSION CREDIT RIGHTS")
  case object PrimaryProtection            extends LifetimeAllowanceType("PRIMARY PROTECTION")
  case object PrimaryProtectionLta         extends LifetimeAllowanceType("PRIMARY PROTECTION LTA")

  private val allValues: Seq[LifetimeAllowanceType] = Seq(
    EnhancedProtection,
    EnhancedProtectionLta,
    FixedProtection,
    FixedProtection2014,
    FixedProtection2014Lta,
    FixedProtection2016Lta,
    FixedProtectionLta,
    IndividualProtection2014,
    IndividualProtection2014Lta,
    IndividualProtection2016,
    IndividualProtection2016Lta,
    InternationalEnhancementS221,
    InternationalEnhancementS224,
    PensionCreditRights,
    PrimaryProtection,
    PrimaryProtectionLta
  )

  implicit val toEnumerable: Enumerable[LifetimeAllowanceType] =
    Enumerable(allValues.map(v => v.toString -> v): _*)

}
