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

package uk.gov.hmrc.pla.stub.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class ProtectionsFormatSpec extends AnyWordSpec with Matchers {

  import ProtectionTestData._

  "FP2016 json read and write functions" when {
    "be an isomorphic pair" in {
      val json             = Json.toJson(openFP2016)
      val parsedProtection = Json.fromJson[Protection](json)
      parsedProtection.get shouldEqual openFP2016
    }
  }

  "FP2016 with pension debits json read and write functions" when {
    "be an isomorphic pair" in {
      val json             = Json.toJson(openFP2016WithPensionDebits)
      val parsedProtection = Json.fromJson[Protection](json)
      parsedProtection.get shouldEqual openFP2016WithPensionDebits
    }
  }

  "IP2016 json read and write functions" when {
    "be an isomorphic pair" in {
      val json             = Json.toJson(openIP2016)
      val parsedProtection = Json.fromJson[Protection](json)
      parsedProtection.get shouldEqual openIP2016
    }
  }

  "IP2014 json read and write functions" when {
    "be an isomorphic pair" in {
      val json             = Json.toJson(openIP2014)
      val parsedProtection = Json.fromJson[Protection](json)
      parsedProtection.get shouldEqual openIP2014
    }
  }

  "FP2014 json read and write functions" when {
    "be an isomorphic pair" in {
      val json             = Json.toJson(openFP2014)
      val parsedProtection = Json.fromJson[Protection](json)
      parsedProtection.get shouldEqual openFP2014
    }
  }

  "Primary protection json read and write functions" when {
    "be an isomorphic pair" in {
      val json             = Json.toJson(openPrimary)
      val parsedProtection = Json.fromJson[Protection](json)
      parsedProtection.get shouldEqual openPrimary
    }
  }

  "Dormant enhanced protection json read and write functions" when {
    "be an isomorphic pair" in {
      val json             = Json.toJson(dormantEnhanced)
      val parsedProtection = Json.fromJson[Protection](json)
      parsedProtection.get shouldEqual dormantEnhanced
    }
  }

}
