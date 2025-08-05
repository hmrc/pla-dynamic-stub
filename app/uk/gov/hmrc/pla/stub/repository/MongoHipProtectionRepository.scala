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

import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import org.mongodb.scala.result.InsertOneResult
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.pla.stub.model.hip.HipProtections
import javax.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

trait HipProtectionRepository {
  def findAllProtectionsByNino(nino: String): Future[List[HipProtections]]
  def findProtectionsByNino(nino: String): Future[Option[HipProtections]]
  def insertProtection(protections: HipProtections): Future[InsertOneResult]
  def removeByNino(nino: String): Future[Unit]
  def removeAllProtections(): Future[Unit]
  def removeProtectionsCollection(): Future[Boolean]
}

@Singleton
class MongoHipProtectionRepository(mongoComponent: MongoComponent)(implicit val ec: ExecutionContext)
    extends PlayMongoRepository[HipProtections](
      mongoComponent = mongoComponent,
      collectionName = "hip-protections",
      domainFormat = HipProtections.protectionsFormat,
      indexes = Seq(
        IndexModel(
          ascending("nino", "id", "sequence"),
          IndexOptions()
            .name("ninoIdAndSequenceIdx")
            .unique(true)
            .sparse(true)
        )
      )
    )
    with HipProtectionRepository {

  override def findAllProtectionsByNino(nino: String): Future[List[HipProtections]] =
    collection.find(equal("nino", nino)).toFuture().map(_.toList)

  override def findProtectionsByNino(nino: String): Future[Option[HipProtections]] =
    findAllProtectionsByNino(nino).map {
      _.headOption
    }

  override def removeByNino(nino: String): Future[Unit] =
    collection.deleteOne(equal("nino", nino)).toFuture().map { _ => }

  override def removeAllProtections(): Future[Unit] =
    collection.deleteMany(empty()).toFuture().map { _ => }

  override def removeProtectionsCollection(): Future[Boolean] =
    collection.drop().toFuture().map(_ => true)

  override def insertProtection(protections: HipProtections): Future[InsertOneResult] =
    collection.insertOne(protections).toFuture()

}
