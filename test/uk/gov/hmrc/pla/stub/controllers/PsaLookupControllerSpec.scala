/*
 * Copyright 2024 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext

class PsaLookupControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with GuiceOneServerPerSuite {

  def Action: ActionBuilder[Request, AnyContent] = cc.actionBuilder

  implicit val ec: ExecutionContext             = app.injector.instanceOf[ExecutionContext]
  implicit val cc: ControllerComponents         = app.injector.instanceOf[ControllerComponents]
  implicit val playBodyParsers: PlayBodyParsers = app.injector.instanceOf[PlayBodyParsers]

  val mockController: PsaLookupController = mock[PsaLookupController]

  val authHeader: (String, String) = "Authorization" -> "Bearer: abcdef12345678901234567890"
  val envHeader: (String, String)  = "Environment"   -> "IST0"

  val validResponse =
    "\"pensionSchemeAdministratorCheckReference\":\"PSA12345678A\",\"ltaType\":5,\"psaCheckResult\":1,\"protectedAmount\":25000"

  val notFoundResponse = "\"reason\":\"Resource not found\""

  "PSA Lookup" when {
    val controller = new PsaLookupController(cc, ec, playBodyParsers)
    "return a 403 Forbidden with empty body when provided no environment header" in {
      val result = controller.updatedPSALookup("PSA12345678A", "IP141000000000A").apply(FakeRequest())
      status(result) shouldBe FORBIDDEN
      contentAsString(result) shouldBe ""
    }

    "return a 401 Unauthorised with body when provided no auth header" in {
      val result = controller
        .updatedPSALookup("PSA12345678A", "IP141000000000A")
        .apply(FakeRequest().withHeaders(envHeader))
      status(result) shouldBe UNAUTHORIZED
      contentAsString(result) should include("Required OAuth credentials not provided")
    }

    "return a 400 BadRequest with body when provided invalid psa and lta references" in {
      val result =
        controller.updatedPSALookup("", "").apply(FakeRequest().withHeaders(envHeader, authHeader))
      val error =
        "Your submission contains one or more errors. Failed Parameter(s) - [pensionSchemeAdministratorCheckReference, lifetimeAllowanceReference]"
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(error)
    }

    "return a 400 BadRequest with body when provided invalid psaReference" in {
      val result = controller
        .updatedPSALookup("", "IP141000000000A")
        .apply(FakeRequest().withHeaders(envHeader, authHeader))
      val error =
        "Your submission contains one or more errors. Failed Parameter(s) - [pensionSchemeAdministratorCheckReference]"
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(error)
    }

    "return a 400 BadRequest with body when provided invalid ltaReference" in {
      val result = controller
        .updatedPSALookup("PSA12345678A", "")
        .apply(FakeRequest().withHeaders(envHeader, authHeader))
      val error = "Your submission contains one or more errors. Failed Parameter(s) - [lifetimeAllowanceReference]"
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(error)
    }

    "return a 404 with body when provided psa reference ending in Z" in {
      val result = controller
        .updatedPSALookup("PSA12345678Z", "IP141000000000A")
        .apply(FakeRequest().withHeaders(envHeader, authHeader))
      status(result) shouldBe NOT_FOUND
      contentAsString(result) should include(notFoundResponse)
    }

    "return a 404 with body when provided lta reference ending in Z" in {
      val result = controller
        .updatedPSALookup("PSA12345678A", "IP141000000000Z")
        .apply(FakeRequest().withHeaders(envHeader, authHeader))
      status(result) shouldBe NOT_FOUND
      contentAsString(result) should include(notFoundResponse)
    }

    "return a 200 with body when provided valid references" in {
      val result = controller
        .updatedPSALookup("PSA12345678A", "IP141000000000A")
        .apply(FakeRequest().withHeaders(envHeader, authHeader))
      status(result) shouldBe OK
      contentAsString(result) should include(validResponse)
    }
  }

}
