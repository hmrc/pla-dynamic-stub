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

import org.scalacheck.Gen
import uk.gov.hmrc.pla.stub.model.hip.{Protection, ProtectionStatus, ProtectionType}
import uk.gov.hmrc.pla.stub.model.{DateModel, Protections, TimeModel}
import uk.gov.hmrc.smartstub.*
import uk.gov.hmrc.smartstub.Enumerable.instances.ninoEnumNoSpaces

import java.time.LocalTime
import java.time.format.DateTimeFormatter.*

object Generator {

  /** "^[1-9A][0-9]{6}[ABCDEFHXJKLMNYPQRSTZW]|(IP14|IP16|FP16)[0-9]{10}[ABCDEFGHJKLMNPRSTXYZ]$^"
    */
  private val genProtectionReference: Gen[String] = {
    val refOne: Gen[String] = for {
      prefix <- Gen.oneOf("A123456789".toList)
      number <- pattern"999999".gen
      suffix <- Gen.oneOf("ABCDEFHXJKLMNYPQRSTZW".toList)
    } yield s"$prefix$number$suffix"

    val refTwo: Gen[String] = for {
      prefix <- Gen.oneOf("IP14", "IP16", "FP16")
      number <- pattern"9999999999".gen
      suffix <- Gen.oneOf("ABCDEFGHJKLMNPRSTXYZ".toList)
    } yield s"$prefix$number$suffix"

    Gen.oneOf(refOne, refTwo)
  }

  /** "^PSA[0-9]{8}[A-Z]?$^"
    */
  val genPensionSchemeAdministratorCheckReference: Gen[String] =
    pattern"99999999Z".map("PSA" + _)

  private val genDate: Gen[DateModel] = Gen.date(2014, 2017).map(DateModel(_))

  private val genTime: Gen[TimeModel] = Gen.choose(0, 24 * 60 * 60).map { x =>
    TimeModel(
      LocalTime.parse(
        {
          BigInt(1000000000L) * x
        }.toString,
        ofPattern("N")
      )
    )
  }

  private val genMoney: Gen[Int] = Gen.choose(1, 1000000000)

  private val genPercentage: Gen[Int] = Gen.choose(0, 100)

  private val genFactor: Gen[Double] = Gen.choose[Double](0, 1)

  private val genId: Gen[Int] = Gen.choose(1, 7)

  private val genVersion: Gen[Int] = Gen.choose(1, 5)

  private val genStatus: Gen[ProtectionStatus] = Gen.oneOf(ProtectionStatus.values.toSeq)

  private val genProtectionType: Gen[ProtectionType] = Gen.oneOf(ProtectionType.values.toSeq)

  private def genProtection(nino: String): Gen[Protection] =
    for {
      id         <- genId
      version    <- genVersion
      protection <- genProtection(nino, id, version)
    } yield protection

  private def genProtection(nino: String, id: Long, sequence: Int): Gen[Protection] =
    for {
      status                                    <- genStatus
      protectionType                            <- genProtectionType
      relevantAmount                            <- genMoney
      preADayPensionInPaymentAmount             <- genMoney
      postADayBenefitCrystallisationEventAmount <- genMoney
      uncrystallisedRightsAmount                <- genMoney
      nonUKRightsAmount                         <- genMoney
      certificateDate                           <- genDate
      certificateTime                           <- genTime
      protectionReference                       <- genProtectionReference.sometimes
      pensionDebitAmount                        <- genMoney.sometimes
      pensionDebitEnteredAmount                 <- genMoney.sometimes
      protectedAmount                           <- genMoney.sometimes
      pensionDebitStartDate                     <- genDate.sometimes
      pensionDebitTotalAmount                   <- genMoney.sometimes
      lumpSumAmount                             <- genMoney.sometimes
      enhancementFactor                         <- genFactor.sometimes
      lumpSumPercentage                         <- genPercentage.sometimes
    } yield Protection(
      nino = nino,
      id = id,
      sequence = sequence,
      status = status,
      `type` = protectionType,
      relevantAmount = relevantAmount,
      preADayPensionInPaymentAmount = preADayPensionInPaymentAmount,
      postADayBenefitCrystallisationEventAmount = postADayBenefitCrystallisationEventAmount,
      uncrystallisedRightsAmount = uncrystallisedRightsAmount,
      nonUKRightsAmount = nonUKRightsAmount,
      certificateDate = certificateDate,
      certificateTime = certificateTime,
      protectionReference = protectionReference,
      pensionDebitAmount = pensionDebitAmount,
      pensionDebitEnteredAmount = pensionDebitEnteredAmount,
      protectedAmount = protectedAmount,
      pensionDebitStartDate = pensionDebitStartDate,
      pensionDebitTotalAmount = pensionDebitTotalAmount,
      lumpSumAmount = lumpSumAmount,
      enhancementFactor = enhancementFactor,
      lumpSumPercentage = lumpSumPercentage
    )

  private def genProtections(nino: String): Gen[Protections] = for {
    pensionSchemeAdministratorCheckReference <- genPensionSchemeAdministratorCheckReference.sometimes
    protections                              <- Gen.choose(2, 5).flatMap(n => Gen.listOfN(n, genProtection(nino)))
  } yield Protections(
    nino = nino,
    pensionSchemeAdministratorCheckReference = pensionSchemeAdministratorCheckReference,
    protections = protections
  )

  val protectionsStore: PersistentGen[String, Protections] = genProtections("").asMutable[String]

}
