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

import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
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

class HipReadProtectionsControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with Injecting {

  private val mockPLAProtectionService: PLAProtectionService = mock[PLAProtectionService]

  private lazy val controller: HipReadProtectionsController = new HipReadProtectionsController(
    inject[MessagesControllerComponents],
    mockPLAProtectionService
  )(inject[ExecutionContext])

  val rand: Random             = new Random()
  val ninoGenerator: Generator = new Generator(rand)
  def randomNino: String       = ninoGenerator.nextNino.nino.replaceFirst("MA", "AA")

  override def beforeEach(): Unit = {
    reset(mockPLAProtectionService)
    super.beforeEach()
  }

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

}
