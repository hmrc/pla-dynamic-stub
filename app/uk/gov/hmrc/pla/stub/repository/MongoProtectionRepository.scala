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

package uk.gov.hmrc.pla.stub.repository

import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Indexes.*
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.pla.stub.model.Protections

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoProtectionRepository @Inject() (
    mongoComponent: MongoComponent
)(using ExecutionContext)
    extends PlayMongoRepository[Protections](
      mongoComponent = mongoComponent,
      collectionName = "protections",
      domainFormat = Protections.format,
      indexes = Seq(
        IndexModel(
          ascending("nino", "id", "version"),
          IndexOptions()
            .name("ninoIdAndVersionIdx")
            .unique(true)
            .sparse(true)
        )
      )
    ) {

  def findAllProtectionsByNino(nino: String): Future[List[Protections]] =
    collection.find(equal("nino", nino)).toFuture().map(_.toList)

  def findProtectionsByNino(nino: String): Future[Option[Protections]] =
    findAllProtectionsByNino(nino).map {
      _.headOption
    }

  def removeByNino(nino: String): Future[Unit] =
    collection.deleteOne(equal("nino", nino)).toFuture().map { _ => }

  def removeAllProtections(): Future[Unit] =
    collection.deleteMany(empty()).toFuture().map { _ => }

  def removeProtectionsCollection(): Future[Boolean] =
    collection.drop().toFuture().map(_ => true)

  def insertProtection(protections: Protections): Future[Unit] =
    collection.insertOne(protections).toFuture().map(_ => (): Unit)

}
