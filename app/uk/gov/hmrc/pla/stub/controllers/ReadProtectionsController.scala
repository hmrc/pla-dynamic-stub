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

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.pla.stub.model.Protections
import uk.gov.hmrc.pla.stub.model.hip.*
import uk.gov.hmrc.pla.stub.services.ProtectionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.smartstub.Generator as _

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReadProtectionsController @Inject() (
    controllerComponents: ControllerComponents,
    protectionService: ProtectionService
)(using ExecutionContext)
    extends BackendController(controllerComponents)
    with Logging {

  def readProtections(nino: String): Action[AnyContent] = Action.async {
    protectionService.retrieveConvertedProtections(nino).map {
      case Some(protections) =>
        Ok(Json.toJson(protections))
      case None =>
        logger.info("No protections set for given Nino, returning empty protections list")
        Ok(
          Json.toJson(
            ReadProtectionsResponse(
              Protections(
                nino = nino,
                pensionSchemeAdministratorCheckReference = Some("stubPSACheckRef"),
                protections = List.empty
              )
            )
          )
        )
    }
  }

}
