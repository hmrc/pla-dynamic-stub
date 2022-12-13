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

import javax.inject.Inject
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.pla.stub.model.ExceptionTrigger
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.Filters._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Mongo repository for use by PLA dynamic stub to store/retrieve the types of exceptions to throw during downstream error testing
 */
trait ExceptionTriggerRepository {
  def findExceptionTriggerByNino(nino: String)(implicit ec: ExecutionContext): Future[Option[ExceptionTrigger]]
  def removeAllExceptionTriggers()(implicit ec: ExecutionContext): Future[Unit]
}

class MongoExceptionTriggerRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[ExceptionTrigger](
    mongoComponent = mongoComponent,
    collectionName = "exceptionTriggers",
    domainFormat = ExceptionTrigger.exceptionTriggerFormat,
    indexes = Seq(
      IndexModel(ascending("nino"), IndexOptions()
          .name("ninoIndex").unique(true).sparse(true)
      )
    )
  )
with ExceptionTriggerRepository {

  override def findExceptionTriggerByNino(nino: String)(implicit ec: ExecutionContext): Future[Option[ExceptionTrigger]] = {
    collection.find(equal("nino", nino)).toFuture().map {
      triggerList => triggerList.headOption
    }
  }

  override def removeAllExceptionTriggers()(implicit ec: ExecutionContext): Future[Unit] =
    collection.deleteMany(empty()).toFuture().map(_ => {})
}
