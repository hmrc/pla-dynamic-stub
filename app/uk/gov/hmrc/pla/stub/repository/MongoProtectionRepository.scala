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

import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DB, WriteConcern}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.pla.stub.model.Protections

import scala.concurrent.{ExecutionContext, Future}

trait ProtectionRepository {
  def findAllProtectionsByNino(nino: String): Future[List[Protections]]
  def findProtectionsByNino(nino: String): Future[Option[Protections]]
  def insertProtection(protections: Protections): Future[WriteResult]
  def removeByNino(nino: String): Future[Unit]
  def removeAllProtections(): Future[Unit]
  def removeProtectionsCollection(): Future[Boolean]
}

class MongoProtectionRepository(implicit mongo: () => DB, implicit val ec: ExecutionContext)
  extends ReactiveRepository[Protections, BSONObjectID]("protections", mongo, Protections.protectionsFormat)
    with ProtectionRepository {

  override def indexes: Seq[Index] = Seq(
    Index(Seq("nino" -> IndexType.Ascending, "id" -> IndexType.Ascending, "version" -> IndexType.Ascending),
      name = Some("ninoIdAndVersionIdx"),
      unique = true, // this should ensure concurrent amendments can't create two objects with same version
      sparse = true)
  )

  override def findAllProtectionsByNino(nino: String): Future[List[Protections]] = {
    find("nino" -> nino)
  }

  override def findProtectionsByNino(nino: String): Future[Option[Protections]] = {
    findAllProtectionsByNino(nino).map {
      _.headOption
    }(ec)
  }

  override def removeByNino(nino: String): Future[Unit] =
    remove("nino" -> nino).map { _ => }(ec)


  override def removeAllProtections(): Future[Unit] =
    removeAll(WriteConcern.Acknowledged).map { _ => }

  override def removeProtectionsCollection(): Future[Boolean] =
    drop(ec)

  override def insertProtection(protections: Protections): Future[WriteResult] =
    insert(protections)(ec)
}
