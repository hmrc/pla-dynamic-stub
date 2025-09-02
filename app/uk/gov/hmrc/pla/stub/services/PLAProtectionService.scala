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

import javax.inject.Inject
import org.mongodb.scala.result.InsertOneResult
import play.api.mvc.Result
import play.api.mvc.Results.{NotFound, Ok}
import uk.gov.hmrc.pla.stub.Generator.pensionSchemeAdministratorCheckReferenceGen
import uk.gov.hmrc.pla.stub.guice.MongoProtectionRepositoryFactory
import uk.gov.hmrc.pla.stub.model._
import uk.gov.hmrc.pla.stub.model.hip.HipProtection
import uk.gov.hmrc.pla.stub.model.hip.HIPProtectionsModel
import uk.gov.hmrc.pla.stub.repository.MongoProtectionRepository

import scala.concurrent.{ExecutionContext, Future}

class PLAProtectionService @Inject() (
    implicit val mongoProtectionRepositoryFactory: MongoProtectionRepositoryFactory,
    val ec: ExecutionContext
) {

  lazy val protectionsStore: MongoProtectionRepository = mongoProtectionRepositoryFactory.apply()

  def saveProtections(protections: Protections): Future[InsertOneResult] = {
    def save(deleted: Unit, data: Protections): Future[InsertOneResult] = protectionsStore.insertProtection(data)

    protectionsStore.removeByNino(protections.nino).flatMap(remove => save(remove, protections))
  }

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

  def retrieveHIPProtections(nino: String): Future[Option[HIPProtectionsModel]] = {
    val ninoWithoutSuffix = nino.dropRight(1)
    retrieveProtections(ninoWithoutSuffix).map(_.map(HIPProtectionsModel(_)))
  }

  def insertOrUpdateHipProtection(protection: HipProtection): Future[Result] =
    insertOrUpdateProtection(protection.toProtection)

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
      save               <- saveProtections(result)
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

  def findProtectionByNinoAndId(nino: String, protectionId: Long): Future[Option[Protection]] =
    for {
      protections <- retrieveProtections(nino)
      result = protections.flatMap(_.protections.find(_.id == protectionId))
    } yield result

  def findHipProtectionByNinoAndId(nino: String, protectionId: Int): Future[Option[HipProtection]] = {
    val ninoWithoutSuffix = nino.dropRight(1)
    findProtectionByNinoAndId(ninoWithoutSuffix, protectionId).map(_.flatMap(HipProtection.fromProtection)) // If protection fails to convert it is ignored
  }

}
