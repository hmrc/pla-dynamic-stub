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

package uk.gov.hmrc.pla.stub.rules

import uk.gov.hmrc.pla.stub.model.hip.HipNotification._
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType.{
  EnhancedProtection,
  FixedProtection,
  FixedProtection2014,
  FixedProtection2016
}
import uk.gov.hmrc.pla.stub.model.hip.{HipNotification, HipProtection, ProtectionStatus}

sealed trait HipAmendmentRules {

  /** @param relevantAmount
    *   the relevant amount on the amendment request
    * @param otherExistingProtections
    *   all existing protections for the individual except the one to be amended
    * @return
    *   the outcome of the business rules check in the form of a HipNotification object with id field in 1-14 range
    */
  def calculateNotificationId(relevantAmount: Double, otherExistingProtections: List[HipProtection]): HipNotification
}

object HipAmendmentRules {

  object IndividualProtection2014AmendmentRules extends HipAmendmentRules {

    override def calculateNotificationId(
        relevantAmount: Double,
        otherExistingProtections: List[HipProtection]
    ): HipNotification = {
      val withdraw       = relevantAmount < 1_125_001
      val defaultOutcome = if (withdraw) HipNotification6 else HipNotification1

      val otherOpenProtection = otherExistingProtections.find {
        _.status == ProtectionStatus.Open
      }
      otherOpenProtection
        .map { openProtection =>
          (withdraw, openProtection.`type`) match {
            case (false, EnhancedProtection)  => HipNotification2
            case (false, FixedProtection)     => HipNotification3
            case (false, FixedProtection2014) => HipNotification4
            case (false, FixedProtection2016) => HipNotification5
            case (true, FixedProtection2016)  => HipNotification7
            case _                            => defaultOutcome
          }
        }
        .getOrElse(defaultOutcome)
    }

  }

  object IndividualProtection2016AmendmentRules extends HipAmendmentRules {

    override def calculateNotificationId(
        relevantAmount: Double,
        otherExistingProtections: List[HipProtection]
    ): HipNotification = {
      val withdraw       = relevantAmount < 1_000_001
      val defaultOutcome = if (withdraw) HipNotification13 else HipNotification8

      val otherOpenProtection = otherExistingProtections.find {
        _.status == ProtectionStatus.Open
      }
      otherOpenProtection
        .map { openProtection =>
          (withdraw, openProtection.`type`) match {
            case (false, EnhancedProtection)  => HipNotification9
            case (false, FixedProtection)     => HipNotification10
            case (false, FixedProtection2014) => HipNotification11
            case (false, FixedProtection2016) => HipNotification12
            case (true, FixedProtection2016)  => HipNotification14
            case _                            => defaultOutcome
          }
        }
        .getOrElse(defaultOutcome)
    }

  }

}
