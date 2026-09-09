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

package uk.gov.hmrc.pla.stub.controllers.testControllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.pla.stub.model.{Error, Protections}
import uk.gov.hmrc.pla.stub.services.ProtectionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class TestSetupController @Inject() (
    controllerComponents: play.api.mvc.MessagesControllerComponents,
    protectionService: ProtectionService
)(using ExecutionContext)
    extends BackendController(controllerComponents) {

  def insertProtection(): Action[JsValue] = Action.async(controllerComponents.parsers.json) { request =>
    val protectionJs = request.body.validate[Protections]
    protectionJs.fold(
      errors =>
        Future.successful(BadRequest(Json.toJson(Error(message = "body failed validation with errors: " + errors)))),
      protections =>
        protectionService
          .saveProtections(protections)
          .map(_ => Ok)
          .recover { case exception => InternalServerError(exception.toString) }
    )
  }

  def removeAllProtections(): Action[AnyContent] =
    Action.async(protectionService.protectionsStore.removeProtectionsCollection().transform(_ => Success(Ok)))

  def removeProtections(nino: String): Action[AnyContent] =
    Action.async(protectionService.protectionsStore.removeByNino(nino).transform(_ => Success(Ok)))

  def removeProtection(nino: String, protectionId: Long): Action[AnyContent] = Action.async {
    protectionService.removeProtectionByNinoAndProtectionId(nino, protectionId).transform(_ => Success(Ok))
  }

  def dropProtectionsCollection(): Action[AnyContent] =
    Action.async(protectionService.protectionsStore.removeProtectionsCollection().transform(_ => Success(Ok)))

}
