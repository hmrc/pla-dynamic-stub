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

import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, _}
import uk.gov.hmrc.pla.stub.Generator
import uk.gov.hmrc.pla.stub.model._
import uk.gov.hmrc.pla.stub.model.hip.ProtectionStatus.Open
import uk.gov.hmrc.smartstub.Enumerable.instances.ninoEnumNoSpaces
import uk.gov.hmrc.smartstub.{Generator => _, _}

import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class PsaLookupController @Inject() (
    val mcc: ControllerComponents,
    implicit val ec: ExecutionContext,
    playBodyParsers: PlayBodyParsers
) extends BackendController(mcc)
    with Logging {

  def psaLookupNew(ref: String, psaref: String): Action[JsValue] = Action(playBodyParsers.json) { _ =>
    val c1          = psaref.substring(3, 4).toShort.toChar
    val c2          = psaref.substring(5, 6).toShort.toChar
    val ninoNumbers = psaref.substring(7, 12)
    val nino        = s"$c1$c2$ninoNumbers"
    val protections = Generator.genProtections(nino).seeded(nino).get.protections
    val result      = protections.find(p => p.protectionReference.contains(ref))
    result match {
      case Some(protection) if protection.status == Open =>
        Ok(
          Json.toJson(
            PSALookupResult(protection.`type`.toPlaId, validResult = true, Some(protection.relevantAmount.toDouble))
          )
        )
      case _ => Ok(Json.toJson(PSALookupResult(0, validResult = false, None)))
    }
  }

  def updatedPSALookup(psaRef: String, ltaRef: String): Action[AnyContent] = Action.async { implicit request =>
    val validationResult = (psaRef.matches("^PSA[0-9]{8}[A-Z]$"), ltaRefValidator(ltaRef))
    val environment      = request.headers.get("Environment")
    val auth             = request.headers.get("Authorization")
    if (environment.isEmpty) {
      logger.error("Request is missing environment header")
      Future.successful(Forbidden)
    } else if (auth.isEmpty) {
      logger.error("Request is missing auth header")
      Future.successful(Unauthorized(Json.toJson(PSALookupErrorResult("Required OAuth credentials not provided"))))
    } else if (!validationResult._1 | !validationResult._2) {
      val refValidationResponse = validationResult match {
        case (false, false) => "pensionSchemeAdministratorCheckReference, lifetimeAllowanceReference"
        case (false, true)  => "pensionSchemeAdministratorCheckReference"
        case (true, false)  => "lifetimeAllowanceReference"
        case _              =>
      }
      val errorMsg = s"Your submission contains one or more errors. Failed Parameter(s) - [$refValidationResponse]"
      logger.error(errorMsg)
      val response = PSALookupErrorResult(errorMsg)
      Future.successful(BadRequest(Json.toJson(response)))
    } else {
      logger.info("Successful request submitted")
      returnPSACheckResult(psaRef, ltaRef)
    }
  }

  private def returnPSACheckResult(psaRef: String, ltaRef: String): Future[Result] =
    (psaRef, ltaRef) match {
      case ("PSA12345670C", "FP161000000000A") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 1, 1, Some(BigDecimal.exact("39495.88"))))))
      case ("PSA12345670A", "IP141000000001A") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 2, 1, Some(BigDecimal.exact("100000.00"))))))
      case ("PSA12345670B", "IP161000000002A") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 3, 1, Some(BigDecimal.exact("1000000.00"))))))
      case ("PSA12345670D", "A234551A") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 2, 1, Some(BigDecimal.exact("39495.88"))))))
      case ("PSA12345670E", "A234552B") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 4, 1, Some(BigDecimal.exact("39495.88"))))))
      case ("PSA12345670F", "A234553B") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 5, 1, Some(BigDecimal.exact("39495.88"))))))
      case ("PSA12345670G", "A234554B") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 6, 1, Some(BigDecimal.exact("39495.88"))))))
      case ("PSA12345670H", "A234555B") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 7, 1, Some(BigDecimal.exact("39495.88"))))))
      case ("PSA12345670I", "A234556B") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 7, 0, Some(BigDecimal.exact("39495.88"))))))
      case ("PSA12345670J", "IP141000000007A") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 2, 0, Some(BigDecimal.exact("39495.88"))))))
      case ("PSA12345678A", "IP141000000000A") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 5, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678K", "A123456K") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 8, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678L", "A123456L") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 9, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678M", "A123456M") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 10, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678N", "A123456N") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 11, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678O", "A123456O") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 12, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678P", "A123456P") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 13, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678Q", "A123456Q") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 14, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678R", "A123456R") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 15, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678S", "A123456S") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 16, 1, Some(BigDecimal.exact("25000"))))))
      case ("PSA12345678T", "A123456T") =>
        Future.successful(Ok(Json.toJson(PSALookupUpdatedResult(psaRef, 17, 1, Some(BigDecimal.exact("25000"))))))
      case _ =>
        val response = PSALookupErrorResult("Resource not found")
        logger.error(response.reason)
        Future.successful(NotFound(Json.toJson(response)))
    }

  private def ltaRefValidator(ltaRef: String): Boolean =
    ltaRef.matches("^(IP14|IP16|FP16)[0-9]{10}[ABCDEFGHJKLMNPRSTXYZ]$") | ltaRef.matches(
      "^[1-9A][0-9]{6}[ABCDEFHXJKLMNYPQRSTZW]$"
    )

}
