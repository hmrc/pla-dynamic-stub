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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsObject, __}
import play.api.mvc.Results.Ok
import play.api.mvc.{MessagesControllerComponents, PlayBodyParsers}
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.pla.stub.model.hip.AmendProtectionLifetimeAllowanceType._
import uk.gov.hmrc.pla.stub.model.hip.HipNotification._
import uk.gov.hmrc.pla.stub.model.hip._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import java.util.Random
import scala.concurrent.{ExecutionContext, Future}

class HipAmendProtectionControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with Injecting {

  private val mockPLAProtectionService: PLAProtectionService = mock[PLAProtectionService]

  private val nowInstant: Instant = Instant.parse("2025-08-15T12:34:56Z")
  private def fixedClock: Clock   = Clock.fixed(nowInstant, ZoneOffset.UTC)

  private lazy val controller: HipAmendProtectionController = new HipAmendProtectionController(
    inject[MessagesControllerComponents],
    mockPLAProtectionService,
    inject[PlayBodyParsers]
  )(inject[ExecutionContext], fixedClock)

  val rand: Random             = new Random()
  val ninoGenerator: Generator = new Generator(rand)
  def randomNino: String       = ninoGenerator.nextNino.nino.replaceFirst("MA", "AA")

  override def beforeEach(): Unit = {
    reset(mockPLAProtectionService)
    super.beforeEach()
  }

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

          val protection = HipProtection(
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
            certificateDate = Some("2025-08-15"),
            certificateTime = Some("123456"),
            protectionReference = Some(protectionReference),
            pensionDebitAmount = Some(25_000),
            pensionDebitEnteredAmount = Some(25_000),
            protectedAmount = Some(120_000),
            pensionDebitStartDate = Some("2026-07-09"),
            pensionDebitTotalAmount = Some(40_000)
          )

          val ninoWithoutSuffix = nino.dropRight(1)

          when(mockPLAProtectionService.findHipProtectionByNinoAndId(eqTo(ninoWithoutSuffix), eqTo(protectionId)))
            .thenReturn(Future.successful(Some(protection)))

          when(mockPLAProtectionService.findAllHipProtectionsByNino(eqTo(ninoWithoutSuffix)))
            .thenReturn(Future.successful(List(protection)))

          when(mockPLAProtectionService.insertOrUpdateHipProtection(any())).thenReturn(Future.successful(Ok))

          val result = controller
            .amendProtection(nino, protectionId, 1)
            .apply(
              FakeRequest(
                "POST",
                s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
              ).withBody(validAmendProtectionRequestInputWith(protectionType = protectionType))
            )

          status(result) shouldBe OK

          val resultBody = contentAsJson(result).asInstanceOf[JsObject]

          resultBody.shouldBe(
            validHipAmendProtectionResponseWith(
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

      "provided with negative relevant amount" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "relevant amount must be positive"

        val requestBody = validAmendProtectionRequestInputWith(relevantAmount = -1, nonUKRightsAmount = -39501)

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }

      "provided with a negative non UK rights amount" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "non UK rights amount must be positive"

        val requestBody = validAmendProtectionRequestInputWith(relevantAmount = 39499, nonUKRightsAmount = -1)

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }

      "provided with a negative post A day benefit crystallisation event amount" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "post A day benefit crystallisation event amount must be positive"

        val requestBody =
          validAmendProtectionRequestInputWith(relevantAmount = 36999, postADayBenefitCrystallisationEventAmount = -1)

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }

      "provided with a negative pre A day pension in payment amount" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "pre A day pension in payment amount must be positive"

        val requestBody =
          validAmendProtectionRequestInputWith(relevantAmount = 37999, preADayPensionInPaymentAmount = -1)

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }

      "provided with a negative uncrystallised rights amount" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "uncrystallised rights amount must be positive"

        val requestBody =
          validAmendProtectionRequestInputWith(uncrystallisedRightsAmount = -1, nonUKRightsAmount = 75501)

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }

      "provided with a negative pension debit entered amount" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "pension debit entered amount must be positive"

        val requestBody = validAmendProtectionRequestInputWith(pensionDebitEnteredAmount = -1)

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }

      "provided with an invalid certificate date" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "invalid certificate date"

        val requestBody = validAmendProtectionRequestInputWith(certificateDate = "22/10/2025")

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }

      "provided with an invalid certificate time" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "invalid certificate time"

        val requestBody = validAmendProtectionRequestInputWith(certificateTime = "15:55:49")

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }

      "provided with an invalid pension debit start date" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error        = "invalid pension debit start date"

        val requestBody = validAmendProtectionRequestInputWith(pensionDebitStartDate = "22/10/2025")

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include(error)
      }
    }

    "return 404 with no matching protection in db" in {
      val nino              = randomNino
      val protectionId      = 12960000000123L
      val sequence          = 1
      val error             = "protection to amend not found"
      val ninoWithoutSuffix = nino.dropRight(1)

      when(mockPLAProtectionService.findHipProtectionByNinoAndId(eqTo(ninoWithoutSuffix), eqTo(protectionId)))
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

    "return 422 for unprocessable entity" when {
      "provided relevant amount does not equal calculated relevant amount" in {
        val nino           = randomNino
        val protectionId   = 12960000000123L
        val sequence       = 1
        val relevantAmount = 100101
        val error = s"The specified Relevant Amount $relevantAmount is not the sum of the specified breakdown amounts"

        val requestBody = validAmendProtectionRequestInputWith(relevantAmount = relevantAmount)

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe UNPROCESSABLE_ENTITY

        contentAsString(result) should include(error)
      }

      "provided with partial pension debits information - missing start date" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error =
          "incomplete pension debits information - require either both, or neither of pension debit start date and pension debit entered amount"

        val requestBody =
          validAmendProtectionRequestInput
            .transform((__ \ "lifetimeAllowanceProtectionRecord" \ "pensionDebitStartDate").json.prune)
            .get

        println(requestBody)

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe UNPROCESSABLE_ENTITY

        contentAsString(result) should include(error)
      }

      "provided with partial pension debits information - missing entered amount" in {
        val nino         = randomNino
        val protectionId = 12960000000123L
        val sequence     = 1
        val error =
          "incomplete pension debits information - require either both, or neither of pension debit start date and pension debit entered amount"

        val requestBody =
          validAmendProtectionRequestInput
            .transform((__ \ "lifetimeAllowanceProtectionRecord" \ "pensionDebitEnteredAmount").json.prune)
            .get

        println(requestBody)

        val result = controller
          .amendProtection(nino, protectionId, 1)
          .apply(
            FakeRequest(
              "POST",
              s"/paye/lifetime-allowance/person/$nino/reference/$protectionId/sequence-number/$sequence"
            ).withBody(requestBody)
          )

        status(result) shouldBe UNPROCESSABLE_ENTITY

        contentAsString(result) should include(error)
      }
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
        HipNotification7,
        HipNotification14
      ).foreach { notification =>
        s"provided with notification ID ${notification.id}" in {
          controller.opensDormantFixedProtection2016(notification) shouldBe true
        }
      }

    "return false" when
      Seq(
        HipNotification1,
        HipNotification2,
        HipNotification3,
        HipNotification4,
        HipNotification5,
        HipNotification6,
        HipNotification8,
        HipNotification9,
        HipNotification10,
        HipNotification11,
        HipNotification12,
        HipNotification13
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
