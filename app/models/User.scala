package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import slick.lifted.MappedTo

case class UserId (value: Int) extends MappedTo[Int] {
  override def toString: String = value.toString
}

/**
 * The user model
 * @param id The unique ID of the user
 * @param loginInfo
 * @param email the email of the authenticated provider
 */
final case class User(
  id: UserId,
//  loginInfo: Seq[LoginInfo],
  email: Email
) extends Identity

