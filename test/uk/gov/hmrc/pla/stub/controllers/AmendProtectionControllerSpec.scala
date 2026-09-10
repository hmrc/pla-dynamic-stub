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

package uk.gov.hmrc.pla.stub.controllers

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.ControllerComponents
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import uk.gov.hmrc.domain.NinoGenerator
import uk.gov.hmrc.pla.stub.model.hip.*
import uk.gov.hmrc.pla.stub.model.hip.AmendProtectionLifetimeAllowanceType.*
import uk.gov.hmrc.pla.stub.model.hip.Notification.*
import uk.gov.hmrc.pla.stub.model.{DateModel, TimeModel}
import uk.gov.hmrc.pla.stub.services.ProtectionService

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.{ExecutionContext, Future}

class AmendProtectionControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val controllerComponents: ControllerComponents = stubControllerComponents()
  private val protectionService: ProtectionService       = mock[ProtectionService]

  private val executionContext: ExecutionContext = ExecutionContext.global

  private val stubNow: Instant  = Instant.parse("2025-08-15T12:34:56Z")
  private def fixedClock: Clock = Clock.fixed(stubNow, ZoneOffset.UTC)

  private val controller: AmendProtectionController = new AmendProtectionController(
    controllerComponents,
    protectionService
  )(using executionContext, fixedClock)

  val ninoGenerator: NinoGenerator = new NinoGenerator()
  def randomNino: String           = ninoGenerator.nextNino.nino.replaceFirst("MA", "AA")

  override def beforeEach(): Unit = {
    reset(protectionService)
    super.beforeEach()
  }

  val validAmendProtectionRequestInput: JsValue = validAmendProtectionRequestInput()

  def validAmendProtectionRequestInput(
      protectionType: AmendProtectionLifetimeAllowanceType =
        AmendProtectionLifetimeAllowanceType.IndividualProtection2014,
      certificateDate: String = "2025-08-15",
      certificateTime: String = "123456",
      status: AmendProtectionRequestStatus = AmendProtectionRequestStatus.Open,
      protectionReference: String = "IP123456789012B",
      relevantAmount: Int = 39500,
      preADayPensionInPaymentAmount: Int = 1500,
      postADayBenefitCrystallisationEventAmount: Int = 2500,
      uncrystallisedRightsAmount: Int = 75500,
      nonUKRightsAmount: Int = 0,
      pensionDebitAmount: Int = 25000,
      pensionDebitEnteredAmount: Int = 25000,
      notificationIdentifier: Int = 3,
      protectedAmount: Int = 120000,
      pensionDebitStartDate: String = "2026-07-09",
      pensionDebitTotalAmount: Int = 40000
  ): JsValue =
    Json.obj(
      "lifetimeAllowanceProtectionRecord" -> Json.obj(
        "type"                                      -> protectionType.jsonString,
        "certificateDate"                           -> certificateDate,
        "certificateTime"                           -> certificateTime,
        "status"                                    -> status.jsonString,
        "protectionReference"                       -> protectionReference,
        "relevantAmount"                            -> relevantAmount,
        "preADayPensionInPaymentAmount"             -> preADayPensionInPaymentAmount,
        "postADayBenefitCrystallisationEventAmount" -> postADayBenefitCrystallisationEventAmount,
        "uncrystallisedRightsAmount"                -> uncrystallisedRightsAmount,
        "nonUKRightsAmount"                         -> nonUKRightsAmount,
        "pensionDebitAmount"                        -> pensionDebitAmount,
        "pensionDebitEnteredAmount"                 -> pensionDebitEnteredAmount,
        "notificationIdentifier"                    -> notificationIdentifier,
        "protectedAmount"                           -> protectedAmount,
        "pensionDebitStartDate"                     -> pensionDebitStartDate,
        "pensionDebitTotalAmount"                   -> pensionDebitTotalAmount
      )
    )

  val validAmendProtectionResponse: JsValue = validAmendProtectionResponse()

  def validAmendProtectionResponse(
      protectionType: AmendProtectionLifetimeAllowanceType = IndividualProtection2014,
      identifier: Long = 12960000000123L,
      sequenceNumber: Int = 2,
      certificateDate: String = "2025-08-15",
      certificateTime: String = "123456",
      status: AmendProtectionResponseStatus = AmendProtectionResponseStatus.Withdrawn,
      protectionReference: String = "IP123456789012B",
      relevantAmount: Int = 27000,
      preADayPensionInPaymentAmount: Int = 1500,
      postADayBenefitCrystallisationEventAmount: Int = 2500,
      uncrystallisedRightsAmount: Int = 75500,
      nonUKRightsAmount: Int = 0,
      notificationIdentifier: Int = 6,
      protectedAmount: Int = 0,
      pensionDebitTotalAmount: Int = 52500
  ): JsValue =
    Json.obj(
      "updatedLifetimeAllowanceProtectionRecord" -> Json.obj(
        "identifier"                                -> identifier,
        "sequenceNumber"                            -> sequenceNumber,
        "type"                                      -> protectionType.jsonString,
        "certificateDate"                           -> certificateDate,
        "certificateTime"                           -> certificateTime,
        "status"                                    -> status.jsonString,
        "protectionReference"                       -> protectionReference,
        "relevantAmount"                            -> relevantAmount,
        "preADayPensionInPaymentAmount"             -> preADayPensionInPaymentAmount,
        "postADayBenefitCrystallisationEventAmount" -> postADayBenefitCrystallisationEventAmount,
        "uncrystallisedRightsAmount"                -> uncrystallisedRightsAmount,
        "nonUKRightsAmount"                         -> nonUKRightsAmount,
        "notificationIdentifier"                    -> notificationIdentifier,
        "protectedAmount"                           -> protectedAmount,
        "pensionDebitTotalAmount"                   -> pensionDebitTotalAmount
      )
    )

  val invalidAmendProtectionRequestInput: JsValue =
    Json.obj(
      "lifetimeAllowanceProtectionRecord" -> Json.obj(
        "type"                                      -> "INDIVIDUAL PROTECTION 2014",
        "certificateDate"                           -> "2025-08-15",
        "certificateTime"                           -> "123456",
        "status"                                    -> "CLOSED",
        "protectionReference"                       -> "IP123456789012B",
        "relevantAmount"                            -> 105000,
        "preADayPensionInPaymentAmount"             -> 1500,
        "postADayBenefitCrystallisationEventAmount" -> 2500,
        "uncrystallisedRightsAmount"                -> 75500,
        "nonUKRightsAmount"                         -> 0,
        "pensionDebitAmount"                        -> 25000,
        "pensionDebitEnteredAmount"                 -> 25000,
        "notificationIdentifier"                    -> 3,
        "protectedAmount"                           -> 120000,
        "pensionDebitStartDate"                     -> "2026-07-09",
        "pensionDebitTotalAmount"                   -> 40000
      )
    )

  "amendProtections" must {

    "return 200 with correct response body" when {
      val values = Seq(
        AmendProtectionLifetimeAllowanceType.IndividualProtection2014    -> 6,
        AmendProtectionLifetimeAllowanceType.IndividualProtection2014LTA -> 6,
        AmendProtectionLifetimeAllowanceType.IndividualProtection2016    -> 13,
        AmendProtectionLifetimeAllowanceType.IndividualProtection2016LTA -> 13
      )

      values.foreach { case (protectionType, notificationIdentifier) =>
        s"the protection type is $protectionType" in {
          val nino                = randomNino
          val protectionId        = 12960000000123L
          val sequence            = 1
          val protectionReference = "IP123456789012B"

          val protection = Protection(
            nino = nino,
            id = protectionId,
            sequence = sequence,
            status = ProtectionStatus.Open,
            `type` = protectionType.toProtectionType,
            relevantAmount = 105000,
            preADayPensionInPaymentAmount = 1500,
            postADayBenefitCrystallisationEventAmount = 2500,
            uncrystallisedRightsAmount = 75_500,
            nonUKRightsAmount = 0,
            certificateDate = DateModel.of(2025, 8, 15),
            certificateTime = TimeModel.of(12, 34, 56),
            protectionReference = Some(protectionReference),
            pensionDebitAmount = Some(25_000),
            pensionDebitEnteredAmount = Some(25_000),
            protectedAmount = Some(120_000),
            pensionDebitStartDate = Some(DateModel.of(2025, 7, 9)),
            pensionDebitTotalAmount = Some(40_000),
            lumpSumAmount = None,
            lumpSumPercentage = None,
            enhancementFactor = None
          )

          when(protectionService.findProtectionByNinoAndId(eqTo(nino), eqTo(protectionId)))
            .thenReturn(Future.successful(Some(protection)))

          when(protectionService.findAllProtectionsByNino(eqTo(nino)))
            .thenReturn(Future.successful(List(protection)))

          when(protectionService.insertOrUpdateProtection(any())).thenReturn(Future.successful(Ok))

          val result = controller
            .amendProtection(nino, protectionId, 1)
            .apply(
              FakeRequest(
                "POST",
                s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
              ).withBody(validAmendProtectionRequestInput(protectionType = protectionType))
            )

          status(result) shouldBe OK

          val resultBody = contentAsJson(result).asInstanceOf[JsObject]

          resultBody.shouldBe(
            validAmendProtectionResponse(
              protectionType = protectionType,
              notificationIdentifier = notificationIdentifier
            )
          )
        }
      }
    }

    "return 400 with invalid request body" when {
      "provided with request that does not parse from Json" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "List(JsonValidationError(List(Received unknown AmendProtectionRequestStatus$: CLOSED)"

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(invalidAmendProtectionRequestInput)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }
    }

    "return 404 with no matching protection in db" in {
      val nino         = randomNino
      val protectionId = 12960000000123L
      val sequence     = 1
      val error        = "protection to amend not found"

      when(protectionService.findProtectionByNinoAndId(eqTo(nino), eqTo(protectionId)))
        .thenReturn(Future.successful(None))

      val result = controller
        .amendProtection(nino, protectionId, 1)
        .apply(
          FakeRequest(
            "POST",
            s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
          ).withBody(validAmendProtectionRequestInput)
        )

      status(result) shouldBe NOT_FOUND
      contentAsString(result) should include(error)
    }
  }

  "calculateMaxProtectedAmount" should {

    "return £1,500,000" when
      Seq(
        IndividualProtection2014,
        IndividualProtection2014LTA
      ).foreach { protectionType =>
        s"provided with $protectionType" in {
          controller.calculateMaxProtectedAmount(protectionType) shouldBe 1_500_000
        }
      }

    "return £1,250,000" when
      Seq(
        IndividualProtection2016,
        IndividualProtection2016LTA
      ).foreach { protectionType =>
        s"provided with $protectionType" in {
          controller.calculateMaxProtectedAmount(protectionType) shouldBe 1_250_000
        }
      }

  }

  "opensDormantFixedProtection2016" should {

    "return true" when
      Seq(
        Notification7,
        Notification14
      ).foreach { notification =>
        s"provided with notification ID ${notification.id}" in {
          controller.opensDormantFixedProtection2016(notification) shouldBe true
        }
      }

    "return false" when
      Seq(
        Notification1,
        Notification2,
        Notification3,
        Notification4,
        Notification5,
        Notification6,
        Notification8,
        Notification9,
        Notification10,
        Notification11,
        Notification12,
        Notification13
      ).foreach { notification =>
        s"provided with notification ID ${notification.id}" in {
          controller.opensDormantFixedProtection2016(notification) shouldBe false
        }
      }

  }

  "calculateAdjustedEnteredAmount" should {
    "return the correct entered amount" when
      Seq(
        (600, (2015, 4, 6)) -> 600,
        (600, (2015, 9, 8)) -> 600,
        (600, (2016, 4, 5)) -> 600,
        (600, (2016, 4, 6)) -> 600,
        (600, (2016, 9, 8)) -> 600,
        (600, (2017, 4, 5)) -> 600,
        (600, (2017, 4, 6)) -> 570,
        (600, (2017, 9, 8)) -> 570,
        (600, (2018, 4, 5)) -> 570,
        (600, (2018, 4, 6)) -> 540,
        (600, (2018, 9, 8)) -> 540,
        (600, (2019, 4, 5)) -> 540,
        (600, (2019, 4, 6)) -> 510,
        (600, (2019, 9, 8)) -> 510,
        (600, (2020, 4, 5)) -> 510,
        (600, (2020, 4, 6)) -> 480,
        (600, (2020, 9, 8)) -> 480,
        (600, (2021, 4, 5)) -> 480,
        (600, (2021, 4, 6)) -> 450,
        (600, (2021, 9, 8)) -> 450,
        (600, (2022, 4, 5)) -> 450,
        (600, (2022, 4, 6)) -> 420,
        (600, (2022, 9, 8)) -> 420,
        (600, (2023, 4, 5)) -> 420,
        (600, (2023, 4, 6)) -> 390,
        (600, (2023, 9, 8)) -> 390,
        (600, (2024, 4, 5)) -> 390,
        (600, (2024, 4, 6)) -> 360,
        (600, (2024, 9, 8)) -> 360,
        (600, (2025, 4, 5)) -> 360,
        (600, (2025, 4, 6)) -> 330,
        (600, (2025, 9, 8)) -> 330,
        (600, (2026, 4, 5)) -> 330,
        (600, (2026, 4, 6)) -> 300,
        (600, (2026, 9, 8)) -> 300,
        (600, (2027, 4, 5)) -> 300
      ).foreach { case ((enteredAmount, (year, month, day)), adjustedEnteredAmount) =>
        s"provided with £$enteredAmount starting $year-$month-$day" in {
          controller.calculateAdjustedEnteredAmount(
            enteredAmount,
            LocalDate.of(year, month, day)
          ) shouldBe adjustedEnteredAmount
        }
      }
  }

}
