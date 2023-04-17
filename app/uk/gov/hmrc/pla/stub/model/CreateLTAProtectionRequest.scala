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

import play.api.libs.json.{Json, Format}


case class CreateLTAProtectionRequest(
  nino: String,
  protection: CreateLTAProtectionRequest.ProtectionDetails,
  pensionDebits: Option[List[PensionDebit]] = None)

object CreateLTAProtectionRequest {
  implicit val createLTARequestFormat: Format[CreateLTAProtectionRequest] = Json.format[CreateLTAProtectionRequest]
  implicit lazy val protectionDetailsFormat: Format[ProtectionDetails] = Json.format[ProtectionDetails]

  case class ProtectionDetails(
                                `type`: Int,
                                relevantAmount: Option[Double] = None,
                                preADayPensionInPayment: Option[Double] = None,
                                postADayBCE: Option[Double] = None,
                                uncrystallisedRights: Option[Double] = None,
                                nonUKRights: Option[Double] = None,
                                pensionDebitAmount: Option[Double] = None,
                                pensionDebitEnteredAmount: Option[Double] = None,
                                pensionDebitStartDate: Option[String] = None,
                                pensionDebitTotalAmount: Option[Double]=None,
                                protectedAmount: Option[Double] = None) {

    import uk.gov.hmrc.pla.stub.model.Protection.Type._

    def requestedType: Option[Value] = `type` match {
      case 1 => Some(FP2016)
      case 2 => Some(IP2014)
      case 3 => Some(IP2016)
      case _ => None
    }
  }

}
