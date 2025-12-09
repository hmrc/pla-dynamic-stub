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

package uk.gov.hmrc.pla.stub.services

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.mvc.Results
import play.api.test.Injecting
import uk.gov.hmrc.pla.stub.model.hip.ProtectionStatus.Withdrawn
import uk.gov.hmrc.pla.stub.model.hip._
import uk.gov.hmrc.pla.stub.model.{DateModel, Protections, TimeModel}
import uk.gov.hmrc.pla.stub.repository.MongoProtectionRepository

import scala.concurrent.{ExecutionContext, Future}

class ProtectionServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with ScalaFutures
    with Injecting {

  private val mockProtectionsStore: MongoProtectionRepository = mock[MongoProtectionRepository]

  private val protectionService = new ProtectionService(mockProtectionsStore, inject[ExecutionContext])

  override def beforeEach(): Unit = {
    reset(mockProtectionsStore)
    super.beforeEach()
  }

  val nino = "AA000000A"

  val id: Long = 0

  val psaCheckReference = "PSA12345678L"

  val protection = Protection(
    nino = nino,
    id = id,
    sequence = 0,
    status = ProtectionStatus.Open,
    `type` = ProtectionType.IndividualProtection2014,
    certificateDate = DateModel.of(2025, 12, 9),
    certificateTime = TimeModel.of(10, 50, 25),
    relevantAmount = 0,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = None
  )

  "Protection Service" when {

    "retrieveConvertedProtections is called" must {

      "return None when no data can be found" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(nino)))
          .thenReturn(Future.successful(None))

        protectionService.retrieveConvertedProtections(nino).futureValue shouldBe None
      }

      "transform response to Protections Model when data is returned" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(nino)))
          .thenReturn(
            Future.successful(
              Some(Protections(nino, Some(psaCheckReference), List(protection)))
            )
          )

        protectionService.retrieveConvertedProtections(nino).futureValue shouldBe Some(
          ReadProtectionsResponse(
            psaCheckReference,
            Seq(ProtectionRecordsList(ProtectionRecord.apply(protection), None))
          )
        )
      }
    }

    "insertOrUpdateProtection is called" must {

      "insert a record if it does not exist" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(nino)))
          .thenReturn(Future.successful(None))

        when(mockProtectionsStore.removeByNino(eqTo(nino)))
          .thenReturn(Future.successful((): Unit))

        when(mockProtectionsStore.insertProtection(any()))
          .thenReturn(Future.successful((): Unit))

        protectionService
          .insertOrUpdateProtection(protection.copy(nino = nino))
          .futureValue shouldBe Results.Ok

        val mock = Mockito.inOrder(mockProtectionsStore)

        mock.verify(mockProtectionsStore).findProtectionsByNino(nino)

        mock.verify(mockProtectionsStore).removeByNino(nino)

        mock.verify(mockProtectionsStore).insertProtection(any())
      }

      "update a record if it exists" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(nino)))
          .thenReturn(
            Future.successful(
              Some(Protections(nino, Some(psaCheckReference), List(protection)))
            )
          )

        when(mockProtectionsStore.removeByNino(eqTo(nino)))
          .thenReturn(Future.successful((): Unit))

        when(mockProtectionsStore.insertProtection(any()))
          .thenReturn(Future.successful((): Unit))

        protectionService
          .insertOrUpdateProtection(protection.copy(nino = nino, status = Withdrawn))
          .futureValue shouldBe Results.Ok

        val mock = Mockito.inOrder(mockProtectionsStore)

        mock.verify(mockProtectionsStore).findProtectionsByNino(nino)

        mock.verify(mockProtectionsStore).removeByNino(nino)

        mock.verify(mockProtectionsStore).insertProtection(any())

      }
    }

    "findProtectionByNinoAndId is called" must {

      "return None if no protection is present with a matching Nino" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(nino)))
          .thenReturn(Future.successful(None))

        protectionService.findProtectionByNinoAndId(nino, id).futureValue shouldBe None

        verify(mockProtectionsStore).findProtectionsByNino(nino)
      }

      "return None if no protection is present with a matching ID" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(nino)))
          .thenReturn(
            Future.successful(
              Some(Protections(nino, Some(psaCheckReference), List(protection)))
            )
          )

        protectionService.findProtectionByNinoAndId(nino, 1).futureValue shouldBe None

        verify(mockProtectionsStore).findProtectionsByNino(nino)
      }

      "return the corresponding protection when the Nino and ID match" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(nino)))
          .thenReturn(
            Future.successful(
              Some(Protections(nino, Some(psaCheckReference), List(protection)))
            )
          )

        protectionService.findProtectionByNinoAndId(nino, id).futureValue shouldBe Some(
          protection.copy(nino = nino)
        )

        verify(mockProtectionsStore).findProtectionsByNino(nino)
      }
    }

    "findAllProtectionsByNino is called" must {

      "return None if no protections are present with a matching Nino" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(nino)))
          .thenReturn(Future.successful(None))

        protectionService.findAllProtectionsByNino(nino).futureValue shouldBe List
          .empty[Protection]

        verify(mockProtectionsStore).findProtectionsByNino(nino)
      }

      "return the corresponding protections when the Nino matches" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(nino)))
          .thenReturn(
            Future.successful(
              Some(Protections(nino, Some(psaCheckReference), List(protection)))
            )
          )

        protectionService.findAllProtectionsByNino(nino).futureValue shouldBe List(
          protection.copy(nino = nino)
        )

        verify(mockProtectionsStore).findProtectionsByNino(nino)
      }
    }
  }

}
