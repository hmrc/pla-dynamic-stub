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

import play.api.libs.json.{JsPath, Json, JsonValidationError}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, NotFound, Status, UnprocessableEntity}
import uk.gov.hmrc.pla.stub.model.Error
import scala.collection.Seq

sealed abstract class AmendRequestValidationError(val status: Status, val message: String) {
  def toResult: Result = status(Json.toJson(Error(message)))
}

object AmendRequestValidationError {

  case class JsonValidationFailed(errors: Seq[(JsPath, Seq[JsonValidationError])])
      extends AmendRequestValidationError(
        BadRequest,
        "failed validation with errors: " + errors
      )

  case class IncorrectRelevantAmount(relevantAmount: Int, calculatedRelevantAmount: Int)
      extends AmendRequestValidationError(
        UnprocessableEntity,
        s"The specified Relevant Amount $relevantAmount is not the sum of the specified breakdown amounts $calculatedRelevantAmount (non UK Rights + Post A Day BCE + Pre A Day Pensions In Payment + Uncrystallised Rights - Pension Debit Total Amount)"
      )

  case object IncompletePensionDebit
      extends AmendRequestValidationError(
        UnprocessableEntity,
        "incomplete pension debits information - require either both, or neither of pension debit start date and pension debit entered amount"
      )

  case object RelevantAmountNotPositive
      extends AmendRequestValidationError(
        BadRequest,
        "relevant amount must be positive"
      )

  case object NonUKRightsAmountNotPositive
      extends AmendRequestValidationError(
        BadRequest,
        "non UK rights amount must be positive"
      )

  case object PostADayBenefitCrystallisationEventAmountNotPositive
      extends AmendRequestValidationError(
        BadRequest,
        "post A day benefit crystallisation event amount must be positive"
      )

  case object PreADayPensionInPaymentAmountNotPostive
      extends AmendRequestValidationError(
        BadRequest,
        "pre A day pension in payment amount must be positive"
      )

  case object UncrystallisedRightsAmountNotPositive
      extends AmendRequestValidationError(
        BadRequest,
        "uncrystallised rights amount must be positive"
      )

  case object PensionDebitEnteredAmountNotPositive
      extends AmendRequestValidationError(
        BadRequest,
        "pension debit entered amount must be positive"
      )

  case object CertificateDateInvalid
      extends AmendRequestValidationError(
        BadRequest,
        "invalid certificate date"
      )

  case object CertificateTimeInvalid
      extends AmendRequestValidationError(
        BadRequest,
        "invalid certificate time"
      )

  case object PensionDebitStartDateInvalid
      extends AmendRequestValidationError(
        BadRequest,
        "invalid pension debit start date"
      )

  case object ProtectionNotFound
      extends AmendRequestValidationError(
        NotFound,
        "protection to amend not found"
      )

  case object ProtectionTypeDoesNotMatch
      extends AmendRequestValidationError(
        BadRequest,
        "specified protection type does not match that of the protection to be amended"
      )

  case object ProtectionSequenceDoesNotMatch
      extends AmendRequestValidationError(
        BadRequest,
        "specified protection sequence does not match that of the protection to be amended"
      )

  case object PensionDebitTotalAmountDoesNotMatch
      extends AmendRequestValidationError(
        BadRequest,
        "specified pension debit total amount does not match that of the protection to be amended"
      )

}
