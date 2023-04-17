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

package uk.gov.hmrc.pla.stub.model

import play.api.libs.json.{Format, Json}


case class UpdateLTAProtectionRequest(
  nino: String,
  protection: UpdateLTAProtectionRequest.ProtectionDetails,
  pensionDebits: Option[List[PensionDebit]] = None)


object UpdateLTAProtectionRequest {
  implicit val updateLTARequestFormat: Format[UpdateLTAProtectionRequest] = Json.format[UpdateLTAProtectionRequest]
  implicit lazy val protectionDetailsFormat: Format[ProtectionDetails] = Json.format[ProtectionDetails]

  case class ProtectionDetails(
    `type`: Int,
    status: Int,
    version: Int,
    relevantAmount: Double,
    preADayPensionInPayment: Double,
    postADayBCE: Double,
    uncrystallisedRights: Double,
    pensionDebitTotalAmount: Option[Double] = None,
    nonUKRights: Double,
    withdrawnDate: Option[String] = None) {

    import uk.gov.hmrc.pla.stub.model.Protection.Type._

    def requestedType: Option[Value] = `type` match {
      case 2 => Some(IP2014)
      case 3 => Some(IP2016)
      case _ => None
    }
  }

}
