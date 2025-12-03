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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.pla.stub.model.hip.Notification._
import uk.gov.hmrc.pla.stub.model.hip.ProtectionStatus.Dormant
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType._
import uk.gov.hmrc.pla.stub.model.hip.{Protection, ProtectionStatus, ProtectionType}
import uk.gov.hmrc.pla.stub.rules.AmendmentRules._
import uk.gov.hmrc.pla.stub.testdata.RandomNinoGenerator

class AmendmentRulesSpec extends AnyWordSpec with Matchers {

  private val nino         = RandomNinoGenerator.generateNino
  private val protectionId = 1
  private val sequence     = 1

  private val protection = Protection(
    nino = nino,
    id = protectionId,
    sequence = sequence,
    status = ProtectionStatus.Open,
    `type` = IndividualProtection2014,
    relevantAmount = 1_254_000,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = None
  )

  "IndividualProtection2014AmendmentRules on calculateNotificationId" when {

    "provided with relevantAmount below threshold" when {

      val relevantAmount = 1_250_000

      "there is NO open protection" should {
        "return Notification no. 6" in {
          IndividualProtection2014AmendmentRules.calculateNotificationId(
            relevantAmount,
            List.empty
          ) shouldBe Notification6
        }
      }

      Seq(FixedProtection2016, FixedProtection2016LTA).foreach { fixedProtection2016Type =>
        s"the dormant protection type is ${fixedProtection2016Type.value}" should {
          "return Notification no. 7" in {
            val dormantProtection = protection.copy(`type` = fixedProtection2016Type, status = Dormant)

            IndividualProtection2014AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(dormantProtection, protection)
            ) shouldBe Notification7
          }
        }
      }

      ProtectionType.values.diff(Seq(FixedProtection2016, FixedProtection2016LTA)).foreach { protectionType =>
        s"the open protection type is ${protectionType.value}" should {
          "return Notification no. 6" in {
            val openProtection = protection.copy(`type` = protectionType)

            IndividualProtection2014AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe Notification6
          }
        }
      }
    }

    "provided with relevantAmount above threshold" when {

      val relevantAmount = 1_250_001

      "there is NO open protection" should {
        "return Notification no. 1" in {
          IndividualProtection2014AmendmentRules.calculateNotificationId(
            relevantAmount,
            List.empty
          ) shouldBe Notification1
        }
      }

      Seq(EnhancedProtection, EnhancedProtectionLTA).foreach { enhancedProtectionType =>
        s"the open protection type is ${enhancedProtectionType.value}" should {
          "return Notification no. 2" in {
            val openProtection = protection.copy(`type` = enhancedProtectionType)

            IndividualProtection2014AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe Notification2
          }
        }
      }

      Seq(FixedProtection, FixedProtectionLTA).foreach { fixedProtectionType =>
        s"the open protection type is ${fixedProtectionType.value}" should {
          "return Notification no. 3" in {
            val openProtection = protection.copy(`type` = fixedProtectionType)

            IndividualProtection2014AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe Notification3
          }
        }
      }

      Seq(FixedProtection2014, FixedProtection2014LTA).foreach { fixedProtection2014Type =>
        s"the open protection type is ${fixedProtection2014Type.value}" should {
          "return Notification no. 4" in {
            val openProtection = protection.copy(`type` = fixedProtection2014Type)

            IndividualProtection2014AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe Notification4
          }
        }
      }

      Seq(FixedProtection2016, FixedProtection2016LTA).foreach { fixedProtection2016Type =>
        s"the dormant protection type is ${fixedProtection2016Type.value}" should {
          "return Notification no. 5" in {
            val dormantProtection = protection.copy(`type` = fixedProtection2016Type, status = Dormant)

            IndividualProtection2014AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(dormantProtection, protection)
            ) shouldBe Notification5
          }
        }
      }

      ProtectionType.values
        .diff(
          Seq(
            EnhancedProtection,
            EnhancedProtectionLTA,
            FixedProtection,
            FixedProtectionLTA,
            FixedProtection2014,
            FixedProtection2014LTA,
            FixedProtection2016,
            FixedProtection2016LTA
          )
        )
        .foreach { protectionType =>
          s"the open protection type is ${protectionType.value}" should {
            "return Notification no. 1" in {
              val openProtection = protection.copy(`type` = protectionType)

              IndividualProtection2014AmendmentRules.calculateNotificationId(
                relevantAmount,
                List(openProtection)
              ) shouldBe Notification1
            }
          }
        }
    }
  }

  "IndividualProtection2016AmendmentRules on calculateNotificationId" when {

    "provided with relevantAmount below threshold" when {

      val relevantAmount = 1_000_000

      "there is NO open protection" should {
        "return Notification no. 13" in {
          IndividualProtection2016AmendmentRules.calculateNotificationId(
            relevantAmount,
            List.empty
          ) shouldBe Notification13
        }
      }

      Seq(FixedProtection2016, FixedProtection2016LTA).foreach { fixedProtection2016Type =>
        s"the dormant protection type is ${fixedProtection2016Type.value}" should {
          "return Notification no. 14" in {
            val openProtection    = protection.copy(`type` = IndividualProtection2016)
            val dormantProtection = protection.copy(`type` = fixedProtection2016Type, status = ProtectionStatus.Dormant)

            IndividualProtection2016AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection, dormantProtection)
            ) shouldBe Notification14
          }
        }
      }

      ProtectionType.values.diff(Seq(FixedProtection2016, FixedProtection2016LTA)).foreach { protectionType =>
        s"the open protection type is ${protectionType.value}" should {
          "return Notification no. 13" in {
            val openProtection = protection.copy(`type` = protectionType)

            IndividualProtection2016AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe Notification13
          }
        }
      }
    }

    "provided with relevantAmount above threshold" when {

      val relevantAmount = 1_000_001

      "there is NO open protection" should {
        "return Notification no. 8" in {
          IndividualProtection2016AmendmentRules.calculateNotificationId(
            relevantAmount,
            List.empty
          ) shouldBe Notification8
        }
      }

      Seq(EnhancedProtection, EnhancedProtectionLTA).foreach { enhancedProtectionType =>
        s"the open protection type is ${enhancedProtectionType.value}" should {
          "return Notification no. 9" in {
            val openProtection = protection.copy(`type` = enhancedProtectionType)

            IndividualProtection2016AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe Notification9
          }
        }
      }

      Seq(FixedProtection, FixedProtectionLTA).foreach { fixedProtectionType =>
        s"the open protection type is ${fixedProtectionType.value}" should {
          "return Notification no. 10" in {
            val openProtection = protection.copy(`type` = fixedProtectionType)

            IndividualProtection2016AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe Notification10
          }
        }
      }

      Seq(FixedProtection2014, FixedProtection2014LTA).foreach { fixedProtection2014Type =>
        s"the open protection type is ${fixedProtection2014Type.value}" should {
          "return Notification no. 11" in {
            val openProtection = protection.copy(`type` = fixedProtection2014Type)

            IndividualProtection2016AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe Notification11
          }
        }
      }

      Seq(FixedProtection2016, FixedProtection2016LTA).foreach { fixedProtection2016Type =>
        s"the open protection type is ${fixedProtection2016Type.value}" should {
          "return Notification no. 12" in {
            val openProtection = protection.copy(`type` = fixedProtection2016Type)

            IndividualProtection2016AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe Notification12
          }
        }
      }

      ProtectionType.values
        .diff(
          Seq(
            EnhancedProtection,
            EnhancedProtectionLTA,
            FixedProtection,
            FixedProtectionLTA,
            FixedProtection2014,
            FixedProtection2014LTA,
            FixedProtection2016,
            FixedProtection2016LTA
          )
        )
        .foreach { protectionType =>
          s"the open protection type is ${protectionType.value}" should {
            "return Notification no. 8" in {
              val openProtection = protection.copy(`type` = protectionType)

              IndividualProtection2016AmendmentRules.calculateNotificationId(
                relevantAmount,
                List(openProtection)
              ) shouldBe Notification8
            }
          }
        }
    }
  }

}
