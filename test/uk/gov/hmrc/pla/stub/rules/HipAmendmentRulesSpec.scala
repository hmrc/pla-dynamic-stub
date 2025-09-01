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
import uk.gov.hmrc.pla.stub.model.hip.HipNotification._
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType._
import uk.gov.hmrc.pla.stub.model.hip.{HipProtection, ProtectionStatus, ProtectionType}
import uk.gov.hmrc.pla.stub.rules.HipAmendmentRules._
import uk.gov.hmrc.pla.stub.testdata.RandomNinoGenerator

class HipAmendmentRulesSpec extends AnyWordSpec with Matchers {

  private val nino         = RandomNinoGenerator.generateNino
  private val protectionId = 1
  private val sequence     = 1

  private val protection = HipProtection(
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
        "return HipNotification no. 6" in {
          IndividualProtection2014AmendmentRules.calculateNotificationId(
            relevantAmount,
            List.empty
          ) shouldBe HipNotification6
        }
      }

      s"the open protection type is ${FixedProtection2016.value}" should {
        "return HipNotification no. 7" in {
          val openProtection = protection.copy(`type` = FixedProtection2016)

          IndividualProtection2014AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification7
        }
      }

      ProtectionType.values.diff(Seq(FixedProtection2016)).foreach { protectionType =>
        s"the open protection type is ${protectionType.value}" should {
          "return HipNotification no. 6" in {
            val openProtection = protection.copy(`type` = protectionType)

            IndividualProtection2014AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe HipNotification6
          }
        }
      }
    }

    "provided with relevantAmount above threshold" when {

      val relevantAmount = 1_250_001

      "there is NO open protection" should {
        "return HipNotification no. 1" in {
          IndividualProtection2014AmendmentRules.calculateNotificationId(
            relevantAmount,
            List.empty
          ) shouldBe HipNotification1
        }
      }

      s"the open protection type is ${EnhancedProtection.value}" should {
        "return HipNotification no. 2" in {
          val openProtection = protection.copy(`type` = EnhancedProtection)

          IndividualProtection2014AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification2
        }
      }

      s"the open protection type is ${FixedProtection.value}" should {
        "return HipNotification no. 3" in {
          val openProtection = protection.copy(`type` = FixedProtection)

          IndividualProtection2014AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification3
        }
      }

      s"the open protection type is ${FixedProtection2014.value}" should {
        "return HipNotification no. 4" in {
          val openProtection = protection.copy(`type` = FixedProtection2014)

          IndividualProtection2014AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification4
        }
      }

      s"the open protection type is ${FixedProtection2016.value}" should {
        "return HipNotification no. 5" in {
          val openProtection = protection.copy(`type` = FixedProtection2016)

          IndividualProtection2014AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification5
        }
      }

      ProtectionType.values
        .diff(Seq(EnhancedProtection, FixedProtection, FixedProtection2014, FixedProtection2016))
        .foreach { protectionType =>
          s"the open protection type is ${protectionType.value}" should {
            "return HipNotification no. 1" in {
              val openProtection = protection.copy(`type` = protectionType)

              IndividualProtection2014AmendmentRules.calculateNotificationId(
                relevantAmount,
                List(openProtection)
              ) shouldBe HipNotification1
            }
          }
        }
    }
  }

  "IndividualProtection2016AmendmentRules on calculateNotificationId" when {

    "provided with relevantAmount below threshold" when {

      val relevantAmount = 1_000_000

      "there is NO open protection" should {
        "return HipNotification no. 13" in {
          IndividualProtection2016AmendmentRules.calculateNotificationId(
            relevantAmount,
            List.empty
          ) shouldBe HipNotification13
        }
      }

      s"the open protection type is ${FixedProtection2016.value}" should {
        "return HipNotification no. 14" in {
          val openProtection = protection.copy(`type` = FixedProtection2016)

          IndividualProtection2016AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification14
        }
      }

      ProtectionType.values.diff(Seq(FixedProtection2016)).foreach { protectionType =>
        s"the open protection type is ${protectionType.value}" should {
          "return HipNotification no. 13" in {
            val openProtection = protection.copy(`type` = protectionType)

            IndividualProtection2016AmendmentRules.calculateNotificationId(
              relevantAmount,
              List(openProtection)
            ) shouldBe HipNotification13
          }
        }
      }
    }

    "provided with relevantAmount above threshold" when {

      val relevantAmount = 1_000_001

      "there is NO open protection" should {
        "return HipNotification no. 8" in {
          IndividualProtection2016AmendmentRules.calculateNotificationId(
            relevantAmount,
            List.empty
          ) shouldBe HipNotification8
        }
      }

      s"the open protection type is ${EnhancedProtection.value}" should {
        "return HipNotification no. 9" in {
          val openProtection = protection.copy(`type` = EnhancedProtection)

          IndividualProtection2016AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification9
        }
      }

      s"the open protection type is ${FixedProtection.value}" should {
        "return HipNotification no. 10" in {
          val openProtection = protection.copy(`type` = FixedProtection)

          IndividualProtection2016AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification10
        }
      }

      s"the open protection type is ${FixedProtection2014.value}" should {
        "return HipNotification no. 11" in {
          val openProtection = protection.copy(`type` = FixedProtection2014)

          IndividualProtection2016AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification11
        }
      }

      s"the open protection type is ${FixedProtection2016.value}" should {
        "return HipNotification no. 12" in {
          val openProtection = protection.copy(`type` = FixedProtection2016)

          IndividualProtection2016AmendmentRules.calculateNotificationId(
            relevantAmount,
            List(openProtection)
          ) shouldBe HipNotification12
        }
      }

      ProtectionType.values
        .diff(Seq(EnhancedProtection, FixedProtection, FixedProtection2014, FixedProtection2016))
        .foreach { protectionType =>
          s"the open protection type is ${protectionType.value}" should {
            "return HipNotification no. 8" in {
              val openProtection = protection.copy(`type` = protectionType)

              IndividualProtection2016AmendmentRules.calculateNotificationId(
                relevantAmount,
                List(openProtection)
              ) shouldBe HipNotification8
            }
          }
        }
    }
  }

}
