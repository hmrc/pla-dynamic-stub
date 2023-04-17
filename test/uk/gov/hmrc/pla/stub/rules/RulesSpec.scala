/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.pla.stub.rules

import java.time.LocalDateTime
import java.util.Random

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.pla.stub.model.Protection

object Generator {
  import uk.gov.hmrc.domain.Generator

  val rand = new Random()
  val ninoGenerator = new Generator(rand)

  def randomNino: String = ninoGenerator.nextNino.nino.replaceFirst("MA", "AA")
  def randomProtectionID = rand.nextLong
  def randomFP16ProtectionReference=("FP16" + Math.abs(rand.nextLong)).substring(0,9) + "C"
  def randomIP16ProtectionReference=("IP16" + Math.abs(rand.nextLong)).substring(0,9) + "B"
  def randomIP14ProtectionReference=("IP14" + Math.abs(rand.nextLong)).substring(0,9) + "A"
  def randomOlderProtectionReference=("A" +  Math.abs(rand.nextLong)).substring(0,5) + "A"
}

object Protections {

  import Generator._

  val currDate = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
  val currTime = LocalDateTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)

  val openFP2016=Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.FP2016),
    status=Protection.extractedStatus(Protection.Status.Open),
    notificationID=Some(22),
    notificationMsg=None,
    protectionReference=Some(randomFP16ProtectionReference),
    version = 1)

  val openIP2016=Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.IP2016),
    status=Protection.extractedStatus(Protection.Status.Open),
    notificationID=Some(12),
    notificationMsg=None,
    protectionReference=Some(randomFP16ProtectionReference),
    version = 1)

  val openFP2014=Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.FP2014),
    status=Protection.extractedStatus(Protection.Status.Open),
    notificationID=None,
    notificationMsg=None,
    protectionReference=Some(randomFP16ProtectionReference),
    version = 1)


  val openIP2014=Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.IP2014),
    status=Protection.extractedStatus(Protection.Status.Open),
    notificationID=None,
    notificationMsg=None,
    protectionReference=Some(randomFP16ProtectionReference),
    version = 1)

  val openPrimary=Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.Primary),
    status=Protection.extractedStatus(Protection.Status.Open),
    notificationID=None,
    notificationMsg=None,
    protectionReference=Some(randomOlderProtectionReference),
    version = 1)

  val openFixed=Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.Fixed),
    status=Protection.extractedStatus(Protection.Status.Open),
    notificationID=None,
    notificationMsg=None,
    protectionReference=Some(randomOlderProtectionReference),
    version = 1)

  val openEnhanced=Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.Enhanced),
    status=Protection.extractedStatus(Protection.Status.Open),
    notificationID=None,
    notificationMsg=None,
    protectionReference=Some(randomOlderProtectionReference),
    version = 1)

  val dormantPrimary=Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.Primary),
    status=Protection.extractedStatus(Protection.Status.Dormant),
    notificationID=None,
    notificationMsg=None,
    protectionReference=Some(randomOlderProtectionReference),
    version = 1)

  val dormantEnhanced=Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.Enhanced),
    status=Protection.extractedStatus(Protection.Status.Dormant),
    notificationID=None,
    notificationMsg=None,
    protectionReference=Some(randomOlderProtectionReference),
    version = 1)

  val rejected = Protection(
    nino=randomNino,
    id=randomProtectionID,
    `type`=Protection.extractedType(Protection.Type.IP2016),
    status=Protection.extractedStatus(Protection.Status.Rejected),
    notificationID=Some(21),
    notificationMsg=None,
    protectionReference=None,
    version = 1)
}

class FP2016ApplicationRulesSpec extends AnyWordSpec with Matchers {

  import Protections._


  "An application for an FP2016 when no protections already exist for the individual" when {
    "return a notification ID of 22" in {
      FP2016ApplicationRules.check(List()) shouldBe 22
    }
  }

  "An application for an FP2016 when an open FP2016 already exists for the individual" when {
    "return a notification ID of 21" in {

      FP2016ApplicationRules.check(List(openFP2016)) shouldBe 21
    }
  }

  "An application for an FP2016 when an open Primary already exists for the individual" when {
    "return a notification ID of 18" in {
      FP2016ApplicationRules.check(List(openPrimary)) shouldBe 18
    }
  }

  "An application for an FP2016 when an open Enhanced already exists for the individual" when {
    "return a notification ID of 17" in {

      FP2016ApplicationRules.check(List(openEnhanced)) shouldBe 17
    }
  }

  "An application for an FP2016 when an open Fixed already exists for the individual" when {
    "return a notification ID of 19" in {
      FP2016ApplicationRules.check(List(openFixed)) shouldBe 19
    }
  }

  "An application for an FP2016 when an open FP2014 already exists for the individual" when {
    "return a notification ID of 20" in {
      FP2016ApplicationRules.check(List(openFP2014)) shouldBe 20
    }
  }

  "An application for an FP2016 when an open IP2014 already exists for the individual" when {
    "return a notification ID of 24" in {
      FP2016ApplicationRules.check(List(openIP2014)) shouldBe 24
    }
  }

  "An application for an FP2016 when an open IP2016 already exists for the individual" when {
    "return a notification ID of 23" in {
      FP2016ApplicationRules.check(List(openIP2016)) shouldBe 23
    }
  }

  "An application for an FP2016 when only a dormant Primary protection already exists for the individual" when {
    "return a notification ID of 18" in {
      FP2016ApplicationRules.check(List(dormantPrimary)) shouldBe 18
    }
  }

  "An application for an FP2016 when only a dormant non-Primary protection already exists for the individual" when {
    "return a notification ID of 22" in {
      FP2016ApplicationRules.check(List(dormantEnhanced)) shouldBe 22
    }
  }

  "An application for an FP2016 when only a rejected protection already exists for the individual" when {
    "return a notification ID of 22" in {
      FP2016ApplicationRules.check(List(rejected)) shouldBe 22
    }
  }

  "An application for an FP2016 when a rejected protection plus an open FP2016 already exists for the individual" when {
    "return a notification ID of 21" in {
      FP2016ApplicationRules.check(List(rejected, openFP2016)) shouldBe 21
    }
  }

  "An application for an FP2016 when a dormant non-Primary protection plus an open FP2016 already exists for the individual" when {
    "return a notification ID of 21" in {
      FP2016ApplicationRules.check(List(dormantEnhanced, openFP2016)) shouldBe 21
    }
  }

  "An application for an FP2016 when a rejected application, a dormant non-Primary protection plus an open FP2016 already exists for the individual" when {
    "return a notification ID of 21" in {
      FP2016ApplicationRules.check(List(rejected, dormantEnhanced, openFP2016)) shouldBe 21
    }
  }

  "An application for an IP2016 when no protections already exist for the individual" when {
    "return a notification ID of 12" in {
      IP2016ApplicationRules.check(List()) shouldBe 12
    }
  }
}

class IP2016ApplicationRulesSpec extends AnyWordSpec with Matchers {

  import Protections._

  "An application for an IP2016 when an open FP2016 already exists for the individual" when {
    "return a notification ID of 16" in {
      IP2016ApplicationRules.check(List(openFP2016)) shouldBe 16
    }
  }

  "An application for an IP2016 when a rejected protection plus an open IP2014 already exists for the individual" when {
    "return a notification ID of 10" in {
      IP2016ApplicationRules.check(List(rejected, openIP2014)) shouldBe 10
    }
  }

  "An application for an IP2016 when an open IP2016 already exists for the individual" when {
    "return a notification ID of 11" in {
      IP2016ApplicationRules.check(List(openIP2016)) shouldBe 11
    }
  }

  "An application for an IP2016 when dormant primary protection already exists for the individual" when {
    "return a notification ID of 9" in {
      IP2016ApplicationRules.check(List(dormantPrimary)) shouldBe 9
    }
  }

  "An application for an IP2016 when open primary protection already exists for the individual" when {
    "return a notification ID of 9" in {
      IP2016ApplicationRules.check(List(openPrimary)) shouldBe 9
    }
  }

  "An application for an IP2016 when openEnhanced protection already exists for the individual" when {
    "return a notification ID of 13" in {
      IP2016ApplicationRules.check(List(openEnhanced)) shouldBe 13
    }
  }

  "An application for an IP2016 when openFixed protection already exists for the individual" when {
    "return a notification ID of 14" in {
      IP2016ApplicationRules.check(List(openFixed)) shouldBe 14
    }
  }

  "An application for an IP2016 when openFP2014 protection already exists for the individual" when {
    "return a notification ID of 15" in {
      IP2016ApplicationRules.check(List(openFP2014)) shouldBe 15
    }
  }
}

class IP2014ApplicationRulesSpec extends AnyWordSpec with Matchers {

  import Protections._

  "An application for an IP2014 when no protections already exist for the individual" when {
    "return a notification ID of 4" in {
      IP2014ApplicationRules.check(List()) shouldBe 4
    }
  }

  "An application for an IP2014 when an open FP2016 already exists for the individual" when {
    "return a notification ID of 8" in {
      IP2014ApplicationRules.check(List(openFP2016)) shouldBe 8
    }
  }

  "An application for an IP2014 when a rejected protection plus an open IP2014 already exists for the individual" when {
    "return a notification ID of 2" in {
      IP2014ApplicationRules.check(List(rejected, openIP2014)) shouldBe 2
    }
  }

  "An application for an IP2014 when an open IP2016 already exists for the individual" when {
    "return a notification ID of 3" in {
      IP2014ApplicationRules.check(List(openIP2016)) shouldBe 3
    }
  }

  "An application for an IP2014 when dormant primary protection already exists for the individual" when {
    "return a notification ID of 4" in {
      IP2014ApplicationRules.check(List(dormantPrimary)) shouldBe 4
    }
  }

  "An application for an IP2014 when open primary protection already exists for the individual" when {
    "return a notification ID of 1" in {
      IP2014ApplicationRules.check(List(openPrimary)) shouldBe 1
    }
  }

  "An application for an IP2014 when openEnhanced protection already exists for the individual" when {
    "return a notification ID of 5" in {
      IP2014ApplicationRules.check(List(openEnhanced)) shouldBe 5
    }
  }

  "An application for an IP2014 when openFixed protection already exists for the individual" when {
    "return a notification ID of 6" in {
      IP2014ApplicationRules.check(List(openFixed)) shouldBe 6
    }
  }

  "An application for an IP2014 when openFP2014 protection already exists for the individual" when {
    "return a notification ID of 7" in {
      IP2014ApplicationRules.check(List(openFP2014)) shouldBe 7
    }
  }
}

class IP2014AmendmentRulesSpec extends AnyWordSpec with Matchers {

  import Protections._

  "An amendment for an IP2014 when no protections already exist for the individual and relevantAmount=<1250000" when {
    "return a notification ID of 25" in {
      IP2014AmendmentRules.check(1250000, List()) shouldBe 25
    }
  }

  "An amendment for an IP2014 when no protections already exist for the individual and relevantAmount>125000" when {
    "return a notification ID of 34" in {
      IP2014AmendmentRules.check(1250001, List()) shouldBe 34
    }
  }

  "An amendment for an IP2014 when open FP2016 already exists for the individual" when {
    "return a notification ID of 29" in {
      IP2014AmendmentRules.check(1250000, List(openFP2016)) shouldBe 29
    }
  }

}

class IP2016AmendmentRulesSpec extends AnyWordSpec with Matchers {

  import Protections._

  "An amendment for an IP2016 when no protections already exist for the individual and relevantAmount=<1000000" when {
    "return a notification ID of 35" in {
      IP2016AmendmentRules.check(1000000, List()) shouldBe 35
    }
  }

  "An amendment for an IP2016 when no protections already exist for the individual and relevantAmount>1000000" when {
    "return a notification ID of 44" in {
      IP2016AmendmentRules.check(1000001, List()) shouldBe 44
    }
  }

  "An amendment for an IP2014 when open FP2016 already exists for the individual" when {
    "return a notification ID of 29" in {
      IP2014AmendmentRules.check(1000000, List(openFP2016)) shouldBe 29
    }
  }

}
