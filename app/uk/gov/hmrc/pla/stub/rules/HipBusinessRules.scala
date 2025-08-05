package uk.gov.hmrc.pla.stub.rules

import uk.gov.hmrc.pla.stub.model.hip.{HipProtection, Notification}

trait HipAmendmentRules {

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
  ): Notification = ???

}

object IndividualProtection2016AmendmentRules extends HipAmendmentRules {

  override def calculateNotificationId(
      relevantAmount: Double,
      otherExistingProtections: List[HipProtection]
  ): Notification = ???

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
