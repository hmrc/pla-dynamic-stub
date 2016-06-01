/*
 * Copyright 2016 HM Revenue & Customs
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

import uk.gov.hmrc.pla.stub.notifications.{CertificateStatus, Notifications}
import uk.gov.hmrc.play.microservice.controller.BaseController

import play.api.mvc._
import play.api.libs.json._

import uk.gov.hmrc.pla.stub.repository.{ProtectionRepository, MongoProtectionRepository}
import uk.gov.hmrc.pla.stub.model._
import uk.gov.hmrc.pla.stub.rules._

import scala.Error
import scala.concurrent.Future
import scala.util.Random
import java.time.LocalDateTime

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * The controller for the Protect your Lifetime Allowance (PLA) service REST API dynamic stub
  */

object PLAStubController extends PLAStubController {
  override val protectionRepository = MongoProtectionRepository()
}

trait PLAStubController extends BaseController {

	val protectionRepository: ProtectionRepository

  /**
    * Get all current protections applicable to the specified Nino
    *
    * @param nino identifies the individual to whom the protections apply
    * @return List of latest versions of each protection
    **/

	def readProtections(nino: String) = Action.async { implicit request =>
    protectionRepository.findAllVersionsOfAllProtectionsByNino(nino).map { (protections: Map[Long,List[Protection]]) =>

      // get all current protections in the form expected in the result
      val resultProtections = protections.values
        .map { p => toResultProtection(p, None, setPreviousVersions = true) }
        .collect { case Some(protection) => protection }

      val psaCheckRef=genPSACheckRef(nino)

      val result = Protections(nino, psaCheckRef, resultProtections.toList)
      Ok(Json.toJson(result))
    }
	}

  def readProtection(nino: String, protectionId: Long, version: Option[Int]) = Action.async { implicit request =>
    protectionRepository.findAllVersionsOfProtectionByNinoAndId(nino,protectionId) map { protectionHistory: List[Protection] =>
      toResultProtection(protectionHistory, version, setPreviousVersions = !version.isDefined)
        .map { result =>
          Ok(Json.toJson(result))
        }
        .getOrElse {
          // no matching protection
          val error = protectionHistory match {
            case Nil => Error("no protection found for specified protection id")
            case _ => Error("protection of specified id found, but no match for specified version")
          }
          NotFound(Json.toJson(error))
        }
    }
  }

  def createProtection(nino: String) = Action.async (BodyParsers.parse.json) { implicit request =>
    val protectionApplicationJs = request.body.validate[CreateLTAProtectionRequest]
    protectionApplicationJs.fold(
      errors => Future.successful(BadRequest(Json.toJson(Error(message="body failed validation with errors: " + errors)))),
      createProtectionRequest =>
        createProtectionRequest.protection.requestedType
            .collect {
            // gather the relevant rules
            case Protection.Type.FP2016 => FP2016ApplicationRules
            case Protection.Type.IP2014 => IP2014ApplicationRules
            case Protection.Type.IP2016 => IP2016ApplicationRules
          }
          .map { appRules: ApplicationRules =>
            // apply the rules against any existing protections to determine the notification ID, and then process
            // the application according to that ID
            val existingProtectionsFut = protectionRepository.findLatestVersionsOfAllProtectionsByNino(nino)
            existingProtectionsFut flatMap { existingProtections: List[Protection] =>
              val notificationId = appRules.check(existingProtections)
              processApplication(nino, createProtectionRequest.protection, notificationId, existingProtections)
            }
          }
          .getOrElse {
            val error = Error("invalid protection type specified")
            Future.successful(BadRequest(Json.toJson(error)))
          }
    )
  }

	def updateProtection(nino: String,
                          protectionId: Long) = Action.async (BodyParsers.parse.json) { implicit request =>
    val protectionAmendmentJs = request.body.validate[ProtectionAmendment]
    protectionAmendmentJs.fold(
      errors => Future.successful(BadRequest(Json.toJson(Error(message = "failed validation with errors: " + errors)))),
      protectionAmendment => {
        // first cross-check relevant amount against total of the breakdown fields, reject if discrepancy found
        val calculatedRelevantAmount =
          protectionAmendment.nonUKRights +
            protectionAmendment.postADayBCE +
            protectionAmendment.preADayPensionInPayment +
            protectionAmendment.uncrystallisedRights
        if (calculatedRelevantAmount != protectionAmendment.relevantAmount) {
          Future.successful(BadRequest(Json.toJson(
            Error(message = "The specified Relevant Amount is not the sum of the specified breakdown amounts " +
              "(non UK Rights + Post A Day BCE + Pre A Day Pensions In Payment + Uncrystallised Rights)"))))
        }

        val amendmentTargetFutureOption = protectionRepository.findLatestVersionOfProtectionByNinoAndId(nino, protectionId)
        amendmentTargetFutureOption flatMap {
          case None =>
            Future.successful(NotFound(Json.toJson(Error(message = "protection to amend not found"))))
          case Some(amendmentTarget) if amendmentTarget.`type` != protectionAmendment.`type` =>
            val error = Error("specified protection type does not match that of the protection to be amended")
            Future.successful(BadRequest(Json.toJson(error)))
          case Some(amendmentTarget) =>
            protectionRepository.findLatestVersionsOfAllProtectionsByNino(nino) flatMap { existingProtections =>
              protectionAmendment.requestedType
                .collect {
                  // gather the rules to be applied
                  case Protection.Type.IP2014 => IP2014AmendmentRules
                  case Protection.Type.IP2016 => IP2016AmendmentRules
                }
                .map { rules: AmendmentRules =>
                  // apply the rules against any existing protections to determine the notification ID, and then process
                  // the requested amendment according to that ID
                  val notificationId = rules.check(calculatedRelevantAmount, existingProtections)
                  processAmendment(nino, amendmentTarget, protectionAmendment, notificationId)
                }
                .getOrElse {
                  // no amendment rules matching specified protection type
                  val error = Error("invalid protection type specified")
                  Future.successful(BadRequest(Json.toJson(error)))
                }
            }
        }
      }
    )
  }

  /**
    * Facility for Pension Scheme Administartor (PSA) to lookup/verify protection details
    *
    * @param ref the protecion reference supplied to the PSA by the individual
    * @param psaref the PSA check reference supplied to the PSA by the individual
    * @return a simple result indicating whether valid certificate found, and if valid the type and relevant amount.
    */
  def psaLookup(ref: String, psaref: String) = Action.async (BodyParsers.parse.json) { implicit request =>
    // decode the Nino from the psa ref
    val c1 = psaref.substring(3, 4).toShort.toChar
    val c2 = psaref.substring(5, 6).toShort.toChar
    val nino = c1 + c2 + psaref.substring(7, 12)
    protectionRepository.findLatestVersionsOfAllProtectionsByNino(nino).map { (protections: List[Protection]) =>
      val result = protections.find(_.protectionReference.contains(ref))
      result match {
        case Some(protection) if protection.status == 1 =>
          Ok(Json.toJson(PSALookupResult(protection.`type`,validResult = true, protection.relevantAmount)))
        case _ => Ok(Json.toJson(PSALookupResult(0,validResult = false, None)))
      }
    }
  }

  // private methods

  /**
    * Process an application for a new protection for which we have determined the relevant notification ID.
    * A protection will be created and stored in the repository and then returned to the client. The protection
    * will include the notification ID and associated message.
    * A "protection" will be created and returned even for unsuccessful/rejected applications, which can occur
    * if existing protections for the individual do not allow the requested new protection. In such cases the
    * response status will be Conflict (409) and the stored/returned protection does not represent a valid certificate -
    * it just encapsulates the details of the non-successful application.
    *
    * @param nino Individuals NINO
    * @param application Individuals application for a pensions lifetime allowance protection
    * @param notificationID Identifies the specific outcome of the application, which determines the detailed result
    * @return The result is a created protection (if successful) or a pseudo-protection object containing details of the
    *         error if unsuccessful.
    */
  private def processApplication(nino: String,
                                 application: CreateLTAProtectionRequest.ProtectionDetails,
                                 notificationID: Short,
                                 existingProtections: List[Protection]): Future[Result] = {

    val notificationEntry = Notifications.table(notificationID)

    // generate some validly formatted protection reference, if the notification ID indicates a scenario where one is
    // expected
    val protectionReference: Option[String] = notificationID match {
      case 4 => Some(("IP14" + Math.abs(Random.nextLong)).substring(0,9) + "A")
      case 12 => Some(("IP16" + Math.abs(Random.nextLong)).substring(0,9) + "B")
      case 22 | 23 | 24 =>  Some(("FP16" + Math.abs(Random.nextLong)).substring(0,9) + "C")
      case _ => None
    }

    val notificationMessage =
      injectMessageParameters(
        notificationEntry.message,
        application.`type`,
        application.relevantAmount,
        protectionReference,
        genPSACheckRef(nino))

    val successfulApplication = notificationEntry.status match {
      case CertificateStatus.Unsuccessful | CertificateStatus.UnknownStatus | CertificateStatus.Rejected => false
      case _ => true
    }

    val newProtection = Protection(
      nino = nino,
      version = 1,
      protectionID = Random.nextLong,
      `type`=application.`type`,
      protectionReference=protectionReference,
      status = Notifications.extractedStatus(notificationEntry.status),
      notificationId = Some(notificationID),
      notificationMsg = Some(notificationMessage),
      certificateDate = if (successfulApplication) Some(LocalDateTime.now) else None,
      relevantAmount = application.relevantAmount,
      preADayPensionInPayment = application.preADayPensionInPayment,
      postADayBCE = application.postADayBCE,
      uncrystallisedRights = application.uncrystallisedRights,
      pensionDebitAmount = application.pensionDebitAmount,
      nonUKRights = application.nonUKRights)

    // certain notifications require changing state of the currently open existing protection to dormant
    val doMaybeUpdateExistingProtection: Future[Any] = notificationID match {
      case 23 =>
        val currentlyOpen = existingProtections.find(_.status==Protection.extractedStatus(Protection.Status.Open))
        currentlyOpen map { openProtection =>
          // amend the protection, giving it a Dormant status & updating the certificate date
          val nowDormantProtection = openProtection.copy(
            status = Protection.extractedStatus(Protection.Status.Dormant),
            version = openProtection.version + 1,
            certificateDate = Some(LocalDateTime.now))
          protectionRepository.insert(nowDormantProtection)
        } getOrElse Future.failed(new Exception("No open protection found, but notification ID indicates one should exist"))
      case _ => Future.successful()  // no update needed for existing protections
    }

    val doUpdateProtections = for {
      _ <- doMaybeUpdateExistingProtection
      done <- protectionRepository.insert(newProtection)
    } yield done

    val responseBody = Json.toJson(newProtection.copy(notificationMsg = None))

    val result = if (successfulApplication) {
      Ok(responseBody)
    } else {
      if (notificationEntry.status==CertificateStatus.UnknownStatus)
      {
        // should never happen
        InternalServerError(Json.toJson(responseBody))
      }
      else
      {
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
    * @param nino individuals NINO
    * @param current The current version of the protection to be amended
    * @param amendment The requested amendment
    * @param notificationId The notification Id resulting from the business rule checks
    * @return Updated protection result
    */
  private def processAmendment(nino: String,
                               current: Protection,
                               amendment: ProtectionAmendment,
                               notificationId: Short): Future[Result] = {

    val notificationEntry = Notifications.table(notificationId)

    val newProtectionOpt = notificationEntry.status match {
      case CertificateStatus.Withdrawn =>
        // the amended protecion will be withdrawn
        val protectionReference = amendment.requestedType collect {
          case Protection.Type.IP2014 => ("IP14" + Math.abs(Random.nextLong)).substring(0, 9) + "A"
          case Protection.Type.IP2016 => ("IP16" + Math.abs(Random.nextLong)).substring(0, 9) + "B"
        }
        val notificationMessage =
          injectMessageParameters(
            notificationEntry.message,
            current.`type`,
            Some(amendment.relevantAmount),
            protectionReference,
            genPSACheckRef(nino))

        // TODO
        // this doesn't actually reflect the business rules - in some 'withdrawn' cases we don't create a new
        // protection as below. Instead a dormant one can become open, and we simply update the amendee status to
        // withdrawn

        Some(Protection(
          nino = nino,
          version = 1,
          protectionID = Random.nextLong,
          `type` = amendment.`type`,
          protectionReference = protectionReference,
          status = Notifications.extractedStatus(notificationEntry.status),
          notificationId = Some(notificationId),
          notificationMsg = Some(notificationMessage),
          certificateDate = Some(LocalDateTime.now),
          relevantAmount = Some(amendment.relevantAmount),
          preADayPensionInPayment = Some(amendment.preADayPensionInPayment),
          postADayBCE = Some(amendment.postADayBCE),
          uncrystallisedRights = Some(amendment.uncrystallisedRights),
          pensionDebitAmount = amendment.pensionDebitAmount,
          nonUKRights = Some(amendment.nonUKRights)
        ))

      case _ => None
    }

    val amendedProtection = notificationEntry.status match {

      case CertificateStatus.Withdrawn =>
        current.copy(version = current.version + 1, status = Protection.extractedStatus(Protection.Status.Withdrawn))

      case _ =>
        val protectionReference: Option[String] = amendment.requestedType
              .collect {
                case Protection.Type.IP2014 => ("IP14" + Math.abs(Random.nextLong)).substring(0, 9) + "A"
                case Protection.Type.IP2016 => ("IP16" + Math.abs(Random.nextLong)).substring(0, 9) + "B"
              }
              .filter(_ => notificationEntry.status==CertificateStatus.Open) // only generate ref if certificate is Open

        val notificationMessage =
          injectMessageParameters(
            notificationEntry.message,
            current.`type`,
            Some(amendment.relevantAmount),
            protectionReference,
            genPSACheckRef(nino))

        Protection(
          nino = nino,
          version = current.version + 1,
          protectionID = current.protectionID,
          `type` = current.`type`,
          protectionReference = current.protectionReference,
          status = Notifications.extractedStatus(notificationEntry.status),
          certificateDate = Some(LocalDateTime.now),
          notificationId = Some(notificationId),
          notificationMsg = Some(notificationMessage),
          relevantAmount = Some(amendment.relevantAmount),
          preADayPensionInPayment = Some(amendment.preADayPensionInPayment),
          postADayBCE = Some(amendment.postADayBCE),
          uncrystallisedRights = Some(amendment.uncrystallisedRights),
          pensionDebitAmount = amendment.pensionDebitAmount,
          nonUKRights = Some(amendment.nonUKRights))
    }

    val responseBody = Json.toJson((newProtectionOpt getOrElse amendedProtection).copy(notificationMsg = None))
    val result = Ok(responseBody)

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
  private def ninoChar2TwoDigits(nc: Char) = nc.toShort.toString.substring(0,1)
  private val psaCheckRefLastCharsList="ABCDEFGHJKLMNPRSTXYZ".toList
  private def randomLastPSAChar=Random.shuffle(psaCheckRefLastCharsList).head
  private def genPSACheckRef(nino: String) = {
    val d1d2 = ninoChar2TwoDigits(nino.charAt(0))
    val d3d4 = ninoChar2TwoDigits(nino.charAt(1))
    "PSA" + d1d2 + d3d4 + nino.substring(2,7) + randomLastPSAChar
  }

  /**
    * Convert a stored protection history to the result protection object returned to the client
    *
    * @param protectionVersions - must have at least one entry. The first entry is always assumed to be the
    *                          latest version.
    * @param version           if set then the result is the specified version of the protection,
    *                          otherwise just returns the latest version
    * @param setPreviousVersions if true then fill in the previousVersions field of the result, if and only if
    *                            it is latest version of the protection that is returned as the overall result.
    **/
  private def toResultProtection(protectionVersions: List[Protection],
                                 version: Option[Int],
                                 setPreviousVersions: Boolean)(implicit request: Request[AnyContent]): Option[Protection] = {
    val protectionOpt =
      version.fold(protectionVersions.headOption)(v => protectionVersions.find(_.version == v))
    protectionOpt map { protection: Protection =>
      // generate previousVersions (if applicable) as well as self field into the result protection
      val previousVersions = Some(protectionVersions.tail map { p =>
        routes.PLAStubController.readProtection(p.nino, p.protectionID, Some(p.version)).absoluteURL()
      }) filter(_ => setPreviousVersions)
      val self = Some(routes.PLAStubController.readProtection(protection.nino, protection.protectionID, None).absoluteURL())
      protection.copy(self = self, previousVersions = previousVersions, notificationMsg = None)
    }
  }

  /**
    * Inject any relevant parameter values from Protection into the notification message
    *
    * @param messageTemplate the static notification message template, which may contain parameter identifiers of format
    *                        '#{param_name}' into which the associated parameter value is injected if available.
    * @param protectionType parameter value: the type of the protection
    * @param relevantAmount optional parameter value: the relevant amount, if available
    * @param protectionReference optional parameter value: the protecion reference, if available.
    * @param psaCheckRef: parameter value: the generated PSA check reference
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
      .replace("#{amount}",injectAmount.toString)
      .replace("#{reference}",injectProtectionRef)
      .replace("#{psa_reference}",psaCheckRef)
  }
}