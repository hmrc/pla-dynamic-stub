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

package uk.gov.hmrc.pla.stub.model

import play.api.libs.json._

case class Version(
                    version: Int,
                    link: String,
                    protection: Protection)

object Version {


  /**
    * This is a bit hacky but can't find a better way
    */
  implicit object VersionFormat extends Format[Version] {
    override def writes(o: Version): JsValue = {
      Json.obj("version" -> o.version,
        "link" -> o.link)
    }

   override def reads(json: JsValue): JsResult[Version] =
     JsSuccess(Version(-1, "foo", dummyProtection))
  }

  val dummyProtection = new Protection("nino",
    1L,
    1,
    1,
    1,
    None,
    None,
    None)
}

