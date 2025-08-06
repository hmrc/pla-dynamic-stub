package uk.gov.hmrc.pla.stub.rules

import uk.gov.hmrc.pla.stub.model.hip.Notification._
import uk.gov.hmrc.pla.stub.model.hip._
import uk.gov.hmrc.pla.stub.model.hip.LifetimeAllowanceType._

sealed trait HipAmendmentRules {

  /** @param relevantAmount
    *   the relevant amount on the amendment request
    * @param otherExistingProtections
    *   all existing protections for the individual except the one to be amended
    * @return
    *   the outcome of the business rules check in the form of a notification Id: should be >= 1
    */
  def calculateNotificationId(relevantAmount: Double, otherExistingProtections: List[HipProtection]): Notification
}

object IndividualProtection2014AmendmentRules extends HipAmendmentRules {

  override def calculateNotificationId(
      relevantAmount: Double,
      otherExistingProtections: List[HipProtection]
  ): Notification = {
    val withdraw       = relevantAmount < 1_125_001
    val defaultOutcome = if (withdraw) Notification1 else Notification6

    val otherOpenProtection = otherExistingProtections.find {
      _.status == ProtectionStatus.Open
    }
    otherOpenProtection
      .map { openProtection =>
        (withdraw, openProtection.`type`) match {
          case (false, EnhancedProtection)  => Notification2
          case (false, FixedProtection)     => Notification3
          case (false, FixedProtection2014) => Notification4
          case (false, FixedProtection2016) => Notification5
          case (true, FixedProtection2016)  => Notification7
        }
      }
      .getOrElse(defaultOutcome)
  }

}

object IndividualProtection2016AmendmentRules extends HipAmendmentRules {

  override def calculateNotificationId(
      relevantAmount: Double,
      otherExistingProtections: List[HipProtection]
  ): Notification = {
    val withdraw       = relevantAmount < 1_000_001
    val defaultOutcome = if (withdraw) Notification8 else Notification13

    val otherOpenProtection = otherExistingProtections.find {
      _.status == ProtectionStatus.Open
    }
    otherOpenProtection
      .map { openProtection =>
        (withdraw, openProtection.`type`) match {
          case (false, EnhancedProtection)  => Notification9
          case (false, FixedProtection)     => Notification3
          case (false, FixedProtection2014) => Notification4
          case (false, FixedProtection2016) => Notification5
          case (true, FixedProtection2016)  => Notification14
        }
      }
      .getOrElse(defaultOutcome)
  }

}

object IndividualProtection2014LtaAmendmentRules extends HipAmendmentRules {

  override def calculateNotificationId(
      relevantAmount: Double,
      otherExistingProtections: List[HipProtection]
  ): Notification = ???

}

object IndividualProtection2016LtaAmendmentRules extends HipAmendmentRules {

  override def calculateNotificationId(
      relevantAmount: Double,
      otherExistingProtections: List[HipProtection]
  ): Notification = ???

}
