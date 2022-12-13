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

package uk.gov.hmrc.pla.stub.repository

import uk.gov.hmrc.pla.stub.model.Protections
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import org.mongodb.scala.result.InsertOneResult
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

trait ProtectionRepository {
  def findAllProtectionsByNino(nino: String): Future[List[Protections]]
  def findProtectionsByNino(nino: String): Future[Option[Protections]]
  def insertProtection(protections: Protections): Future[InsertOneResult]
  def removeByNino(nino: String): Future[Unit]
  def removeAllProtections(): Future[Unit]
  def removeProtectionsCollection(): Future[Boolean]
}

class MongoProtectionRepository(mongoComponent: MongoComponent)(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[Protections](
    mongoComponent = mongoComponent,
    collectionName ="protections",
    domainFormat = Protections.protectionsFormat,
    indexes = Seq(
      IndexModel(ascending("nino", "id", "version"), IndexOptions()
      .name("ninoIdAndVersionIdx").unique(true).sparse(true))
    )
  )
  with ProtectionRepository {

  override def findAllProtectionsByNino(nino: String): Future[List[Protections]] = {
    collection.find(equal("nino", nino)).toFuture().map(_.toList)
  }

  override def findProtectionsByNino(nino: String): Future[Option[Protections]] = {
    findAllProtectionsByNino(nino).map {
      _.headOption
    }
  }

  override def removeByNino(nino: String): Future[Unit] =
    collection.deleteOne(equal("nino", nino)).toFuture().map { _ => }


  override def removeAllProtections(): Future[Unit] =
    collection.deleteMany(empty()).toFuture().map { _ => }

  override def removeProtectionsCollection(): Future[Boolean] =
    collection.drop().toFuture().map(_ => true)

  override def insertProtection(protections: Protections): Future[InsertOneResult] =
    collection.insertOne(protections).toFuture()
}
