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

package uk.gov.hmrc.pla.stub.services

import play.api.mvc.Result
import play.api.mvc.Results.{NotFound, Ok}
import uk.gov.hmrc.pla.stub.Generator.pensionSchemeAdministratorCheckReferenceGen
import uk.gov.hmrc.pla.stub.model._
import uk.gov.hmrc.pla.stub.model.hip.{HIPProtectionsModel, HipProtection}
import uk.gov.hmrc.pla.stub.repository.MongoProtectionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PLAProtectionService @Inject() (
    val protectionsStore: MongoProtectionRepository,
    implicit val ec: ExecutionContext
) {

  def saveProtections(protections: Protections): Future[Unit] =
    for {
      _ <- protectionsStore.removeByNino(protections.nino)
      _ <- protectionsStore.insertProtection(protections)
    } yield ()

  def updateDormantProtectionStatusAsOpen(nino: String): Future[Unit] =
    retrieveProtections(nino).map { optProtections =>
      val protections = optProtections.get
      protections.protections.find(_.status == 2) match {
        case Some(existingDormantProtection) =>
          val ltaProtections: List[Protection] =
            existingDormantProtection.copy(status = 1) :: protections.protections.filter(_.status != 2)
          saveProtections(protections.copy(protections = ltaProtections))
        case None => ()
      }
    }

  def retrieveProtections(nino: String): Future[Option[Protections]] =
    protectionsStore.findProtectionsByNino(nino)

  def retrieveHIPProtections(nino: String): Future[Option[HIPProtectionsModel]] =
    retrieveProtections(nino).map(_.map(HIPProtectionsModel(_)))

  def insertOrUpdateHipProtection(hipProtection: HipProtection): Future[Result] =
    insertOrUpdateProtection(hipProtection.toProtection)

  def insertOrUpdateProtection(protection: Protection): Future[Result] = {
    val protections                              = protectionsStore.findProtectionsByNino(protection.nino)
    val pensionSchemeAdministratorCheckReference = pensionSchemeAdministratorCheckReferenceGen.sample

    def ltaProtections(optProtections: Option[Protections]): Future[List[Protection]] = Future {
      optProtections match {
        case Some(value) => protection :: value.protections.filter(_.id != protection.id)
        case None        => List(protection)
      }
    }

    def listToProtections(list: List[Protection]): Future[Protections] = Future(
      Protections(protection.nino, pensionSchemeAdministratorCheckReference, list)
    )

    for {
      optProtections     <- protections
      updatedProtections <- ltaProtections(optProtections)
      result             <- listToProtections(updatedProtections)
      _                  <- saveProtections(result)
    } yield Ok
  }

  def removeProtectionByNinoAndProtectionId(nino: String, protectionId: Long): Future[Result] =
    retrieveProtections(nino).map { optProtections =>
      val protections = optProtections.get
      protections.protections.find(_.id == protectionId) match {
        case Some(_) =>
          val ltaProtections: List[Protection] = protections.protections.filter(_.id != protectionId)
          saveProtections(protections.copy(protections = ltaProtections))
          Ok
        case None => NotFound
      }
    }

  def findAllProtectionsByNino(nino: String): Future[Option[List[Protection]]] =
    retrieveProtections(nino).map {
      case Some(protections) => Some(protections.protections)
      case _                 => None
    }

  def findAllHipProtectionsByNino(nino: String): Future[List[HipProtection]] =
    retrieveProtections(nino).map {
      case Some(protections) => protections.protections.flatMap(HipProtection.fromProtection)
      case _                 => List.empty[HipProtection]
    }

  def findProtectionByNinoAndId(nino: String, protectionId: Long): Future[Option[Protection]] =
    for {
      protections <- retrieveProtections(nino)
      result = protections.flatMap(_.protections.find(_.id == protectionId))
    } yield result

  def findHipProtectionByNinoAndId(nino: String, protectionId: Long): Future[Option[HipProtection]] =
    findProtectionByNinoAndId(nino, protectionId).map(_.flatMap(HipProtection.fromProtection)) // If protection fails to convert it is ignored

}
