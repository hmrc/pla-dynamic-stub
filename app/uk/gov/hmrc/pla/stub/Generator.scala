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

package uk.gov.hmrc.pla.stub

import java.time.LocalTime
import java.time.format.DateTimeFormatter._
import cats.implicits._
import org.scalacheck._
import org.scalacheck.support.cats._
import uk.gov.hmrc.pla.stub.model.{PensionDebit, Protection, Protections, Version}
import uk.gov.hmrc.smartstub.{AdvGen, _}
import uk.gov.hmrc.smartstub.Enumerable.instances.utrEnum

object Generator {


  /**
    * "^[1-9A][0-9]{6}[ABCDEFHXJKLMNYPQRSTZW]|(IP14|IP16|FP16)[0-9]{10}[ABCDEFGHJKLMNPRSTXYZ]$^"
    **/
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

  /**
    * "^PSA[0-9]{8}[A-Z]?$^"
    **/
  val pensionSchemeAdministratorCheckReferenceGen: Gen[String] =
    pattern"99999999Z".map("PSA" + _)


  val genTime: Gen[LocalTime] = Gen.choose(0, 24 * 60 * 60).map {
    x =>
      LocalTime.parse(
        {
          BigInt(1000000000L) * x
        }.toString,
        ofPattern("N")
      )
  }

  val genMoney: Gen[Option[Double]] =
    Gen.choose(1, 1000000000).map {
      _.toDouble / 100
    }.sometimes


  val pensionDebitGen: Gen[PensionDebit] =
    (Gen.choose(1, 1000000).map(_.toDouble),
      Gen.date(2014, 2017).map (_.format(ISO_LOCAL_DATE))).mapN(PensionDebit.apply)

  def genProtection(nino: String): Gen[Protection] = {
    for {
      id <- Gen.choose(1, 7)
      version <- Gen.choose(1, 5)
      protection <- genProtection(nino, id, version)
    } yield protection
  }

  def genVersions(nino: String, id: Long, version: Int): Gen[List[Version]] = version match {
    case n if n <= 0 => Gen.const(Nil)
    case _ =>
      Gen.listOfN(version, genProtection(nino, id, 0)).map {
        _.zipWithIndex.map {
          case (protection, i) => val newV = i + 1
            Version(newV, s"/individual/$nino/protections/$id/version/$newV", protection.copy(version = newV))
        }
      }
  }

  def genProtection(nino: String, id: Long, version: Int): Gen[Protection] =
    (Gen.const(nino),
      Gen.const(id),
      Gen.const(version),
      Gen.choose(1, 7),
      Gen.choose(1, 6),
      Gen.choose(1, 47).map(_.toShort).sometimes,
      Gen.alphaStr.sometimes,
      refGen.almostAlways,
      Gen.date(2014, 2017).map {_.format(ISO_LOCAL_DATE)}.sometimes,
      genTime.map {_.format(ISO_LOCAL_TIME)}.sometimes,
      genMoney,
      genMoney,
      genMoney,
      genMoney,
      genMoney,
      genMoney,
      genMoney,
      Gen.const(None),
      genMoney,
      Gen.listOf(pensionDebitGen).sometimes,
      genVersions(nino, id, version - 1).map {_.some},
      Gen.const(None))                                                // withdrawnDate
      .mapN(Protection.apply)

  def genProtections(nino: String): Gen[Protections] = (
    Gen.const(nino),
    pensionSchemeAdministratorCheckReferenceGen.sometimes,
    Gen.choose(2, 5).flatMap { n => Gen.listOfN(n, genProtection(nino)) })
    .mapN(Protections.apply)

  val protectionsStore: PersistentGen[String, Protections]=  genProtections("").asMutable[String]


}
