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

package uk.gov.hmrc.pla.stub.guice

import com.google.inject.AbstractModule
import javax.inject.Inject
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.pla.stub.repository.MongoProtectionRepository

import scala.concurrent.ExecutionContext

class MongoProtectionRepositoryFactory @Inject()(val mongoComponent: MongoComponent,
                                                 implicit val ec: ExecutionContext){
  private lazy val repository = new MongoProtectionRepository(mongoComponent)
  def apply(): MongoProtectionRepository = repository
}

class GuiceModuleLoader extends AbstractModule {
    override def configure(): Unit = {
      // Sample bind config (if needed in the future)
      //bind(classOf[TraitToImplement]).to(classOf[ClassToImplement]).asEagerSingleton()
    }
}
