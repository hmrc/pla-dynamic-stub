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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.test.Injecting
import uk.gov.hmrc.pla.stub.model.Protection
import uk.gov.hmrc.pla.stub.model.hip.{HipProtection, ProtectionStatus, ProtectionType}
import uk.gov.hmrc.pla.stub.repository.MongoProtectionRepository

import scala.concurrent.{ExecutionContext, Future}

object TestData {

  val nino         = "AA000000A"
  val strippedNino = nino.dropRight(1)

  val idLong: Long = 0
  val idInt: Int   = idLong.toInt

  val protection = Protection(
    nino = strippedNino,
    id = idLong,
    version = 0,
    `type` = 0,
    status = 0,
    notificationID = None,
    notificationMsg = None,
    protectionReference = None
  )

  val hipProtection = HipProtection(
    nino = nino,
    id = idInt,
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
    with Injecting {

  private val mockProtectionsStore: MongoProtectionRepository = mock[MongoProtectionRepository]

  private val protectionService = new PLAProtectionService(mockProtectionsStore, inject[ExecutionContext])

  override def beforeEach(): Unit = {
    reset(mockProtectionsStore)
    super.beforeEach()
  }

  "Protection Service" when {

    "retrieveProtections is called" must {

      "pass nino to repository unchanged" in {

        protectionService.retrieveProtections(TestData.nino)

        verify(mockProtectionsStore).findProtectionsByNino(TestData.nino)

      }

    }

    "retrieveHIPProtections is called" must {

      "strip suffix from nino when provided to repository" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        protectionService.retrieveHIPProtections(TestData.nino)

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)

      }

    }

    "insertOrUpdateProtection is called" must {

      "pass nino to repository unchanged" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        when(mockProtectionsStore.removeByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful((): Unit))

        when(mockProtectionsStore.insertProtection(any()))
          .thenReturn(Future.successful((): Unit))

        protectionService.insertOrUpdateProtection(TestData.protection)

        val mock = Mockito.inOrder(mockProtectionsStore)

        mock.verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).removeByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).insertProtection(any())

      }

    }

    "insertOrUpdateHipProtection is called" must {

      "strip suffix from nino when provided to repository" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        when(mockProtectionsStore.removeByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.unit)

        when(mockProtectionsStore.insertProtection(any()))
          .thenReturn(Future.unit)

        protectionService.insertOrUpdateHipProtection(TestData.hipProtection)

        val mock = Mockito.inOrder(mockProtectionsStore)

        mock.verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).removeByNino(TestData.strippedNino)

        mock.verify(mockProtectionsStore).insertProtection(any())

      }

    }

    "findProtectionByNinoAndId is called" must {

      "pass nino to repository unchanged" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        protectionService.findProtectionByNinoAndId(TestData.strippedNino, TestData.idLong)

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)

      }

    }

    "findHipProtectionByNinoAndId is called" must {

      "strip suffix from nino when provided to repository" in {

        when(mockProtectionsStore.findProtectionsByNino(eqTo(TestData.strippedNino)))
          .thenReturn(Future.successful(None))

        protectionService.findHipProtectionByNinoAndId(TestData.nino, TestData.idInt)

        verify(mockProtectionsStore).findProtectionsByNino(TestData.strippedNino)

      }

    }

  }

}
