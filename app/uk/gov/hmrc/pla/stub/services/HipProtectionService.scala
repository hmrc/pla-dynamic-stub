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

import org.mongodb.scala.result.InsertOneResult
import play.api.mvc.Result
import play.api.mvc.Results.{NotFound, Ok}
import uk.gov.hmrc.pla.stub.Generator.pensionSchemeAdministratorCheckReferenceGen
import uk.gov.hmrc.pla.stub.guice.MongoHipProtectionRepositoryFactory
import uk.gov.hmrc.pla.stub.model.hip.{HipProtection, HipProtections, ProtectionStatus}
import uk.gov.hmrc.pla.stub.repository.MongoHipProtectionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HipProtectionService @Inject() (
    implicit val mongoProtectionRepositoryFactory: MongoHipProtectionRepositoryFactory,
    val ec: ExecutionContext
) {

  lazy val protectionsStore: MongoHipProtectionRepository = mongoProtectionRepositoryFactory.apply()

  def saveProtections(protections: HipProtections): Future[InsertOneResult] = {
    def save(deleted: Unit, data: HipProtections): Future[InsertOneResult] = protectionsStore.insertProtection(data)

    protectionsStore.removeByNino(protections.nino).flatMap(remove => save(remove, protections))
  }

  def updateDormantProtectionStatusAsOpen(nino: String): Future[Unit] =
    retrieveProtections(nino).map { optProtections =>
      val protections = optProtections.get
      protections.protections.find(_.status == ProtectionStatus.Dormant) match {
        case Some(existingDormantProtection) =>
          val ltaProtections: List[HipProtection] =
            existingDormantProtection.copy(status = ProtectionStatus.Open) :: protections.protections
              .filter(
                _.status != ProtectionStatus.Dormant
              )
          saveProtections(protections.copy(protections = ltaProtections))
        case None => ()
      }
    }

  def retrieveProtections(nino: String): Future[Option[HipProtections]] =
    protectionsStore.findProtectionsByNino(nino)

  def insertOrUpdateProtection(protection: HipProtection): Future[Result] = {
    val protections                              = protectionsStore.findProtectionsByNino(protection.nino)
    val pensionSchemeAdministratorCheckReference = pensionSchemeAdministratorCheckReferenceGen.sample

    def ltaProtections(optProtections: Option[HipProtections]): Future[List[HipProtection]] = Future {
      optProtections match {
        case Some(value) => protection :: value.protections.filter(_.id != protection.id)
        case None        => List(protection)
      }
    }

    def listToProtections(list: List[HipProtection]): Future[HipProtections] = Future(
      HipProtections(protection.nino, pensionSchemeAdministratorCheckReference, list)
    )

    for {
      optProtections     <- protections
      updatedProtections <- ltaProtections(optProtections)
      result             <- listToProtections(updatedProtections)
      save               <- saveProtections(result)
    } yield Ok
  }

  def removeProtectionByNinoAndProtectionId(nino: String, protectionId: Int): Future[Result] =
    retrieveProtections(nino).map { optProtections =>
      val protections = optProtections.get
      protections.protections.find(_.id == protectionId) match {
        case Some(_) =>
          val ltaProtections: List[HipProtection] = protections.protections.filter(_.id != protectionId)
          saveProtections(protections.copy(protections = ltaProtections))
          Ok
        case None => NotFound
      }
    }

  def findAllProtectionsByNino(nino: String): Future[Option[List[HipProtection]]] =
    retrieveProtections(nino).map {
      case Some(protections) => Some(protections.protections)
      case _                 => None
    }

  def findProtectionByNinoAndId(nino: String, protectionId: Int): Future[Option[HipProtection]] = {
    val protections: Future[Option[HipProtections]] = retrieveProtections(nino)
    protections.map {
      _.flatMap(_.protections.find(p => p.id == protectionId))
    }
  }

}
