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

import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.WriteConcern
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.pla.stub.model.ExceptionTrigger

import scala.concurrent.{ExecutionContext, Future}

/**
 * Mongo repository for use by PLA dynamic stub to store/retrieve the types of exceptions to throw during downstream error testing
 */
trait ExceptionTriggerRepository {
  def findExceptionTriggerByNino(nino: String)(implicit ec: ExecutionContext): Future[Option[ExceptionTrigger]]
  def removeAllExceptionTriggers()(implicit ec: ExecutionContext): Future[Unit]
}

class MongoExceptionTriggerRepository()(implicit reactiveMongoComponent: ReactiveMongoComponent)
  extends ReactiveRepository[ExceptionTrigger, BSONObjectID]("exceptionTriggers",
  reactiveMongoComponent.mongoConnector.db,ExceptionTrigger.exceptionTriggerFormat)
with ExceptionTriggerRepository {

  override def indexes = Seq(
    Index(Seq("nino" -> IndexType.Ascending),
      name = Some("ninoIndex"),
      unique = true, // this should ensure concurrent amendments can't create two objects with same version
      sparse = true)
  )

  override def findExceptionTriggerByNino(nino: String)(implicit ec: ExecutionContext): Future[Option[ExceptionTrigger]] = {
    find("nino" -> nino).map {
      triggerList => triggerList.headOption
    }
  }

  override def removeAllExceptionTriggers()(implicit ec: ExecutionContext): Future[Unit] =
    removeAll(WriteConcern.Acknowledged).map {_ => }
}
