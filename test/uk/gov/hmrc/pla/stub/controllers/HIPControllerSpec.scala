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
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results.Ok
import play.api.mvc.{MessagesControllerComponents, PlayBodyParsers}
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.pla.stub.model.Protections
import uk.gov.hmrc.pla.stub.model.hip.ProtectionStatus.Open
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType.IndividualProtection2016
import uk.gov.hmrc.pla.stub.model.hip._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService

import java.util.Random
import scala.concurrent.{ExecutionContext, Future}

class HIPControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with Injecting {

  val mockPLAProtectionService: PLAProtectionService = mock[PLAProtectionService]

  lazy val controller: HIPController = new HIPController(
    inject[MessagesControllerComponents],
    mockPLAProtectionService,
    inject[PlayBodyParsers],
    inject[ExecutionContext]
  )

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

          when(mockPLAProtectionService.retrieveHIPProtections(eqTo(nino)))
            .thenReturn(Future.successful(Some(protections)))

          val result = controller.readProtections(nino)(FakeRequest())

          status(result) shouldBe OK

          contentAsJson(result) shouldBe Json.toJson(protections)
        }

        "the given nino has no protections against it" in {

          val nino = randomNino

          when(mockPLAProtectionService.retrieveHIPProtections(eqTo(nino)))
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
        val nino         = randomNino
        val protectionId = 1
        val sequence     = 1

        val protection = HipProtection(
          nino = nino,
          id = protectionId,
          sequence = sequence,
          status = ProtectionStatus.Open,
          `type` = ProtectionType.IndividualProtection2014,
          relevantAmount = 1_254_000,
          preADayPensionInPaymentAmount = 0,
          postADayBenefitCrystallisationEventAmount = 0,
          uncrystallisedRightsAmount = 0,
          nonUKRightsAmount = 0,
          certificateDate = None,
          certificateTime = None,
          protectionReference = None,
          pensionDebitAmount = None,
          pensionDebitEnteredAmount = None,
          protectedAmount = None,
          pensionDebitStartDate = None,
          pensionDebitTotalAmount = None
        )

        when(mockPLAProtectionService.findHipProtectionByNinoAndId(eqTo(nino), eqTo(protectionId)))
          .thenReturn(Future.successful(Some(protection)))

        when(mockPLAProtectionService.findAllProtectionsByNino(eqTo(nino)))
          .thenReturn(Future.successful(Some(List(protection.toProtection))))

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

        val resultBody = contentAsJson(result).asInstanceOf[JsObject] - "certificateDate" - "certificateTime"
        resultBody.shouldBe(validAmendProtectionResponseOutput)
      }
    }
  }

}
