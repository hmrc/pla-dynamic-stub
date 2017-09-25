/*
 * Copyright 2017 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.mvc.Results.{NotFound, Ok}
import uk.gov.hmrc.pla.stub.model.Generator.pensionSchemeAdministratorCheckReferenceGen
import uk.gov.hmrc.pla.stub.model.{Generator, _}
import uk.gov.hmrc.smartstub._

object PLAProtectionService {

  lazy val protectionsStore = Generator.protectionsStore.empty
  def createLTAProtectionResponse(data: CreateLTAProtectionRequest): CreateLTAProtectionResponse = {
    val protection : Protection = Generator.genProtection(data.nino).sample.get
    val newLTAProtection = Protection(
      nino = data.nino,
      version = protection.version,
      id = protection.id.toInt,
      `type` = data.protection.`type`,
      protectionReference = protection.protectionReference,
      status = protection.status,
      notificationID = protection.notificationID,
      notificationMsg = protection.notificationMsg,
      certificateDate = protection.certificateDate,
      certificateTime = protection.certificateTime,
      relevantAmount = data.protection.relevantAmount,
      protectedAmount = protection.protectedAmount,
      preADayPensionInPayment = data.protection.preADayPensionInPayment,
      postADayBCE = data.protection.postADayBCE,
      uncrystallisedRights = data.protection.uncrystallisedRights,
      nonUKRights = data.protection.nonUKRights,
      pensionDebits = data.pensionDebits,
      pensionDebitTotalAmount = protection.pensionDebitTotalAmount
    )

    val protections = protectionsStore.get(data.nino)
    val pensionSchemeAdministratorCheckReference = pensionSchemeAdministratorCheckReferenceGen.sometimes.sample.get
    val ltaProtections : List[Protection] = protections match {
      case Some(protections) =>   newLTAProtection :: protections.protections
      case None =>  List(newLTAProtection)
    }
    protectionsStore(data.nino) = Protections(data.nino,pensionSchemeAdministratorCheckReference,ltaProtections)
    CreateLTAProtectionResponse(nino = data.nino,
      pensionSchemeAdministratorCheckReference = pensionSchemeAdministratorCheckReference,
      protection = newLTAProtection)
  }

  def updateLTAProtection(data: UpdateLTAProtectionRequest,nino:String,protectionId: Long)  = {
    val protections: Option[Protections] = PLAProtectionService.retrieveProtections(nino)
    val existingProtection: Option[Protection] = protections.get.protections.find(p => p.id == protectionId)
    existingProtection match {
      case Some(existingProtection) => Ok(Json.toJson(updateLTAProtectionResponse(existingProtection,data,protectionId)))
      case None => NotFound(Json.toJson(Error("Potection to update not found")))
    }
  }

  def updateLTAProtectionResponse(protection: Protection,data: UpdateLTAProtectionRequest,protectionId: Long) : UpdateLTAProtectionResponse = {

    val updatedLTAProtection = Protection(
      nino = data.nino,
      version = protection.version,
      id = protection.id.toInt,
      `type` = data.protection.`type`,
      protectionReference = protection.protectionReference,
      status = protection.status,
      notificationID = protection.notificationID,
      notificationMsg = protection.notificationMsg,
      certificateDate = protection.certificateDate,
      certificateTime = protection.certificateTime,
      relevantAmount = Some(data.protection.relevantAmount),
      protectedAmount = protection.protectedAmount,
      preADayPensionInPayment = Some(data.protection.preADayPensionInPayment),
      postADayBCE = Some(data.protection.postADayBCE),
      uncrystallisedRights = Some(data.protection.uncrystallisedRights),
      nonUKRights = Some(data.protection.nonUKRights),
      pensionDebits = data.pensionDebits,
      pensionDebitTotalAmount = protection.pensionDebitTotalAmount
    )
    val pensionSchemeAdministratorCheckReference = pensionSchemeAdministratorCheckReferenceGen.sometimes.sample.get
    val protections: Protections = PLAProtectionService.retrieveProtections(data.nino).get
    val ltaProtections : List[Protection] = updatedLTAProtection :: protections.protections.filter(_.id != protectionId)
    protectionsStore(data.nino) = Protections(data.nino,pensionSchemeAdministratorCheckReference,ltaProtections)
    UpdateLTAProtectionResponse(nino = data.nino,
      pensionSchemeAdministratorCheckReference = pensionSchemeAdministratorCheckReference,
      protection = updatedLTAProtection)
  }

  def retrieveProtections(nino: String): Option[Protections] = {
    protectionsStore.get(nino)
  }

}
