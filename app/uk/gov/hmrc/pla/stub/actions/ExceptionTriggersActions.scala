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

package uk.gov.hmrc.pla.stub.actions


import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.pla.stub.model.ExceptionTrigger
import uk.gov.hmrc.pla.stub.repository.MongoExceptionTriggerRepository

import scala.concurrent.{ExecutionContext, Future}

class ExceptionTriggersActions @Inject()(implicit reactiveMongoComponent: ReactiveMongoComponent) {

  lazy val exceptionTriggerRepository = new MongoExceptionTriggerRepository

  val noNotificationIdJson = Json.parse(
    s"""
       |  {
       |      "nino": "AA055121",
       |      "pensionSchemeAdministratorCheckReference" : "PSA123456789",
       |      "protection": {
       |        "id": 1234567,
       |        "version": 1,
       |        "type": 1,
       |        "certificateDate": "2015-05-22",
       |        "certificateTime": "12:22:59",
       |        "status": 1,
       |        "protectionReference": "IP161234567890C",
       |        "relevantAmount": 1250000.00
       |      }
       |    }
       |
    """.stripMargin).as[JsObject]
}

case class WithExceptionTriggerCheckAction(nino: String)
                                          (implicit ec: ExecutionContext, cc: ControllerComponents,
                                           exceptionTriggersActions: ExceptionTriggersActions)
  extends ActionBuilder[Request, AnyContent] {

  override val parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
  override protected val executionContext: ExecutionContext = cc.executionContext

  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    exceptionTriggersActions.exceptionTriggerRepository.findExceptionTriggerByNino(nino).flatMap {
      case Some(trigger) => processExceptionTrigger(trigger)
      case None => block(request)
    }
  }

  /**
   * When passed an exception trigger, either returns the corresponding error response or throws the correct exception/timeout
   *
   * @param trigger : ExceptionTrigger
   * @return
   */
  private def processExceptionTrigger(trigger: ExceptionTrigger): Future[Result] = {
    import ExceptionTrigger.ExceptionType
    trigger.extractedExceptionType match {
      case ExceptionType.BadRequest => Future.successful(Results.BadRequest("Simulated bad request"))
      case ExceptionType.NotFound => Future.successful(Results.NotFound("Simulated not found"))
      case ExceptionType.InternalServerError => Future.successful(Results.InternalServerError("Simulated 500 error"))
      case ExceptionType.BadGateway => Future.successful(Results.BadGateway("Simulated 502 error"))
      case ExceptionType.ServiceUnavailable => Future.successful(Results.ServiceUnavailable("Simulated 503 error"))
      case ExceptionType.UncaughtException => throw new Exception()
      case ExceptionType.Timeout => Thread.sleep(60000); Future.successful(Results.Ok)
      case ExceptionType.NoNotificationId => Future.successful(Results.Ok(exceptionTriggersActions.noNotificationIdJson))
    }
  }
}
