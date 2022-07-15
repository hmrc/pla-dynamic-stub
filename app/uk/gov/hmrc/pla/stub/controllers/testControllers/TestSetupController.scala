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

package uk.gov.hmrc.pla.stub.controllers.testControllers

import uk.gov.hmrc.pla.stub.repository.MongoExceptionTriggerRepository
import uk.gov.hmrc.pla.stub.model.{Error, ExceptionTrigger, Protection}
import play.api.mvc._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService

import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class TestSetupController @Inject()(val mcc: play.api.mvc.MessagesControllerComponents,
                                    val protectionService: PLAProtectionService,
                                    implicit val ec: ExecutionContext,
                                    playBodyParsers: PlayBodyParsers,
                                    implicit val reactiveMongoComponent: ReactiveMongoComponent) extends BackendController(mcc) {

  val exceptionTriggerRepository = new MongoExceptionTriggerRepository()

  /**
   * Stub-only convenience operation to add a protection to test data
   *
   * @return
   */
  def insertProtection(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    val protectionJs = request.body.validate[Protection]
    protectionJs.fold(
      errors => Future.successful(BadRequest(Json.toJson(Error(message = "body failed validation with errors: " + errors)))),
      protection =>
        protectionService.insertOrUpdateProtection(protection)
          .map { _ => Ok }(ec)
          .recover { case exception => Results.InternalServerError(exception.toString) }
    )
  }

  /**
   * Stub-only convenience operation to tear down test data
   *
   * @return
   */
  def removeAllProtections(): Action[AnyContent] = Action.async { _ =>
    protectionService.protectionsStore.removeProtectionsCollection()
    Future.successful(Ok)
  }

  /**
   * Stub-only convenience operation to tear down test data for a given NINO
   *
   * @param nino
   * @return
   */
  def removeProtections(nino: String): Action[AnyContent] = Action.async { _ =>
    protectionService.protectionsStore.removeByNino(nino)
    Future.successful(Ok)
  }

  /**
   * Stub-only convenience operation to tear down test data for a specified protection
   *
   * @param nino
   * @param protectionId
   * @return
   */
  def removeProtection(nino: String, protectionId: Long): Action[AnyContent] = Action.async { _ =>
    protectionService.removeProtectionByNinoAndProtectionId(nino, protectionId)
    Future.successful(Ok)
  }

  /**
   * Stub-only convenience operation to tear down test data for a specified protection
   *
   * @return
   */
  def dropProtectionsCollection(): Action[AnyContent] = Action.async { _ =>
    protectionService.protectionsStore.removeProtectionsCollection()
    Future.successful(Ok)
  }


  /**
   * Stub-only convenience operation to remove all exception triggers from the database
   *
   * @return
   */
  def removeExceptionTriggers(): Action[AnyContent] = Action.async {_ =>
    exceptionTriggerRepository.removeAllExceptionTriggers()(ec)
    Future.successful(Ok)
  }

  /**
   * Stub-only convenience operation to add an exception trigger for a particular nino
   *
   * @return
   */
  def insertExceptionTrigger(): Action[JsValue] = Action.async(playBodyParsers.json) { implicit request =>
    val exceptionTriggerJs = request.body.validate[ExceptionTrigger]
    exceptionTriggerJs.fold(
      errors => Future.successful(BadRequest(Json.toJson(Error(message = "body failed validation with errors: " + errors)))),
      exceptionTrigger =>
        exceptionTriggerRepository.insert(exceptionTrigger)
          .map { _ => Ok }(ec)
          .recover { case exception => Results.InternalServerError(exception.toString) }
    )
  }


}
