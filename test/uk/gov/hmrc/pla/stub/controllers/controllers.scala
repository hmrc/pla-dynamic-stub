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

package uk.gov.hmrc.pla.stub

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.pla.stub.model.hip.{
  AmendProtectionLifetimeAllowanceType,
  AmendProtectionRequestStatus,
  AmendProtectionResponseStatus
}
import uk.gov.hmrc.pla.stub.model.hip.AmendProtectionLifetimeAllowanceType.IndividualProtection2014

package object controllers {

  val successfulProtectionsRetrieveOutput: JsValue =
    Json.parse(
      """
        |{
        |    "nino": "RC966967C",
        |    "protections": [
        |        {
        |            "nino": "RC966967C",
        |            "id": 2,
        |            "version": 2,
        |            "type": 2,
        |            "status": 1,
        |            "notificationID": 28,
        |            "notificationMsg": "peymPuEzcqfxeuLqmjtrwrqsLxnUnud",
        |            "protectionReference": "FP166913560729C",
        |            "certificateTime": "02:12:18",
        |            "relevantAmount": 1250000,
        |            "protectedAmount": 4740961.08,
        |            "preADayPensionInPayment": 250000,
        |            "postADayBCE": 250000,
        |            "uncrystallisedRights": 500000,
        |            "nonUKRights": 250000,
        |            "pensionDebitTotalAmount": 4348894.14,
        |            "pensionDebits": [
        |                {
        |                    "pensionDebitEnteredAmount": 400,
        |                    "pensionDebitStartDate": "2015-05-25"
        |                },
        |                {
        |                    "pensionDebitEnteredAmount": 200,
        |                    "pensionDebitStartDate": "2015-05-24"
        |                },
        |                {
        |                    "pensionDebitEnteredAmount": 100,
        |                    "pensionDebitStartDate": "2015-05-23"
        |                }
        |            ]
        |        },
        |        {
        |            "nino": "RC966967C",
        |            "id": 4,
        |            "version": 1,
        |            "type": 1,
        |            "status": 1,
        |            "notificationID": 11,
        |            "notificationMsg": "bjaX",
        |            "protectionReference": "FP168307204247H",
        |            "certificateDate": "2016-07-02",
        |            "certificateTime": "14:40:52",
        |            "relevantAmount": 1250000,
        |            "protectedAmount": 5797723.42,
        |            "preADayPensionInPayment": 250000,
        |            "postADayBCE": 250000,
        |            "uncrystallisedRights": 500000,
        |            "nonUKRights": 250000,
        |            "pensionDebits": [
        |                {
        |                    "pensionDebitEnteredAmount": 400,
        |                    "pensionDebitStartDate": "2015-05-25"
        |                },
        |                {
        |                    "pensionDebitEnteredAmount": 200,
        |                    "pensionDebitStartDate": "2015-05-24"
        |                },
        |                {
        |                    "pensionDebitEnteredAmount": 100,
        |                    "pensionDebitStartDate": "2015-05-23"
        |                }
        |            ]
        |        }
        |    ]
        |}
    """.stripMargin
    )

  val successfulEmptyProtectionsRetrieveOutput: JsValue =
    Json.parse(
      """
        |{
        |    "nino": "AA000000A",
        |    "protections":[]
        |}
    """.stripMargin
    )

  val successfulProtectionRetrieveOutput: JsValue =
    Json.parse(
      """
        |{
        |    "nino": "RC966967C",
        |    "id": 1,
        |    "version": 1,
        |    "type": 1,
        |    "status": 1,
        |    "notificationID": 11,
        |    "notificationMsg": "bjaX",
        |    "protectionReference": "FP168307204247H",
        |    "certificateDate": "2016-07-02",
        |    "certificateTime": "14:40:52",
        |    "relevantAmount": 1250000,
        |    "protectedAmount": 5797723.42,
        |    "preADayPensionInPayment": 250000,
        |    "postADayBCE": 250000,
        |    "uncrystallisedRights": 500000,
        |    "nonUKRights": 250000,
        |    "pensionDebits": [
        |        {
        |            "pensionDebitEnteredAmount": 400,
        |            "pensionDebitStartDate": "2015-05-25"
        |        },
        |        {
        |            "pensionDebitEnteredAmount": 200,
        |            "pensionDebitStartDate": "2015-05-24"
        |        },
        |        {
        |            "pensionDebitEnteredAmount": 100,
        |            "pensionDebitStartDate": "2015-05-23"
        |        }
        |    ]
        |}
    """.stripMargin
    )

  val validCreateProtectionRequestInput: JsValue =
    Json.parse(
      """{
        |  "nino": "RC966967C",
        |  "protection": {
        |    "type": 2,
        |    "status":1,
        |    "relevantAmount": 1250000,
        |    "preADayPensionInPayment": 250000,
        |    "postADayBCE": 250000,
        |    "uncrystallisedRights": 500000,
        |    "nonUKRights": 250000,
        |    "pensionDebitAmount": 0,
        |    "protectedAmount": 200,
        |    "pensionDebitEnteredAmount": 150,
        |    "pensionDebitStartDate": "2015-05-25",
        |    "pensionDebitTotalAmount": 15000
        |  },
        |  "pensionDebits": [
        |    {
        |      "pensionDebitEnteredAmount": 400.00,
        |      "pensionDebitStartDate": "2015-05-25"
        |    },
        |    {
        |      "pensionDebitEnteredAmount": 200.00,
        |      "pensionDebitStartDate": "2015-05-24"
        |    },
        |    {
        |      "pensionDebitEnteredAmount": 100.00,
        |      "pensionDebitStartDate": "2015-05-23"
        |    }
        |  ]
        |}
    """.stripMargin
    )

  val validCreateProtectionResponseOutput: JsValue =
    Json.parse(
      """{
        |    "nino": "RC966967C",
        |    "protection": {
        |        "nino": "RC966967C",
        |        "id": 2,
        |        "version": 2,
        |        "type": 2,
        |        "status": 1,
        |        "notificationID": 28,
        |        "notificationMsg": "peymPuEzcqfxeuLqmjtrwrqsLxnUnud",
        |        "protectionReference": "FP166913560729C",
        |        "certificateTime": "02:12:18",
        |        "relevantAmount": 1250000,
        |        "protectedAmount": 4740961.08,
        |        "preADayPensionInPayment": 250000,
        |        "postADayBCE": 250000,
        |        "uncrystallisedRights": 500000,
        |        "nonUKRights": 250000,
        |        "pensionDebitTotalAmount": 4348894.14,
        |        "pensionDebits": [
        |            {
        |                "pensionDebitEnteredAmount": 400,
        |                "pensionDebitStartDate": "2015-05-25"
        |            },
        |            {
        |                "pensionDebitEnteredAmount": 200,
        |                "pensionDebitStartDate": "2015-05-24"
        |            },
        |            {
        |                "pensionDebitEnteredAmount": 100,
        |                "pensionDebitStartDate": "2015-05-23"
        |            }
        |        ]
        |    }
        |}
    """.stripMargin
    )

  val validUpdateProtectionRequestInput: JsValue =
    Json.parse(
      """{
        |  "nino": "RC966967C",
        |  "protection": {
        |  	"id" :5,
        |  	"version" : 4,
        |    "type": 2,
        |    "status":1,
        |    "protectionReference": "IP141234567890C",
        |    "notificationID": 5,
        |    "relevantAmount": 1250000,
        |    "preADayPensionInPayment": 250000,
        |    "postADayBCE": 250000,
        |    "uncrystallisedRights": 450000,
        |    "nonUKRights": 250000,
        |    "pensionDebitAmount": 0,
        |    "protectedAmount": 200,
        |    "pensionDebitEnteredAmount": 150,
        |    "pensionDebitStartDate": "2015-05-25",
        |    "pensionDebitTotalAmount": 15000
        |  },
        |  "pensionDebits": [
        |    {
        |      "pensionDebitEnteredAmount": 400.00,
        |      "pensionDebitStartDate": "2015-05-25"
        |    },
        |    {
        |      "pensionDebitEnteredAmount": 200.00,
        |      "pensionDebitStartDate": "2015-05-24"
        |    },
        |    {
        |      "pensionDebitEnteredAmount": 100.00,
        |      "pensionDebitStartDate": "2015-05-23"
        |    }
        |  ]
        |}
    """.stripMargin
    )

  val validUpdateProtectionResponseOutput: JsValue =
    Json.parse(
      """{
        |    "nino": "RC966967C",
        |    "pensionSchemeAdministratorCheckReference": "PSA94965839X",
        |    "protection": {
        |        "nino": "RC966967C",
        |        "id": 5,
        |        "version": 4,
        |        "type": 2,
        |        "status": 1,
        |        "notificationID": 26,
        |        "protectionReference": "A622783H",
        |        "relevantAmount": 1250000,
        |        "preADayPensionInPayment": 250000,
        |        "postADayBCE": 250000,
        |        "uncrystallisedRights": 450000,
        |        "nonUKRights": 250000,
        |        "pensionDebits": [
        |            {
        |                "pensionDebitEnteredAmount": 400,
        |                "pensionDebitStartDate": "2015-05-25"
        |            },
        |            {
        |                "pensionDebitEnteredAmount": 200,
        |                "pensionDebitStartDate": "2015-05-24"
        |            },
        |            {
        |                "pensionDebitEnteredAmount": 100,
        |                "pensionDebitStartDate": "2015-05-23"
        |            }
        |        ]
        |    }
        |}
    """.stripMargin
    )

  val validAmendProtectionRequestInput: JsValue = validAmendProtectionRequestInputWith()

  def validAmendProtectionRequestInputWith(
      protectionType: AmendProtectionLifetimeAllowanceType =
        AmendProtectionLifetimeAllowanceType.IndividualProtection2014,
      certificateDate: String = "2025-08-15",
      certificateTime: String = "123456",
      status: AmendProtectionRequestStatus = AmendProtectionRequestStatus.Open,
      protectionReference: String = "IP123456789012B",
      relevantAmount: Int = 39500,
      preADayPensionInPaymentAmount: Int = 1500,
      postADayBenefitCrystallisationEventAmount: Int = 2500,
      uncrystallisedRightsAmount: Int = 75500,
      nonUKRightsAmount: Int = 0,
      pensionDebitAmount: Int = 25000,
      pensionDebitEnteredAmount: Int = 25000,
      notificationIdentifier: Int = 3,
      protectedAmount: Int = 120000,
      pensionDebitStartDate: String = "2026-07-09",
      pensionDebitTotalAmount: Int = 40000
  ): JsValue =
    Json.parse(s"""{
                  |  "lifetimeAllowanceProtectionRecord": {
                  |    "type": "$protectionType",
                  |    "certificateDate": "$certificateDate",
                  |    "certificateTime": "$certificateTime",
                  |    "status": "$status",
                  |    "protectionReference": "$protectionReference",
                  |    "relevantAmount": $relevantAmount,
                  |    "preADayPensionInPaymentAmount": $preADayPensionInPaymentAmount,
                  |    "postADayBenefitCrystallisationEventAmount": $postADayBenefitCrystallisationEventAmount,
                  |    "uncrystallisedRightsAmount": $uncrystallisedRightsAmount,
                  |    "nonUKRightsAmount": $nonUKRightsAmount,
                  |    "pensionDebitAmount": $pensionDebitAmount,
                  |    "pensionDebitEnteredAmount": $pensionDebitEnteredAmount,
                  |    "notificationIdentifier": $notificationIdentifier,
                  |    "protectedAmount": $protectedAmount,
                  |    "pensionDebitStartDate": "$pensionDebitStartDate",
                  |    "pensionDebitTotalAmount": $pensionDebitTotalAmount
                  |  }
                  |}
    """.stripMargin)

  val validHipAmendProtectionResponse: JsValue = validHipAmendProtectionResponseWith()

  def validHipAmendProtectionResponseWith(
      protectionType: AmendProtectionLifetimeAllowanceType = IndividualProtection2014,
      identifier: Long = 12960000000123L,
      sequenceNumber: Int = 2,
      certificateDate: String = "2025-08-15",
      certificateTime: String = "123456",
      status: AmendProtectionResponseStatus = AmendProtectionResponseStatus.Withdrawn,
      protectionReference: String = "IP123456789012B",
      relevantAmount: Int = 27000,
      preADayPensionInPaymentAmount: Int = 1500,
      postADayBenefitCrystallisationEventAmount: Int = 2500,
      uncrystallisedRightsAmount: Int = 75500,
      nonUKRightsAmount: Int = 0,
      notificationIdentifier: Int = 6,
      protectedAmount: Int = 27000,
      pensionDebitTotalAmount: Int = 52500
  ): JsValue =
    Json.parse(
      s"""{
         |  "updatedLifetimeAllowanceProtectionRecord": {
         |     "identifier": $identifier,
         |     "sequenceNumber": $sequenceNumber,
         |     "type": "$protectionType",
         |     "certificateDate": "$certificateDate",
         |     "certificateTime": "$certificateTime",
         |     "status": "$status",
         |     "protectionReference": "$protectionReference",
         |     "relevantAmount": $relevantAmount,
         |     "preADayPensionInPaymentAmount": $preADayPensionInPaymentAmount,
         |     "postADayBenefitCrystallisationEventAmount": $postADayBenefitCrystallisationEventAmount,
         |     "uncrystallisedRightsAmount": $uncrystallisedRightsAmount,
         |     "nonUKRightsAmount": $nonUKRightsAmount,
         |     "notificationIdentifier": $notificationIdentifier,
         |     "protectedAmount": $protectedAmount,
         |     "pensionDebitTotalAmount": $pensionDebitTotalAmount
         |  }
         |}
    """.stripMargin
    )

  val invalidAmendProtectionRequestInput: JsValue =
    Json.parse("""{
                 |  "lifetimeAllowanceProtectionRecord": {
                 |    "type": "INDIVIDUAL PROTECTION 2014",
                 |    "certificateDate": "2025-08-15",
                 |    "certificateTime": "123456",
                 |    "status": "CLOSED",
                 |    "protectionReference": "IP123456789012B",
                 |    "relevantAmount": 105000,
                 |    "preADayPensionInPaymentAmount": 1500,
                 |    "postADayBenefitCrystallisationEventAmount": 2500,
                 |    "uncrystallisedRightsAmount": 75500,
                 |    "nonUKRightsAmount": 0,
                 |    "pensionDebitAmount": 25000,
                 |    "pensionDebitEnteredAmount": 25000,
                 |    "notificationIdentifier": 3,
                 |    "protectedAmount": 120000,
                 |    "pensionDebitStartDate": "2026-07-09",
                 |    "pensionDebitTotalAmount": 40000
                 |  }
                 |}
    """.stripMargin)

}
