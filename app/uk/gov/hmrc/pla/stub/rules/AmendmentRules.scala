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

import uk.gov.hmrc.pla.stub.model.hip.Notification._
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType.{
  EnhancedProtection,
  EnhancedProtectionLTA,
  FixedProtection,
  FixedProtection2014,
  FixedProtection2014LTA,
  FixedProtection2016,
  FixedProtection2016LTA,
  FixedProtectionLTA
}
import uk.gov.hmrc.pla.stub.model.hip.{Notification, Protection, ProtectionStatus, ProtectionType}

sealed trait AmendmentRules {

  /** @param relevantAmount
    *   the relevant amount on the amendment request
    * @param otherExistingProtections
    *   all existing protections for the individual except the one to be amended
    * @return
    *   the outcome of the business rules check in the form of a HipNotification object with id field in 1-14 range
    */
  def calculateNotificationId(relevantAmount: Double, otherExistingProtections: List[Protection]): Notification
}

object AmendmentRules {

  object IndividualProtection2014AmendmentRules extends AmendmentRules {

    override def calculateNotificationId(
        relevantAmount: Double,
        otherExistingProtections: List[Protection]
    ): Notification = {
      val withdraw       = relevantAmount < 1_250_001
      val defaultOutcome = if (withdraw) Notification6 else Notification1

      val otherOpenProtection        = otherExistingProtections.find(_.status == ProtectionStatus.Open)
      val dormantFixedProtection2016 = otherExistingProtections.exists(isDormantFixedProtection2016)

      otherOpenProtection
        .map { openProtection =>
          (withdraw, openProtection.`type`, dormantFixedProtection2016) match {
            case (false, EnhancedProtection | EnhancedProtectionLTA, false)   => Notification2
            case (false, FixedProtection | FixedProtectionLTA, false)         => Notification3
            case (false, FixedProtection2014 | FixedProtection2014LTA, false) => Notification4
            case (false, _, true)                                             => Notification5
            case (true, _, true)                                              => Notification7
            case _                                                            => defaultOutcome
          }
        }
        .getOrElse(defaultOutcome)
    }

  }

  object IndividualProtection2016AmendmentRules extends AmendmentRules {

    override def calculateNotificationId(
        relevantAmount: Double,
        otherExistingProtections: List[Protection]
    ): Notification = {
      val withdraw       = relevantAmount < 1_000_001
      val defaultOutcome = if (withdraw) Notification13 else Notification8

      val otherOpenProtection        = otherExistingProtections.find(_.status == ProtectionStatus.Open)
      val dormantFixedProtection2016 = otherExistingProtections.exists(isDormantFixedProtection2016)
      otherOpenProtection
        .map { openProtection =>
          (withdraw, openProtection.`type`, dormantFixedProtection2016) match {
            case (false, EnhancedProtection | EnhancedProtectionLTA, false)   => Notification9
            case (false, FixedProtection | FixedProtectionLTA, false)         => Notification10
            case (false, FixedProtection2014 | FixedProtection2014LTA, false) => Notification11
            case (false, FixedProtection2016 | FixedProtection2016LTA, false) => Notification12
            case (true, _, true)                                              => Notification14
            case _                                                            => defaultOutcome
          }
        }
        .getOrElse(defaultOutcome)
    }

  }

  private def isFixedProtection2016(protectionType: ProtectionType): Boolean =
    protectionType match {
      case FixedProtection2016    => true
      case FixedProtection2016LTA => true
      case _                      => false
    }

  private def isDormantFixedProtection2016(protection: Protection): Boolean =
    isFixedProtection2016(protection.`type`) && protection.status == ProtectionStatus.Dormant

}
