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

package uk.gov.hmrc.pla.stub.model

import org.scalacheck.Gen
import uk.gov.hmrc.domain.NinoGenerator
import uk.gov.hmrc.smartstub.*

import java.util.Random

object Generator {

  val rand          = new Random()
  val ninoGenerator = new NinoGenerator(rand)

  def randomNino: String       = ninoGenerator.nextNino.nino.replaceFirst("MA", "AA")
  def randomProtectionID: Long = rand.nextLong

  /** "^[0-9]{4}[ABCDEFGHJKLMNPRSTXYZ]$^"
    */
  private def refGenForProtectionType(protectionType: String): Gen[String] =
    for {
      number <- pattern"9999".gen
      suffix <- Gen.oneOf("ABCDEFGHJKLMNPRSTXYZ".toList)
    } yield s"$protectionType$number$suffix"

  def refGenFP16: Option[String]                     = refGenForProtectionType("FP16").seeded(1L)
  def refGenIP16: Option[String]                     = refGenForProtectionType("IP16").seeded(1L)
  def refGenIP14: Option[String]                     = refGenForProtectionType("IP14").seeded(1L)
  def randomOlderProtectionReference: Option[String] = refGenForProtectionType("A").seeded(1L)

  /** "^PSA[0-9]{8}[A-Z]?$^"
    */
  val pensionSchemeAdministratorCheckReferenceGen: Gen[String] = pattern"99999999Z".map("PSA" + _)
}
