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

import java.time.{LocalDate, LocalTime}

object ProtectionTestData {

  import Generator._

  val currentDate: String = LocalDate.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
  val currentTime: String = LocalTime.now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)

  val openFP2016 = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.FP2016),
    status = Protection.extractedStatus(Protection.Status.Open),
    notificationID = Some(22),
    notificationMsg = None,
    protectionReference = refGenFP16,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val openFP2016WithPensionDebits = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.FP2016),
    status = Protection.extractedStatus(Protection.Status.Open),
    notificationID = Some(22),
    notificationMsg = None,
    protectionReference = refGenFP16,
    version = 1,
    pensionDebits = Some(List(PensionDebit(100000.0, "29-8-2016"), PensionDebit(250000.0, "12-03-2017"))),
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val openIP2016 = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.IP2016),
    status = Protection.extractedStatus(Protection.Status.Open),
    notificationID = Some(12),
    notificationMsg = None,
    protectionReference = refGenIP16,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val openFP2014 = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.FP2014),
    status = Protection.extractedStatus(Protection.Status.Open),
    notificationID = None,
    notificationMsg = None,
    protectionReference = refGenFP16,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val openIP2014 = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.IP2014),
    status = Protection.extractedStatus(Protection.Status.Open),
    notificationID = None,
    notificationMsg = None,
    protectionReference = refGenIP14,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val openPrimary = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.Primary),
    status = Protection.extractedStatus(Protection.Status.Open),
    notificationID = None,
    notificationMsg = None,
    protectionReference = randomOlderProtectionReference,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val openFixed = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.Fixed),
    status = Protection.extractedStatus(Protection.Status.Open),
    notificationID = None,
    notificationMsg = None,
    protectionReference = randomOlderProtectionReference,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val openEnhanced = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.Enhanced),
    status = Protection.extractedStatus(Protection.Status.Open),
    notificationID = None,
    notificationMsg = None,
    protectionReference = randomOlderProtectionReference,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val dormantPrimary = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.Primary),
    status = Protection.extractedStatus(Protection.Status.Dormant),
    notificationID = None,
    notificationMsg = None,
    protectionReference = randomOlderProtectionReference,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val dormantEnhanced = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.Enhanced),
    status = Protection.extractedStatus(Protection.Status.Dormant),
    notificationID = None,
    notificationMsg = None,
    protectionReference = randomOlderProtectionReference,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val withdrawnPrimary = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.Enhanced),
    status = Protection.extractedStatus(Protection.Status.Withdrawn),
    notificationID = None,
    notificationMsg = None,
    protectionReference = randomOlderProtectionReference,
    version = 1,
    certificateDate = Some(currentDate),
    certificateTime = Some(currentTime)
  )

  val rejected = Protection(
    nino = randomNino,
    id = randomProtectionID,
    `type` = Protection.extractedType(Protection.Type.IP2016),
    status = Protection.extractedStatus(Protection.Status.Rejected),
    notificationID = Some(21),
    notificationMsg = None,
    protectionReference = None,
    version = 1
  )

}
