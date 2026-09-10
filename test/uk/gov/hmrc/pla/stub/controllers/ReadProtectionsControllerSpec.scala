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

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status, stubControllerComponents}
import uk.gov.hmrc.domain.NinoGenerator
import uk.gov.hmrc.pla.stub.model.hip.*
import uk.gov.hmrc.pla.stub.model.hip.ProtectionStatus.Open
import uk.gov.hmrc.pla.stub.model.hip.ProtectionType.IndividualProtection2016
import uk.gov.hmrc.pla.stub.model.{DateModel, Protections, TimeModel}
import uk.gov.hmrc.pla.stub.services.ProtectionService

import scala.concurrent.{ExecutionContext, Future}

class ReadProtectionsControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val controllerComponents                        = stubControllerComponents()
  private val mockPLAProtectionService: ProtectionService = mock[ProtectionService]

  private val executionContext: ExecutionContext = ExecutionContext.global

  private val controller: ReadProtectionsController = new ReadProtectionsController(
    controllerComponents,
    mockPLAProtectionService
  )(using executionContext)

  val ninoGenerator: NinoGenerator = new NinoGenerator()
  def randomNino: String           = ninoGenerator.nextNino.nino.replaceFirst("MA", "AA")

  override def beforeEach(): Unit = {
    reset(mockPLAProtectionService)
    super.beforeEach()
  }

  "readProtections is called" must {

    "return 200 with correct protections" when {

      "protections are present in the cache for the given nino" in {

        val protections: ReadProtectionsResponse = ReadProtectionsResponse(
          "1234567890",
          Seq(
            ProtectionRecordsList(
              ProtectionRecord(
                identifier = 1,
                sequenceNumber = 2,
                `type` = IndividualProtection2016,
                certificateDate = DateModel.of(2025, 1, 10),
                certificateTime = TimeModel.of(13, 54, 29),
                status = Open,
                protectionReference = None,
                relevantAmount = None,
                preADayPensionInPaymentAmount = None,
                postADayBenefitCrystallisationEventAmount = None,
                uncrystallisedRightsAmount = None,
                nonUKRightsAmount = None,
                pensionDebitAmount = None,
                pensionDebitEnteredAmount = None,
                protectedAmount = None,
                pensionDebitStartDate = None,
                pensionDebitTotalAmount = None,
                lumpSumAmount = None,
                lumpSumPercentage = None,
                enhancementFactor = None
              ),
              None
            )
          )
        )

        val nino = randomNino

        when(mockPLAProtectionService.retrieveConvertedProtections(eqTo(nino)))
          .thenReturn(Future.successful(Some(protections)))

        val result = controller.readProtections(nino)(FakeRequest())

        status(result) shouldBe OK

        contentAsJson(result) shouldBe Json.toJson(protections)
      }

      "the given nino has no protections against it" in {

        val nino = randomNino

        when(mockPLAProtectionService.retrieveConvertedProtections(eqTo(nino)))
          .thenReturn(Future.successful(None))

        val result = controller.readProtections(nino)(FakeRequest())

        status(result) shouldBe OK

        contentAsJson(result) shouldBe Json.toJson(
          ReadProtectionsResponse(Protections(nino, Some("stubPSACheckRef"), List.empty))
        )
      }
    }
  }

}
