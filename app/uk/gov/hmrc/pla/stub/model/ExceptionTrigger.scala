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

package uk.gov.hmrc.pla.stub.model

import play.api.libs.json.{Json, Format}


case class ExceptionTrigger(nino: String, exceptionType: String) {

  import ExceptionTrigger.ExceptionType._
  def extractedExceptionType = exceptionType match {
    case "400" => BadRequest
    case "404" => NotFound
    case "500" => InternalServerError
    case "502" => BadGateway
    case "503" => ServiceUnavailable
    case "uncaught" => UncaughtException
    case "timeout" => Timeout
    case "noid" => NoNotificationId
  }
}

object ExceptionTrigger {

  implicit lazy val exceptionTriggerFormat: Format[ExceptionTrigger] = Json.format[ExceptionTrigger]


  object ExceptionType extends Enumeration {
    val BadRequest, NotFound, InternalServerError, BadGateway, ServiceUnavailable, UncaughtException, Timeout, NoNotificationId = Value
  }
}
