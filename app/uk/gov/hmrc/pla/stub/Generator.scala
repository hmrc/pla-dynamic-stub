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

import cats.implicits._
import org.scalacheck.Gen
import org.scalacheck.cats.implicits._
import uk.gov.hmrc.pla.stub.model.hip.{Protection, ProtectionStatus, ProtectionType}
import uk.gov.hmrc.pla.stub.model.{DateModel, Protections, TimeModel}
import uk.gov.hmrc.smartstub.Enumerable.instances.utrEnum
import uk.gov.hmrc.smartstub.{AdvGen, _}

import java.time.LocalTime
import java.time.format.DateTimeFormatter._

object Generator {

  /** "^[1-9A][0-9]{6}[ABCDEFHXJKLMNYPQRSTZW]|(IP14|IP16|FP16)[0-9]{10}[ABCDEFGHJKLMNPRSTXYZ]$^"
    */
  val refGen: Gen[String] = {
    val refOne = List(
      Gen.oneOf("A" :: {
        1 to 9
      }.map {
        _.toString
      }.toList),
      pattern"999999".gen,
      Gen.oneOf("ABCDEFHXJKLMNYPQRSTZW".toList)
    ).sequence

    val refTwo = List(
      Gen.oneOf("IP14", "IP16", "FP16"),
      pattern"9999999999".gen,
      Gen.oneOf("ABCDEFGHJKLMNPRSTXYZ".toList)
    ).sequence

    Gen.oneOf(refOne, refTwo).map {
      _.mkString
    }
  }

  /** "^PSA[0-9]{8}[A-Z]?$^"
    */
  val pensionSchemeAdministratorCheckReferenceGen: Gen[String] =
    pattern"99999999Z".map("PSA" + _)

  val genDate: Gen[DateModel] = Gen.date(2014, 2017).map(DateModel(_))

  val genTime: Gen[TimeModel] = Gen.choose(0, 24 * 60 * 60).map { x =>
    TimeModel(
      LocalTime.parse(
        {
          BigInt(1000000000L) * x
        }.toString,
        ofPattern("N")
      )
    )
  }

  val genMoney: Gen[Option[Int]] =
    Gen
      .choose(1, 1000000000)
      .sometimes

  val genPercentage: Gen[Int] = Gen.choose(0, 100)

  val genFactor: Gen[Double] = Gen.choose[Double](0, 1)

  def genProtection(nino: String): Gen[Protection] =
    for {
      id         <- Gen.choose(1, 7)
      version    <- Gen.choose(1, 5)
      protection <- genProtection(nino, id, version)
    } yield protection

  private def genProtection(nino: String, id: Long, sequence: Int): Gen[Protection] =
    (
      Gen.const(nino),
      Gen.const(id),
      Gen.const(sequence),
      Gen.oneOf(ProtectionStatus.values),
      Gen.oneOf(ProtectionType.values),
      genMoney.map(_.getOrElse(0)),
      genMoney.map(_.getOrElse(0)),
      genMoney.map(_.getOrElse(0)),
      genMoney.map(_.getOrElse(0)),
      genMoney.map(_.getOrElse(0)),
      genDate,
      genTime,
      refGen.sometimes,
      genMoney,
      genMoney,
      genMoney,
      genDate.sometimes,
      genMoney,
      genMoney,
      genFactor.sometimes,
      genPercentage.sometimes
    )
      .mapN(Protection.apply)

  def genProtections(nino: String): Gen[Protections] = (
    Gen.const(nino),
    pensionSchemeAdministratorCheckReferenceGen.sometimes,
    Gen.choose(2, 5).flatMap(n => Gen.listOfN(n, genProtection(nino)))
  )
    .mapN(Protections.apply)

  val protectionsStore: PersistentGen[String, Protections] = genProtections("").asMutable[String]

}
