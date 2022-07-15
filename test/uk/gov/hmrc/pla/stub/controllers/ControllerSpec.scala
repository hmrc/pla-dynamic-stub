/*
 * Copyright 2022 HM Revenue & Customs
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

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json._
import play.api.mvc.Results.Ok
import play.api.mvc.{ControllerComponents, PlayBodyParsers}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.pla.stub.actions.ExceptionTriggersActions
import uk.gov.hmrc.pla.stub.model._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService

import scala.concurrent.{ExecutionContext, Future}

object TestData {

  val validFP2016CeateRequest = CreateLTAProtectionRequest(
    nino = Generator.randomNino,
    protection = ProtectionApplicationTestData.Fp2016
  )
  val authHeader = "Authorization" -> "Bearer: abcdef12345678901234567890"
  val envHeader = "Environment" -> "IST0"

  val validHeadersExample = Map(authHeader, envHeader)
  val noAuthHeadersExample = Map(envHeader)
  val noEnvHeadersExample = Map(authHeader)
  val emptyHeadersExample = Map[String, String]()
  val invalidRequestJsError = JsError("invalid request body")

  val validResponse = "\"pensionSchemeAdministratorCheckReference\":\"PSA12345678A\",\"ltaType\":5,\"psaCheckResult\":1,\"protectedAmount\":25000"
  val notFoundResponse = "\"reason\":\"Resource not found\""
  val notFoundProtectionsForNinoResponse = "\"no protections found for nino\""
}
class PLAStubControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with GuiceOneServerPerSuite {

  def Action = cc.actionBuilder

  implicit val ec = ExecutionContext.global
  implicit lazy val cc = app.injector.instanceOf[ControllerComponents]
  implicit lazy val playBodyParsers = app.injector.instanceOf[PlayBodyParsers]
  implicit lazy val exceptionTriggersActions = app.injector.instanceOf[ExceptionTriggersActions]
  implicit lazy val protectionService = mock[PLAProtectionService]
  val mockController: PLAStubController = mock[PLAStubController]

  "Read Protections" when {
    "return Status: OK Body: Protections for given nino on retrieval protections request" in {
      val nino = "RC966967C"
      val protections = Json.fromJson[Protections](successfulProtectionsRetrieveOutput)
      when(mockController.readProtections(nino)).thenReturn(Action {
        Ok(Json.toJson(protections.get))
      })

      val response = mockController.readProtections(nino).apply(FakeRequest("GET", s"/individual/$nino/protections/"))
      status(response) shouldBe OK
      contentAsJson(response).shouldBe(successfulProtectionsRetrieveOutput)

    }

    "return Status: OK Body: List of Empty Protections on retrieval of empty list" in {
      val nino = "AA000000A"
      val protections = Json.fromJson[Protections](successfulEmptyProtectionsRetrieveOutput)
      when(mockController.readProtections(nino)).thenReturn(Action {
        Ok(Json.toJson(protections.get))
      })

      val response = mockController.readProtections(nino).apply(FakeRequest("GET", s"/individual/$nino/protections/"))
      status(response) shouldBe OK
      contentAsJson(response).shouldBe(successfulEmptyProtectionsRetrieveOutput)

    }

  }
  "Read Protection" when {
    "return Status: OK Body: Protection for given nino and protection id on retrieval protection request" in {
      val nino = "RC966967C"
      val protectionId=1
      val protection = Json.fromJson[Protection](successfulProtectionRetrieveOutput)
      when(mockController.readProtection(nino, protectionId)).thenReturn(Action {
        Ok(Json.toJson(protection.get))
      })

      val response = mockController.readProtection(nino, 1).apply(FakeRequest("GET", s"/individual/$nino/protections/$protectionId"))
      status(response) shouldBe OK
      contentAsJson(response).shouldBe(successfulProtectionRetrieveOutput)

    }
  }

  "Read Protection Version" when {
    "return Status: OK Body: Protection for given nino , protection id and version on retrieval protection request" in {
      val nino = "RC966967C"
      val protectionId=1
      val versionId=1
      val protection = Json.fromJson[Protection](successfulProtectionRetrieveOutput)
      when(mockController.readProtectionVersion(nino, protectionId,versionId)).thenReturn(Action {
        Ok(Json.toJson(protection.get))
      })
      val response = mockController.readProtectionVersion(nino, protectionId,versionId)
        .apply(FakeRequest("GET", s"/individual/$nino/protections/$protectionId/version/$versionId"))
      status(response) shouldBe OK
      contentAsJson(response).shouldBe(successfulProtectionRetrieveOutput)

    }
  }
  "Create Protection" when {
    "return Status: OK Body: CreateLTAProtectionResponse for successful valid CreateLTAProtectionRequest with all optional data" in {
      val nino = "RC966967C"
      when(mockController.createProtection(nino)).thenReturn(Action.async(playBodyParsers.json) {
        _ =>
          Future.successful(Ok(validCreateProtectionResponseOutput))
      })
      val response = mockController.createProtection(nino).apply(FakeRequest("POST", s"/individual/$nino/protection/")
        .withBody(validCreateProtectionRequestInput))
      status(response) shouldBe OK
      contentAsJson(response).shouldBe(validCreateProtectionResponseOutput)

    }
  }

  "Update Protection" when {
    "return Status: OK Body: UpdateLTAProtectionResponse for successful valid UpdateLTAProtectionRequest with all optional data" in {
      val nino = "RC966967C"
      val protectionId=5
      when(mockController.updateProtection(nino,protectionId)).thenReturn(Action.async(playBodyParsers.json) {
        _ =>
          Future.successful(Ok(validUpdateProtectionResponseOutput))
      })
      val response = mockController.updateProtection(nino,protectionId).apply(FakeRequest("POST", s"/individual/$nino/protections/$protectionId")
        .withBody(validUpdateProtectionRequestInput))
      status(response) shouldBe OK
      contentAsJson(response).shouldBe(validUpdateProtectionResponseOutput)

    }
  }

  "PSA Lookup" when {
    val controller = new PLAStubController(cc, protectionService, ec, playBodyParsers, exceptionTriggersActions)
    "return a 403 Forbidden with empty body when provided no environment header" in {
      val result = controller.updatedPSALookup("PSA12345678A", "IP141000000000A").apply(FakeRequest())
      status(result) shouldBe FORBIDDEN
      contentAsString(result) shouldBe ""
    }

    "return a 401 Unauthorised with body when provided no auth header" in {
      val result = controller.updatedPSALookup("PSA12345678A", "IP141000000000A").apply(FakeRequest().withHeaders(TestData.envHeader))
      status(result) shouldBe UNAUTHORIZED
      contentAsString(result) should include("Required OAuth credentials not provided")
    }

    "return a 400 BadRequest with body when provided invalid psa and lta references" in {
      val result = controller.updatedPSALookup("", "").apply(FakeRequest().withHeaders(TestData.envHeader, TestData.authHeader))
      val error = "Your submission contains one or more errors. Failed Parameter(s) - [pensionSchemeAdministratorCheckReference, lifetimeAllowanceReference]"
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(error)
    }

    "return a 400 BadRequest with body when provided invalid psaReference" in {
      val result = controller.updatedPSALookup("", "IP141000000000A").apply(FakeRequest().withHeaders(TestData.envHeader, TestData.authHeader))
      val error = "Your submission contains one or more errors. Failed Parameter(s) - [pensionSchemeAdministratorCheckReference]"
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(error)
    }

    "return a 400 BadRequest with body when provided invalid ltaReference" in {
      val result = controller.updatedPSALookup("PSA12345678A", "").apply(FakeRequest().withHeaders(TestData.envHeader, TestData.authHeader))
      val error = "Your submission contains one or more errors. Failed Parameter(s) - [lifetimeAllowanceReference]"
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(error)
    }

    "return a 404 with body when provided psa reference ending in Z" in {
      val result = controller.updatedPSALookup("PSA12345678Z", "IP141000000000A").apply(FakeRequest().withHeaders(TestData.envHeader, TestData.authHeader))
      status(result) shouldBe NOT_FOUND
      contentAsString(result) should include(TestData.notFoundResponse)
    }

    "return a 404 with body when provided lta reference ending in Z" in {
      val result = controller.updatedPSALookup("PSA12345678A", "IP141000000000Z").apply(FakeRequest().withHeaders(TestData.envHeader, TestData.authHeader))
      status(result) shouldBe NOT_FOUND
      contentAsString(result) should include(TestData.notFoundResponse)
    }

    "return a 200 with body when provided valid references" in {
      val result = controller.updatedPSALookup("PSA12345678A", "IP141000000000A").apply(FakeRequest().withHeaders(TestData.envHeader, TestData.authHeader))
      status(result) shouldBe OK
      contentAsString(result) should include(TestData.validResponse)
    }
  }
}
