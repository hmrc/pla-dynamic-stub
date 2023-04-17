/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.pla.stub.model.Protection
import Protection.Type._
import Protection.Status._
import uk.gov.hmrc.pla.stub.model.Protection
import uk.gov.hmrc.pla.stub.notifications.Notifications


/**
  * Applies the relevant business rules for an application or amendment, with inputs being whatever existing protections are in
  * place
  */
trait ApplicationRules {
  /**
    *
    * @param existingProtections all existing protections for the individual at the time of the amendment or application
    * @return the outcome of the business rules check in the form of a notification ID - should be greater than 1 but 0
    *         is reserved for the case where something seriously goes wrong (most likely invalid existing protection data)
    */
  def check(existingProtections: List[Protection]): Short
}

trait AmendmentRules {
  /**
    *
    * @param relevantAmount the relevant amount on the amendment request
    * @param existingStatus the current status of the  protection to be amended
    * @param otherExistingProtections all existing protections for the individual except the one to be amended
    * @return the outcome of the business rules check in the form of a notification Id: should be >= 1
    */
  def check(relevantAmount: Double, otherExistingProtections: List[Protection]): Short
}

// Check Application for an IP2014 protection
object IP2014ApplicationRules extends ApplicationRules {
  override def check(existingProtections: List[Protection]) = {
    val defaultOutcome=4
    existingProtections.foldLeft(defaultOutcome) { (provisionalOutcome, protection) => {
        val pStatus = protection.requestedStatus.get
        val pType = protection.requestedType.get
        provisionalOutcome match {
          case id if Notifications.isFailedApplication(id) => provisionalOutcome // propagate failed status
          case _ if pStatus==Open => pType match {
            case Primary => 1
            case IP2014 => 2
            case IP2016 => 3
            case Enhanced => 5
            case Fixed => 6
            case FP2014 => 7
            case FP2016 => 8
          }
          case _ => provisionalOutcome // ignore non-open protections
        }
      }
    }
  }.toShort
}

// Check Application for an IP2016 protection
object IP2016ApplicationRules extends ApplicationRules {
  override def check(existingProtections: List[Protection]) = {
    val defaultOutcome = 12  // default successful outcome
    existingProtections.foldLeft(defaultOutcome) { (provisionalOutcome, protection) => {
        val pStatus = protection.requestedStatus.get
        val pType = protection.requestedType.get
        provisionalOutcome match {
          case id if Notifications.isFailedApplication(id) => provisionalOutcome  // propagates failed status
          case _ if  pStatus==Dormant && pType==Primary => 9 //
          case _ if  pStatus==Open => pType match {
            case Primary => 9
            case IP2014 => 10
            case IP2016 => 11
            case Enhanced => 13
            case Fixed => 14
            case FP2014 => 15
            case FP2016 => 16
          }
          case _ => provisionalOutcome // ignore other protection status / type combinations
        }
      }
    }
  }.toShort
}

// Check Application for an FP2016 protection
object FP2016ApplicationRules extends ApplicationRules {
  override def check(existingProtections: List[Protection]) = {
    val defaultOutcome = 22 // default successful outcome if no other relevant protections exist
    existingProtections.foldLeft(defaultOutcome) { (provisionalOutcome, protection) => {
        val pStatus = protection.requestedStatus.get
        val pType = protection.requestedType.get
        provisionalOutcome match {
          case id if Notifications.isFailedApplication(id) => provisionalOutcome  // propagate failed status
          case _ if pStatus == Dormant && pType == Primary => 18
          case _ if pStatus == Open => pType match {
            case Primary => 18
            case IP2014 => 24
            case IP2016 => 23
            case Enhanced => 17
            case Fixed => 19
            case FP2014 => 20
            case FP2016 => 21
          }
          case _ => provisionalOutcome // ignore protections with other status/type combinations
        }
      }
    }
  }.toShort
}

// Check amendment of an IP2014 protection
object IP2014AmendmentRules extends AmendmentRules {
  override def check(relevantAmount: Double, otherExistingProtections: List[Protection]) = {
    val doWithdrawProtection = (relevantAmount < 1250001.0)
    val defaultOutcome = if (doWithdrawProtection) 25 else 34

    val otherOpenProtectionOpt = otherExistingProtections.find {
      _.status == Protection.extractedStatus(Protection.Status.Open)
    }
    otherOpenProtectionOpt map { openProtection: Protection =>
      (doWithdrawProtection, openProtection.requestedType.get) match {
        case (true, Enhanced) => 26
        case (true, Fixed) => 27
        case (true, FP2014) => 28
        case (true, FP2016) => 29
        case (true, _) => 25
        case (false, Enhanced) => 30
        case (false, Fixed) => 31
        case (false, FP2014) => 32
        case (false, FP2016) => 33
        case (false, _) => 34
      }
    } getOrElse defaultOutcome
  }.toShort
}

// Check amendment of an IP2016 protection
object IP2016AmendmentRules extends AmendmentRules {
  override def check(relevantAmount: Double, otherExistingProtections: List[Protection]) = {
    val doWithdrawProtection = (relevantAmount < 1000001.0)
    val defaultOutcome = if (doWithdrawProtection) 35 else 44

    val otherOpenProtectionOpt = otherExistingProtections.find {
      _.status == Protection.extractedStatus(Protection.Status.Open)
    }
    otherOpenProtectionOpt map { openProtection: Protection =>
      (doWithdrawProtection, openProtection.requestedType.get) match {
        case (true, Enhanced) => 36
        case (true, Fixed) => 37
        case (true, FP2014) => 38
        case (true, FP2016) => 39
        case (true, _) => 35
        case (false, Enhanced) => 40
        case (false, Fixed) => 41
        case (false, FP2014) => 42
        case (false, FP2016) => 43
        case (false, _) => 44
      }
    } getOrElse defaultOutcome
  }.toShort
}
