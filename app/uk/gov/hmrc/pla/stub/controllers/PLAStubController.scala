/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDateTime

import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action,AnyContent, _}
import uk.gov.hmrc.pla.stub.actions.ExceptionTriggersActions.WithExceptionTriggerCheckAction
import uk.gov.hmrc.pla.stub.model._
import uk.gov.hmrc.pla.stub.notifications.{CertificateStatus, Notifications}
import uk.gov.hmrc.pla.stub.repository.{ExceptionTriggerRepository, MongoExceptionTriggerRepository, MongoProtectionRepository, ProtectionRepository}
import uk.gov.hmrc.pla.stub.rules._
import uk.gov.hmrc.pla.stub.services.PLAProtectionService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random
import uk.gov.hmrc.smartstub.{Generator => _, _}
import uk.gov.hmrc.smartstub.Enumerable.instances.ninoEnumNoSpaces




/**
  * The controller for the Protect your Lifetime Allowance (PLA) service REST API dynamic stub
  */

object PLAStubController extends PLAStubController {
  override val protectionRepository = MongoProtectionRepository()
  override val exceptionTriggerRepository = MongoExceptionTriggerRepository()
}

trait PLAStubController  extends BaseController {

  val protectionRepository: ProtectionRepository
  val exceptionTriggerRepository: ExceptionTriggerRepository

  def readProtectionsNew(nino: String): Action[AnyContent] = Action { implicit request =>
    val result = PLAProtectionService.retrieveProtections(nino)
    result match {
      case Some(protection) => Ok(Json.toJson(protection))
      case None => NotFound(Json.toJson(Error("no protections found for nino")))
    }
  }

  def readProtectionNew(nino: String, protectionId: Long): Action[AnyContent] = Action { implicit request =>
    val protections: Option[Protections] = PLAProtectionService.retrieveProtections(nino)
    val protection: Option[Protection] = protections.get.protections.find(p => p.id == protectionId)
    protection match {
      case Some(protection) => Ok(Json.toJson(protection))
      case None => NotFound(Json.toJson(Error("no protection found for specified protection id")))
    }
  }

  def readProtectionVersionNew(nino: String, protectionId: Long, version: Int): Action[AnyContent] = Action { implicit request =>
    val protections: Option[Protections] = PLAProtectionService.retrieveProtections(nino)
    val protection: Option[Protection] = protections.get.protections.find(p => p.id == protectionId)
    protection match {
      case Some(protection) if protection.previousVersions.get.exists(p => p.version == version) =>
        Ok(Json.toJson(protection.previousVersions.get.find(p => p.version == version).get))
      case None => NotFound(Json.toJson(Error("no protection found for specified protection id")))
      case _ => NotFound(Json.toJson(Error("protection of specified id found, but no match for specified version")))
    }
  }

  // TODO - this is unused and shouldn't be if we're keeping to spec but leaving here as the old version used this approach
  def readProtectionNew(nino: String, protectionId: Long, version: Option[Int]): Action[AnyContent] = Action { implicit request =>
    val protections: Option[Protections] = PLAProtectionService.retrieveProtections(nino)
    val protection: Option[Protection] = protections.get.protections.find(p => p.id == protectionId)
    protection match {
      case Some(protection) if version.isEmpty => Ok(Json.toJson(protection))
      case Some(protection) if version.nonEmpty && protection.previousVersions.get.exists(p => p.version == version.get) =>
        Ok(Json.toJson(protection.previousVersions.get.find(p => p.version == version.get).get))
      case None => NotFound(Json.toJson(Error("no protection found for specified protection id")))
      case _ => NotFound(Json.toJson(Error("protection of specified id found, but no match for specified version")))
    }
  }

  def createProtectionNew(nino:String) : Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CreateLTAProtectionRequest](data =>
      Future.successful(Ok(Json.toJson(PLAProtectionService.createLTAProtectionResponse(data)))))
  }

  def updateProtectionNew(nino:String,protectionId: Long) : Action[JsValue]  = Action.async(parse.json) { implicit request =>
    withJsonBody[UpdateLTAProtectionRequest](data =>
      Future.successful(PLAProtectionService.updateLTAProtection(data,nino,protectionId)))
  }

  /**
    * Facility for Pension Scheme Administartor (PSA) to lookup/verify protection details
    *
    * @param ref    the protecion reference supplied to the PSA by the individual
    * @param psaref the PSA check reference supplied to the PSA by the individual
    * @return a simple result indicating whether valid certificate found, and if valid the type and relevant amount.
    */
  def psaLookup(ref: String, psaref: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    // decode the Nino from the psa ref
    val c1 = psaref.substring(3, 4).toShort.toChar
    val c2 = psaref.substring(5, 6).toShort.toChar
    val nino = c1 + c2 + psaref.substring(7, 12)
    protectionRepository.findLatestVersionsOfAllProtectionsByNino(nino).map { (protections: List[Protection]) =>
      val result = protections.find(_.protectionReference.contains(ref))
      result match {
        case Some(protection) if protection.status == 1 =>
          Ok(Json.toJson(PSALookupResult(protection.`type`, validResult = true, protection.relevantAmount)))
        case _ => Ok(Json.toJson(PSALookupResult(0, validResult = false, None)))
      }
    }
  }

  def psaLookupNew(ref: String, psaref: String): Action[JsValue] = Action(BodyParsers.parse.json) { implicit request =>
    // decode the Nino from the psa ref
    val c1 = psaref.substring(3, 4).toShort.toChar
    val c2 = psaref.substring(5, 6).toShort.toChar
    val nino = c1 + c2 + psaref.substring(7, 12)
    val protections = Generator.genProtections(nino).seeded(nino).get.protections
    val result = protections.find(p => p.protectionReference.contains(ref))
    result match {
      case Some(protection) if protection.status == 1 =>
        Ok(Json.toJson(PSALookupResult(protection.`type`, validResult = true, protection.relevantAmount)))
      case _ => Ok(Json.toJson(PSALookupResult(0, validResult = false, None)))
    }
  }


  /**
    * Updated Facility for Pension Scheme Administrator (PSA) to lookup/verify current protection details
    *
    * @param psaRef the individuals PSA reference number
    * @param ltaRef the lifetime allowance Reference number
    * @return a simple result indicating whether valid certificate found, and if valid the type and relevant amount.
    */
  def updatedPSALookup(psaRef: String, ltaRef: String): Action[AnyContent] = Action.async { implicit request =>
    val validationResult = (psaRef.matches("^PSA[0-9]{8}[A-Z]$"), ltaRefValidator(ltaRef))
    val environment = request.headers.get("Environment")
    val auth = request.headers.get("Authorization")
    if (environment.isEmpty) {
      Logger.error("Request is missing environment header")
      Future.successful(Forbidden)
    } else if (auth.isEmpty) {
      Logger.error("Request is missing auth header")
      Future.successful(Unauthorized(Json.toJson(PSALookupErrorResult("Required OAuth credentials not provided"))))
    } else if (!validationResult._1 | !validationResult._2) {
      val refValidationResponse = validationResult match {
        case (false, false) => "pensionSchemeAdministratorCheckReference, lifetimeAllowanceReference"
        case (false, true) => "pensionSchemeAdministratorCheckReference"
        case (true, false) => "lifetimeAllowanceReference"
      }
      val errorMsg = s"Your submission contains one or more errors. Failed Parameter(s) - [$refValidationResponse]"
      Logger.error(errorMsg)
      val response = PSALookupErrorResult(errorMsg)
      Future.successful(BadRequest(Json.toJson(response)))
    } else {
      Logger.info("Successful request submitted")
      returnPSACheckResult(psaRef, ltaRef)
    }
  }

  // private methods

  /**
    * When passed a psaRef and ltaRef, return specific results used for testing purposes.
    *
    * @param psaRef
    * @param ltaRef
    * @return
    */
  private def returnPSACheckResult(psaRef: String, ltaRef: String): Future[Result] = {
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
      case _ =>
        val response = PSALookupErrorResult("Resource not found")
        Logger.error(response.reason)
        Future.successful(NotFound(Json.toJson(response)))
    }
  }

    /**
      * When passed an exception trigger, either returns the corresponding error response or throws he correct exception/timeout
      *
      * @param trigger
      * @return
      */
    private def processExceptionTrigger(trigger: ExceptionTrigger): Future[Result] = {
      import ExceptionTrigger.ExceptionType
      trigger.extractedExceptionType match {
        case ExceptionType.BadRequest => Future.successful(BadRequest("Simulated bad request"))
        case ExceptionType.NotFound => Future.successful(NotFound("Simulated npot found"))
        case ExceptionType.InternalServerError => Future.successful(InternalServerError("Simulated 500 error"))
        case ExceptionType.BadGateway => Future.successful(BadGateway("Simulated 502 error"))
        case ExceptionType.ServiceUnavailable => Future.successful(ServiceUnavailable("Simulated 503 error"))
        case ExceptionType.UncaughtException => throw new Exception()
        case ExceptionType.Timeout => Thread.sleep(60000); Future.successful(Ok)
      }
    }

  /**
    * Process an application for a new protection for which we have determined the relevant notification ID.
    * A protection will be created and stored in the repository and then returned to the client. The protection
    * will include the notification ID and associated message.
    * A "protection" will be created and returned even for unsuccessful/rejected applications, which can occur
    * if existing protections for the individual do not allow the requested new protection. In such cases the
    * response status will be Conflict (409) and the stored/returned protection does not represent a valid certificate -
    * it just encapsulates the details of the non-successful application.
    *
    * @param nino               Individuals NINO
    * @param applicationRequest Details of the application as parsed from the request body
    * @param notificationID     Identifies the specific outcome of the application, which determines the detailed result
    * @return The result is a created protection (if successful) or a pseudo-protection object containing details of the
    *         error if unsuccessful.
    */
  private def processApplication(nino: String,
                                 applicationRequest: CreateLTAProtectionRequest,
                                 notificationID: Short,
                                 existingProtections: List[Protection]): Future[Result] = {

    val notificationEntry = Notifications.table(notificationID)

    // generate some validly formatted protection reference, if the notification ID indicates a scenario where one is
    // expected
    val protectionReference: Option[String] = notificationID match {
      case 3 | 4 | 8 => Some(("IP14" + Math.abs(Random.nextLong)).substring(0, 9) + "A")
      case 12 => Some(("IP16" + Math.abs(Random.nextLong)).substring(0, 9) + "B")
      case 22 | 23 => Some(("FP16" + Math.abs(Random.nextLong)).substring(0, 9) + "C")
      case _ => None
    }

    val notificationMessage =
      injectMessageParameters(
        notificationEntry.message,
        applicationRequest.protection.`type`,
        applicationRequest.protection.relevantAmount,
        protectionReference,
        genPSACheckRef(nino))

    val successfulApplication = notificationEntry.status match {
      case CertificateStatus.Unsuccessful | CertificateStatus.UnknownStatus | CertificateStatus.Rejected => false
      case _ => true
    }

    val currDate = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
    val currTime = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)
    val totalPensionDebitAmount = applicationRequest.pensionDebits.map { pensionDebits =>
      pensionDebits.map(_.pensionDebitEnteredAmount).sum
    }

    import Protection.Type._
    val maxProtectedAmount = applicationRequest.protection.requestedType match {
      case Some(FP2016) => 1250000.0
      case Some(IP2016) => 1250000.0
      case Some(IP2014) => 1500000.0
    }

    val relevantAmountMinusPSOs = applicationRequest.protection.relevantAmount.map { amt => amt - totalPensionDebitAmount.getOrElse(0.0) }

    val newProtection = Protection(
      nino = nino,
      version = 1,
      // NPS has documented API range constraint 0..4294967295 (i.e. unsigned int full range) for 'id' - code below
      // generates values uniformly distributed across this range (represented as Long since Scala doesn't have an unsigned
      // int type)
      id = Random.nextInt & 0x00000000ffffffffL,
      `type` = applicationRequest.protection.`type`,
      protectionReference = protectionReference,
      status = Notifications.extractedStatus(notificationEntry.status),
      notificationID = Some(notificationID),
      notificationMsg = Some(notificationMessage),
      certificateDate = if (successfulApplication) Some(currDate) else None,
      certificateTime = if (successfulApplication) Some(currTime) else None,
      relevantAmount = relevantAmountMinusPSOs,
      protectedAmount = relevantAmountMinusPSOs.map { amt => amt.min(maxProtectedAmount) },
      preADayPensionInPayment = applicationRequest.protection.preADayPensionInPayment,
      postADayBCE = applicationRequest.protection.postADayBCE,
      uncrystallisedRights = applicationRequest.protection.uncrystallisedRights,
      nonUKRights = applicationRequest.protection.nonUKRights,
      pensionDebits = applicationRequest.pensionDebits,
      pensionDebitTotalAmount = totalPensionDebitAmount
    )

    // certain notifications require changing state of the currently open existing protection to dormant
    val doMaybeUpdateExistingProtection: Future[Any] = notificationID match {
      case 23 =>
        val currentlyOpen = existingProtections.find(_.status == Protection.extractedStatus(Protection.Status.Open))
        currentlyOpen map { openProtection =>
          // amend the protection, giving it a Dormant status & updating the certificate date
          val nowDormantProtection = openProtection.copy(
            status = Protection.extractedStatus(Protection.Status.Dormant),
            version = openProtection.version + 1,
            certificateDate = Some(currDate),
            certificateTime = Some(currTime))
          protectionRepository.insert(nowDormantProtection)
        } getOrElse Future.failed(new Exception("No open protection found, but notification ID indicates one should exist"))
      case 3 =>
        val currentlyOpen = existingProtections.find(_.status == Protection.extractedStatus(Protection.Status.Open))
        currentlyOpen map { openProtection =>
          // amend the protection, giving it a Withdrawn status & updating the certificate date
          val nowWithdrawnProtection = openProtection.copy(
            status = Protection.extractedStatus(Protection.Status.Withdrawn),
            version = openProtection.version + 1,
            certificateDate = Some(currDate),
            certificateTime = Some(currTime))
          protectionRepository.insert(nowWithdrawnProtection)
        } getOrElse Future.failed(new Exception("No open protection found, but notification ID indicates one should exist"))
      case 8 =>
        val currentlyOpen = existingProtections.find(_.status == Protection.extractedStatus(Protection.Status.Open))
        currentlyOpen map { openProtection =>
          // amend the protection, giving it a Dormant status & updating the certificate date
          val nowDormantProtection = openProtection.copy(
            status = Protection.extractedStatus(Protection.Status.Dormant),
            version = openProtection.version + 1,
            certificateDate = Some(currDate),
            certificateTime = Some(currTime))
          protectionRepository.insert(nowDormantProtection)
        } getOrElse Future.failed(new Exception("No open protection found, but notification ID indicates one should exist"))
      case _ => Future.successful() // no update needed for existing protections
    }

    val doUpdateProtections = for {
      _ <- doMaybeUpdateExistingProtection
      done <- protectionRepository.insert(newProtection)
    } yield done

    val response = CreateLTAProtectionResponse(nino = nino, pensionSchemeAdministratorCheckReference = Some(genPSACheckRef(nino)), protection = newProtection.copy(notificationMsg = None))

    val responseBody = Json.toJson(response)

    val result = if (successfulApplication) {
      Ok(responseBody)
    } else {
      if (notificationEntry.status == CertificateStatus.UnknownStatus) {
        // should never happen
        InternalServerError(Json.toJson(responseBody))
      }
      else {
        // unsuccessful/rejected due to conflict with some existing protection
        Conflict(responseBody)
      }
    }

    doUpdateProtections
      .map { _ => result }
      .recover { case exception => Results.InternalServerError(exception.toString) }
  }

  /**
    * Process an amendment request for which we have determined the appropriate notification ID.
    * The new version of the protection will be created and stored in the repository.
    * This may also result in a change to another protection, if the amendment requires the current
    * certificate to be withdrawn and therefore another protection to become active.
    *
    * @param nino             individuals NINO
    * @param current          The current version of the protection to be amended
    * @param amendmentRequest Details of the requested amendment, as parsed from the request body.
    * @param notificationId   The notification Id resulting from the business rule checks
    * @return Updated protection result
    */
  private def processAmendment(nino: String,
                               current: Protection,
                               amendmentRequest: UpdateLTAProtectionRequest,
                               notificationId: Short): Future[Result] = {

    val notificationEntry = Notifications.table(notificationId)

    val newProtectionOpt = notificationEntry.status match {
      case CertificateStatus.Withdrawn =>
        // the amended protection will be withdrawn
        val protectionReference = amendmentRequest.protection.requestedType collect {
          case Protection.Type.IP2014 => ("IP14" + Math.abs(Random.nextLong)).substring(0, 9) + "A"
          case Protection.Type.IP2016 => ("IP16" + Math.abs(Random.nextLong)).substring(0, 9) + "B"
        }
        val notificationMessage =
          injectMessageParameters(
            notificationEntry.message,
            current.`type`,
            Some(amendmentRequest.protection.relevantAmount),
            protectionReference,
            genPSACheckRef(nino))

        // TODO
        // this doesn't actually reflect the business rules - in some 'withdrawn' cases we don't create a new
        // protection as below. Instead a dormant one can become open, and we simply update the amendee status to
        // withdrawn
        val currDate = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val currTime = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)

        val amendmentPsoAmt = amendmentRequest.pensionDebits.map { debits => debits.map(_.pensionDebitEnteredAmount).sum }.getOrElse(0.0)
        val updatedPensionDebitTotalAmount = amendmentRequest.protection.pensionDebitTotalAmount.getOrElse(0.0) + amendmentPsoAmt
        val relevantAmountMinusPSO = amendmentRequest.protection.relevantAmount - amendmentPsoAmt

        import Protection.Type._
        val maxProtectedAmount = amendmentRequest.protection.requestedType match {
          case Some(IP2014) => 1500000.00
          case Some(IP2016) => 1250000.00
        }

        Some(Protection(
          nino = nino,
          version = 1,
          id = Random.nextLong(),
          `type` = amendmentRequest.protection.`type`, // should be unchanged
          protectionReference = protectionReference,
          status = Notifications.extractedStatus(notificationEntry.status),
          notificationID = Some(notificationId),
          notificationMsg = Some(notificationMessage),
          certificateDate = Some(currDate),
          certificateTime = Some(currTime),
          relevantAmount = Some(relevantAmountMinusPSO),
          protectedAmount = Some(relevantAmountMinusPSO.min(maxProtectedAmount)),
          preADayPensionInPayment = Some(amendmentRequest.protection.preADayPensionInPayment),
          postADayBCE = Some(amendmentRequest.protection.postADayBCE),
          uncrystallisedRights = Some(amendmentRequest.protection.uncrystallisedRights),
          nonUKRights = Some(amendmentRequest.protection.nonUKRights),
          pensionDebitTotalAmount = Some(updatedPensionDebitTotalAmount),
          pensionDebits = amendmentRequest.pensionDebits)
        )

      case _ => None
    }

    val amendedProtection = notificationEntry.status match {

      case CertificateStatus.Withdrawn =>
        current.copy(version = current.version + 1, status = Protection.extractedStatus(Protection.Status.Withdrawn))

      case _ =>
        val protectionReference: Option[String] = amendmentRequest.protection.requestedType
          .collect {
            case Protection.Type.IP2014 => ("IP14" + Math.abs(Random.nextLong)).substring(0, 9) + "A"
            case Protection.Type.IP2016 => ("IP16" + Math.abs(Random.nextLong)).substring(0, 9) + "B"
          }
          .filter(_ => notificationEntry.status == CertificateStatus.Open) // only generate ref if certificate is Open

        val notificationMessage =
          injectMessageParameters(
            notificationEntry.message,
            current.`type`,
            Some(amendmentRequest.protection.relevantAmount),
            protectionReference,
            genPSACheckRef(nino))

        val currDate = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val currTime = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)

        val amendmentPsoAmt = amendmentRequest.pensionDebits.map { debits => debits.map(_.pensionDebitEnteredAmount).sum }.getOrElse(0.0)
        val updatedPensionDebitTotalAmount = amendmentRequest.protection.pensionDebitTotalAmount.getOrElse(0.0) + amendmentPsoAmt
        val relevantAmountMinusPSO = amendmentRequest.protection.relevantAmount - amendmentPsoAmt

        import Protection.Type._
        val maxProtectedAmount = amendmentRequest.protection.requestedType match {
          case Some(IP2014) => 1500000.00
          case Some(IP2016) => 1250000.00
        }

        Protection(
          nino = nino,
          version = current.version + 1,
          id = current.id,
          `type` = current.`type`,
          protectionReference = current.protectionReference,
          status = Notifications.extractedStatus(notificationEntry.status),
          certificateDate = Some(currDate),
          certificateTime = Some(currTime),
          notificationID = Some(notificationId),
          notificationMsg = Some(notificationMessage),
          relevantAmount = Some(relevantAmountMinusPSO),
          protectedAmount = Some(relevantAmountMinusPSO.min(maxProtectedAmount)),
          preADayPensionInPayment = Some(amendmentRequest.protection.preADayPensionInPayment),
          postADayBCE = Some(amendmentRequest.protection.postADayBCE),
          uncrystallisedRights = Some(amendmentRequest.protection.uncrystallisedRights),
          nonUKRights = Some(amendmentRequest.protection.nonUKRights),
          pensionDebitTotalAmount = Some(updatedPensionDebitTotalAmount),
          pensionDebits = amendmentRequest.pensionDebits)
    }

    val responseProtection = (newProtectionOpt getOrElse amendedProtection).copy(notificationMsg = None)
    val okResponse = UpdateLTAProtectionResponse(nino, Some(genPSACheckRef(nino)), responseProtection)
    val okResponseBody = Json.toJson(okResponse)
    val result = Ok(okResponseBody)

    val doMaybeCreateNewProtectionFut: Future[Any] = newProtectionOpt map { newProtection =>
      protectionRepository.insert(newProtection)
    } getOrElse Future.successful(None)

    val doAmendProtectionFut = protectionRepository.insert(amendedProtection)

    val updateRepoFut = for {
      _ <- doMaybeCreateNewProtectionFut
      done <- doAmendProtectionFut
    } yield done

    updateRepoFut map { _ => result } recover { case x => InternalServerError(x.toString) }
  }

  // helper for generating a  test PSA Check Ref from a test Nino (n.b. only the stub does this)
  private def ninoChar2TwoDigits(nc: Char) = nc.toShort.toString.substring(0, 1)

  private val psaCheckRefLastCharsList = "ABCDEFGHJKLMNPRSTXYZ".toList

  private def randomLastPSAChar = Random.shuffle(psaCheckRefLastCharsList).head

  private def genPSACheckRef(nino: String) = {
    val d1d2 = ninoChar2TwoDigits(nino.charAt(0))
    val d3d4 = ninoChar2TwoDigits(nino.charAt(1))
    "PSA" + d1d2 + d3d4 + nino.substring(2, 7) + nino.head
  }

  //  /**
  //    * Convert a stored protection history to the result protection object returned to the client
  //    *
  //    * @param protectionVersions  - must have at least one entry. The first entry is always assumed to be the
  //    *                            latest version.
  //    * @param version             if set then the result is the specified version of the protection,
  //    *                            otherwise just returns the latest version
  //    * @param setPreviousVersions if true then fill in the previousVersions field of the result, if and only if
  //    *                            it is latest version of the protection that is returned as the overall result.
  //    **/
  //  private def toResultProtection(protectionVersions: List[Protection],
  //                                 version: Option[Int],
  //                                 setPreviousVersions: Boolean)(implicit request: Request[AnyContent]): Option[Protection] = {
  //    val protectionOpt =
  //      version.fold(protectionVersions.headOption)(v => protectionVersions.find(_.version == v))
  //    protectionOpt map { protection: Protection =>
  //      // generate previousVersions (if applicable) as well as self field into the result protection
  //      val previousVersionList = Some(protectionVersions.tail map { p =>
  //        routes.PLAStubController.readProtection(p.nino, p.id, Some(p.version)).absoluteURL()
  //      }) filter (_ => setPreviousVersions)
  //      // val self = Some(routes.PLAStubController.readProtection(protection.nino, protection.id, None).absoluteURL())
  //      protection.copy(notificationMsg = None)
  ////      protection.copy(previousVersions = previousVersionList, notificationMsg = None)
  //    }
  //  }
  //
  /**
    * Inject any relevant parameter values from Protection into the notification message
    *
    * @param messageTemplate     the static notification message template, which may contain parameter identifiers of format
    *                            '#{param_name}' into which the associated parameter value is injected if available.
    * @param protectionType      parameter value: the type of the protection
    * @param relevantAmount      optional parameter value: the relevant amount, if available
    * @param protectionReference optional parameter value: the protecion reference, if available.
    * @param psaCheckRef         : parameter value: the generated PSA check reference
    * @return
    */
  private def injectMessageParameters(messageTemplate: String,
                                      protectionType: Int,
                                      relevantAmount: Option[Double],
                                      protectionReference: Option[String],
                                      psaCheckRef: String): String = {
    val injectAmount = relevantAmount map { amount =>
      protectionType match {
        case 2 => Math.min(amount, 1500000.00)
        case 3 => Math.min(amount, 1250000.00)
        case _ => amount
      }
    } getOrElse 0.0
    val injectProtectionRef = protectionReference.getOrElse("<NONE>")
    messageTemplate
      .replace("#{amount}", injectAmount.toString)
      .replace("#{reference}", injectProtectionRef)
      .replace("#{psa_reference}", psaCheckRef)
  }

  /**
    * Useful method to validate an ltaRef against the two regular expressions provided by DES
    *
    * @param ltaRef short for lifetimeAllowanceReference
    * @return
    */
  private def ltaRefValidator(ltaRef: String): Boolean = {
    ltaRef.matches("^(IP14|IP16|FP16)[0-9]{10}[ABCDEFGHJKLMNPRSTXYZ]$") | ltaRef.matches("^[1-9A][0-9]{6}[ABCDEFHXJKLMNYPQRSTZW]$")
  }

  //}

  object ControllerHelper {
    /*
   * Checks that the standard extra headers required for NPS requests are present in a request
   * @param headers a simple map of all request headers
   * @param the result of validating the request body
   * @rreturn the overall validation result, of non-success then will include both body and  header validation errors
   */
    def addExtraRequestHeaderChecks[T](headers: Map[String, String], bodyValidationResultJs: JsResult[T]): JsResult[T] = {
      val environment = headers.get("Environment")
      val token = headers.get("Authorization")
      val notSet = "<NOT SET>"
      play.Logger.info("Request headers: environment =" + environment.getOrElse(notSet) + ", authorisation=" + token.getOrElse(notSet))

      //  Ensure any header validation errors are accumulated with any body validation errors into a single JsError
      //  (the below code is not so nice, could be a good use case for scalaz validation)
      val noAuthHeaderErr = JsError("required header 'Authorisation' not set in NPS request")
      val noEnvHeaderErr = JsError("required header 'Environment' not set in NPS request")

      // 1. accumlate any header errors
      def headerNotPresentErrors: Option[JsError] = (environment, token) match {
        case (Some(_), Some(_)) => None
        case (Some(_), None) => Some(noAuthHeaderErr)
        case (None, Some(_)) => Some(noEnvHeaderErr)
        case (None, None) => Some(noAuthHeaderErr ++ noEnvHeaderErr)
      }

      // 2. accumulate any header + any body errors
      (bodyValidationResultJs, headerNotPresentErrors) match {
        case (e1: JsError, e2: Some[JsError]) => e1 ++ e2.get
        case (e1: JsError, _) => e1
        case (_, e2: Some[JsError]) => e2.get
        case _ => bodyValidationResultJs // success case
      }
    }
  }

}
