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
import uk.gov.hmrc.pla.stub.model.{Protection, Protections}
import uk.gov.hmrc.pla.stub.repository.MongoProtectionRepository

import scala.concurrent.{ExecutionContext, Future}

object TestData {

  val nino                 = "AA000000A"
  val strippedNino: String = nino.dropRight(1)

  val id: Long = 0

  val psaCheckReference = "PSA12345678L"

  val protection = Protection(
    nino = strippedNino,
    id = id,
    version = 0,
    `type` = 2,
    status = 1,
    notificationID = None,
    notificationMsg = None,
    protectionReference = None
  )

  val hipProtection = HipProtection(
    nino = nino,
    id = id,
    sequence = 0,
    status = ProtectionStatus.Open,
    `type` = ProtectionType.IndividualProtection2014,
    relevantAmount = 0,
    preADayPensionInPaymentAmount = 0,
    postADayBenefitCrystallisationEventAmount = 0,
    uncrystallisedRightsAmount = 0,
    nonUKRightsAmount = 0,
    protectionReference = None
  )

}

class PLAProtectionServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with GuiceOneServerPerSuite
    with BeforeAndAfterEach
    with ScalaFutures
    with Injecting {

  private val mockProtectionsStore: MongoProtectionRepository = mock[MongoProtectionRepository]

  private val protectionService = new PLAProtectionService(mockProtectionsStore, inject[ExecutionContext])

  override def beforeEach(): Unit = {
    reset(mockProtectionsStore)
    super.beforeEach()
  }

  "Protection Service" when {

    "retrieveProtections is called" must {

      "return None when no data can be found" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        protectionService.retrieveProtections(TestData.strippedNino).futureValue shouldBe None
      }

      "return protections when data is present" in {

        val protections =
          Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection))

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(Some(protections)))

        protectionService.retrieveProtections(TestData.strippedNino).futureValue shouldBe Some(protections)
      }
    }

    "retrieveHIPProtections is called" must {

      "return None when no data can be found" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        protectionService.retrieveHIPProtections(TestData.strippedNino).futureValue shouldBe None
      }

      "transform response to HIP Protections Model when data is returned" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(
            Future.successful(
              Some(Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection)))
            )
          )

        protectionService.retrieveHIPProtections(TestData.strippedNino).futureValue shouldBe Some(
          HIPProtectionsModel(
            TestData.psaCheckReference,
            Seq(ProtectionRecordsList(ProtectionRecord(TestData.protection), None))
          )
        )
      }
    }

    "insertOrUpdateProtection is called" must {

      "insert a record if it does not exist" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        when(mockProtectionsStore.removeByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful((): Unit))

        when(mockProtectionsStore.insertProtection(any()))
          .thenReturn(Future.successful((): Unit))

        protectionService.insertOrUpdateProtection(TestData.protection).futureValue shouldBe Results.Ok

        val mock = Mockito.inOrder(mockProtectionsStore)

        mock.verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).removeByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).insertProtection(any())
      }

      "update a record if it exists" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(
            Future.successful(
              Some(Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection)))
            )
          )

        when(mockProtectionsStore.removeByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful((): Unit))

        when(mockProtectionsStore.insertProtection(any()))
          .thenReturn(Future.successful((): Unit))

        protectionService.insertOrUpdateProtection(TestData.protection.copy(status = 3)).futureValue shouldBe Results.Ok

        val mock = Mockito.inOrder(mockProtectionsStore)

        mock.verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).removeByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).insertProtection(any())
      }
    }

    "insertOrUpdateHipProtection is called" must {

      "insert a record if it does not exist" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        when(mockProtectionsStore.removeByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful((): Unit))

        when(mockProtectionsStore.insertProtection(any()))
          .thenReturn(Future.successful((): Unit))

        protectionService
          .insertOrUpdateHipProtection(TestData.hipProtection.copy(nino = TestData.strippedNino))
          .futureValue shouldBe Results.Ok

        val mock = Mockito.inOrder(mockProtectionsStore)

        mock.verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).removeByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).insertProtection(any())
      }

      "update a record if it exists" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(
            Future.successful(
              Some(Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection)))
            )
          )

        when(mockProtectionsStore.removeByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful((): Unit))

        when(mockProtectionsStore.insertProtection(any()))
          .thenReturn(Future.successful((): Unit))

        protectionService
          .insertOrUpdateHipProtection(TestData.hipProtection.copy(nino = TestData.strippedNino, status = Withdrawn))
          .futureValue shouldBe Results.Ok

        val mock = Mockito.inOrder(mockProtectionsStore)

        mock.verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).removeByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).insertProtection(any())

      }
    }

    "findProtectionByNinoAndId is called" must {

      "return None if no protection is present with a matching Nino" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        protectionService.findProtectionByNinoAndId(TestData.strippedNino, TestData.id).futureValue shouldBe None

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }

      "return None if no protection is present with a matching ID" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(
            Future.successful(
              Some(Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection)))
            )
          )

        protectionService.findProtectionByNinoAndId(TestData.strippedNino, 1).futureValue shouldBe None

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }

      "return the corresponding protection when the Nino and ID match" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(
            Future.successful(
              Some(Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection)))
            )
          )

        protectionService.findProtectionByNinoAndId(TestData.strippedNino, TestData.id).futureValue shouldBe Some(
          TestData.protection
        )

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }
    }

    "findHipProtectionByNinoAndId is called" must {

      "return None if no protection is present with a matching Nino" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        protectionService.findHipProtectionByNinoAndId(TestData.strippedNino, TestData.id).futureValue shouldBe None

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }

      "return None if no protection is present with a matching ID" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(
            Future.successful(
              Some(Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection)))
            )
          )

        protectionService.findHipProtectionByNinoAndId(TestData.strippedNino, 1).futureValue shouldBe None

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }

      "return the corresponding protection when the Nino and ID match" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(
            Future.successful(
              Some(Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection)))
            )
          )

        protectionService.findHipProtectionByNinoAndId(TestData.strippedNino, TestData.id).futureValue shouldBe Some(
          TestData.hipProtection.copy(nino = TestData.strippedNino)
        )

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }
    }

    "findAllProtectionsByNino is called" must {

      "return None if no protections are present with a matching Nino" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        protectionService.findAllProtectionsByNino(TestData.strippedNino).futureValue shouldBe None

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }

      "return the corresponding protections when the Nino matches" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(
            Future.successful(
              Some(Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection)))
            )
          )

        protectionService.findAllProtectionsByNino(TestData.strippedNino).futureValue shouldBe Some(
          List(TestData.protection)
        )

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }
    }

    "findAllHipProtectionsByNino is called" must {

      "return None if no protections are present with a matching Nino" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        protectionService.findAllHipProtectionsByNino(TestData.strippedNino).futureValue shouldBe List
          .empty[HipProtection]

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }

      "return the corresponding protections when the Nino matches" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(
            Future.successful(
              Some(Protections(TestData.strippedNino, Some(TestData.psaCheckReference), List(TestData.protection)))
            )
          )

        protectionService.findAllHipProtectionsByNino(TestData.strippedNino).futureValue shouldBe List(
          TestData.hipProtection.copy(nino = TestData.strippedNino)
        )

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)
      }
    }
  }

}
