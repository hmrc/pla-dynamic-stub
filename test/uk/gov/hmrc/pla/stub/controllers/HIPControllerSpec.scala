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
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results.Ok
import play.api.mvc.{MessagesControllerComponents, PlayBodyParsers}
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.pla.stub.model.Protections
import uk.gov.hmrc.pla.stub.model.hip.ProtectionStatus.Open
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType.IndividualProtection2016
import uk.gov.hmrc.pla.stub.model.hip._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService

import java.time.{Clock, Instant, ZoneOffset}
import java.util.Random
import scala.concurrent.{ExecutionContext, Future}

class HIPControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with Injecting {

  private val mockPLAProtectionService: PLAProtectionService = mock[PLAProtectionService]

  private val nowInstant: Instant = Instant.parse("2025-08-15T12:34:56Z")
  private def fixedClock: Clock   = Clock.fixed(nowInstant, ZoneOffset.UTC)

  private lazy val controller: HIPController = new HIPController(
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

  "HIPController" when {

    "readProtections is called" must {

      "return 200 with correct protections" when {

        "protections are present in the cache for the given nino" in {

          val protections: HIPProtectionsModel = HIPProtectionsModel(
            "1234567890",
            Seq(
              ProtectionRecordsList(
                ProtectionRecord(
                  1,
                  2,
                  IndividualProtection2016,
                  "2025-01-10",
                  "135429",
                  Open,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None
                ),
                None
              )
            )
          )

          val nino = randomNino

          val ninoWithoutSuffix = nino.dropRight(1)

          when(mockPLAProtectionService.retrieveHIPProtections(eqTo(ninoWithoutSuffix)))
            .thenReturn(Future.successful(Some(protections)))

          val result = controller.readProtections(nino)(FakeRequest())

          status(result) shouldBe OK

          contentAsJson(result) shouldBe Json.toJson(protections)
        }

        "the given nino has no protections against it" in {

          val nino = randomNino

          val ninoWithoutSuffix = nino.dropRight(1)

          when(mockPLAProtectionService.retrieveHIPProtections(eqTo(ninoWithoutSuffix)))
            .thenReturn(Future.successful(None))

          val result = controller.readProtections(nino)(FakeRequest())

          status(result) shouldBe OK

          contentAsJson(result) shouldBe Json.toJson(
            HIPProtectionsModel(Protections(nino, Some("stubPSACheckRef"), List.empty))
          )
        }
      }

    }

    "amendProtections is called" must {

      "return 200 with correct response body" in {
        val nino                = randomNino
        val protectionId        = 12960000000123L
        val sequence            = 1
        val protectionReference = "IP123456789012B"

        val protection = HipProtection(
          nino = nino,
          id = protectionId,
          sequence = sequence,
          status = ProtectionStatus.Open,
          `type` = ProtectionType.IndividualProtection2014,
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
            ).withBody(validAmendProtectionRequestInput)
          )

        status(result) shouldBe OK

        val resultBody = contentAsJson(result).asInstanceOf[JsObject]
        resultBody.shouldBe(validHipAmendProtectionResponse)
      }

      "return 400 with invalid request body" in {
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

    }
  }

}
