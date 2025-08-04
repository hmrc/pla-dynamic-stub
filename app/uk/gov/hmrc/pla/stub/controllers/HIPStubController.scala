package uk.gov.hmrc.pla.stub.controllers

import java.time.LocalDateTime
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, _}
import uk.gov.hmrc.pla.stub.Generator
import uk.gov.hmrc.pla.stub.model._
import uk.gov.hmrc.pla.stub.notifications.{CertificateStatus, Notifications}
import uk.gov.hmrc.pla.stub.rules._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService
import uk.gov.hmrc.smartstub.Enumerable.instances.ninoEnumNoSpaces
import uk.gov.hmrc.smartstub.{Generator => _, _}

import javax.inject.Inject
import uk.gov.hmrc.pla.stub.actions.{ExceptionTriggersActions, WithExceptionTriggerCheckAction}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class HIPStubController @Inject() (
  mcc: ControllerComponents,
) extends BackendController(mcc) with Logging {

}
