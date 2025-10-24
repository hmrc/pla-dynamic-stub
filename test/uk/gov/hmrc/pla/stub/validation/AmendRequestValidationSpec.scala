/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.pla.stub.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.pla.stub.model.hip.{
  AmendProtectionLifetimeAllowanceType,
  AmendProtectionRequestStatus,
  HipProtection,
  LifetimeAllowanceProtectionRecord,
  ProtectionStatus,
  ProtectionType
}
import uk.gov.hmrc.pla.stub.validation.AmendRequestValidationError._

class AmendRequestValidationSpec extends AnyWordSpec with Matchers {

  val nino         = "AA123456A"
  val protectionId = 1
  val sequence     = 1

  val testRequest = LifetimeAllowanceProtectionRecord(
    `type` = AmendProtectionLifetimeAllowanceType.IndividualProtection2014,
    certificateDate = Some("2025-10-24"),
    certificateTime = Some("123330"),
    status = AmendProtectionRequestStatus.Open,
    protectionReference = Some("PSA123456789012B"),
    relevantAmount = 1_375_000,
    preADayPensionInPaymentAmount = 375_000,
    postADayBenefitCrystallisationEventAmount = 375_000,
    uncrystallisedRightsAmount = 375_000,
    nonUKRightsAmount = 375_000,
    pensionDebitAmount = None,
    pensionDebitEnteredAmount = Some(1_000),
    notificationIdentifier = None,
    protectedAmount = Some(1_375_000),
    pensionDebitStartDate = Some("2025-10-24"),
    pensionDebitTotalAmount = Some(125_000)
  )

  val testAmendmentTarget = HipProtection(
    nino = nino,
    id = protectionId,
    sequence = sequence,
    status = ProtectionStatus.Open,
    `type` = ProtectionType.IndividualProtection2014,
    relevantAmount = 1_375_000,
    preADayPensionInPaymentAmount = 375_000,
    postADayBenefitCrystallisationEventAmount = 375_000,
    uncrystallisedRightsAmount = 375_000,
    nonUKRightsAmount = 375_000,
    certificateDate = Some("2025-10-24"),
    certificateTime = Some("123330"),
    protectionReference = Some("PSA123456789012B"),
    pensionDebitAmount = None,
    pensionDebitEnteredAmount = Some(1_000),
    protectedAmount = Some(1_375_000),
    pensionDebitStartDate = Some("2025-10-24"),
    pensionDebitTotalAmount = Some(125_000)
  )

  "validateRequest" should {
    "return Right containing the request body" when {
      "request is valid" in {
        AmendRequestValidation.validateRequest(testRequest) shouldBe Right(testRequest)
      }
    }

    "return Left".that {
      "contains IncorrectRelevantAmount" when {
        "relevant amount is too high" in {
          val request = testRequest.copy(
            relevantAmount = 1_375_001
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(IncorrectRelevantAmount(1_375_001, 1_375_000))
        }

        "relevant amount is too low" in {
          val request = testRequest.copy(
            relevantAmount = 1_374_999
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(IncorrectRelevantAmount(1_374_999, 1_375_000))
        }
      }

      "contains IncompletePensionDebit" when {
        "pensionDebitStartDate is present, but pensionDebitEnteredAmount is missing" in {
          val request = testRequest.copy(
            pensionDebitEnteredAmount = None
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(IncompletePensionDebit)
        }

        "pensionDebitEnteredAmount is present, but pensionDebitStartDate is missing" in {
          val request = testRequest.copy(
            pensionDebitStartDate = None
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(IncompletePensionDebit)
        }
      }

      "contains RelevantAmountNotPositive" when {
        "relevantAmount is negative" in {
          val request = testRequest.copy(
            relevantAmount = -1,
            nonUKRightsAmount = -1_000_001
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(RelevantAmountNotPositive)
        }
      }

      "contains NonUKRightsAmountNotPositive" when {
        "nonUKRightsAmount is negative" in {
          val request = testRequest.copy(
            relevantAmount = 999_999,
            nonUKRightsAmount = -1
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(NonUKRightsAmountNotPositive)
        }
      }

      "contains PostADayBenefitCrystallisationEventAmountNotPositive" when {
        "postADayBenefitCrystallisationEventAmount is negative" in {
          val request = testRequest.copy(
            relevantAmount = 999_999,
            postADayBenefitCrystallisationEventAmount = -1
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(
            PostADayBenefitCrystallisationEventAmountNotPositive
          )
        }
      }

      "contains PreADayPensionInPaymentAmountNotPositive" when {
        "preADayPensionInPaymentAmount is negative" in {
          val request = testRequest.copy(
            relevantAmount = 999_999,
            preADayPensionInPaymentAmount = -1
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(PreADayPensionInPaymentAmountNotPostive)
        }
      }

      "contains UncrystallisedRightsAmountNotPositive" when {
        "uncrystallisedRightsAmount is negative" in {
          val request = testRequest.copy(
            relevantAmount = 999_999,
            uncrystallisedRightsAmount = -1
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(UncrystallisedRightsAmountNotPositive)
        }
      }

      "contains PensionDebitEnteredAmountNotPositive" when {
        "pensionDebitEnteredAmount is negative" in {
          val request = testRequest.copy(
            pensionDebitEnteredAmount = Some(-1)
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(PensionDebitEnteredAmountNotPositive)
        }
      }

      "contains CertificateDateInvalid" when {
        "certificateDate is not valid date in YYYY-MM-DD" in {
          val request = testRequest.copy(
            certificateDate = Some("24/10/2025")
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(CertificateDateInvalid)
        }
      }

      "contains CertificateTimeInvalid" when {
        "certificateTime is not valid time in HHMMSS" in {
          val request = testRequest.copy(
            certificateTime = Some("12:33:30")
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(CertificateTimeInvalid)
        }
      }

      "contains PensionDebitStartDateInvalid" when {
        "pensionDebitStartDate is not valid date in YYYY-MM-DD" in {
          val request = testRequest.copy(
            pensionDebitStartDate = Some("24/10/2025")
          )

          AmendRequestValidation.validateRequest(request) shouldBe Left(PensionDebitStartDateInvalid)
        }
      }
    }
  }

  "validateRequestAgainstTarget" should {
    "return Right containing the amendment target" when {
      "request is valid for amendment target" in {
        AmendRequestValidation.validateRequestAgainstTarget(testRequest, testAmendmentTarget, sequence) shouldBe Right(
          testAmendmentTarget
        )
      }
    }

    "return Left".that {
      "contains ProtectionTypeDoesNotMatch" when {
        "protection type does not match target protection" in {
          val request = testRequest.copy(
            `type` = AmendProtectionLifetimeAllowanceType.IndividualProtection2014LTA
          )

          AmendRequestValidation.validateRequestAgainstTarget(request, testAmendmentTarget, sequence) shouldBe Left(
            ProtectionTypeDoesNotMatch
          )
        }
      }

      "contains ProtectionSequenceDoesNotMatch" when {
        "sequence does not match target protection" in {
          AmendRequestValidation.validateRequestAgainstTarget(
            testRequest,
            testAmendmentTarget,
            sequence + 1
          ) shouldBe Left(ProtectionSequenceDoesNotMatch)
        }
      }

      "contains PensionDebitTotalAmountDoesNotMatch" when {
        "pension debit total amount does not match" in {
          val request = testRequest.copy(
            pensionDebitTotalAmount = Some(0)
          )

          AmendRequestValidation.validateRequestAgainstTarget(request, testAmendmentTarget, sequence) shouldBe Left(
            PensionDebitTotalAmountDoesNotMatch
          )
        }
      }
    }
  }

}
