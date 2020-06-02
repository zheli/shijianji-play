package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.data.format.Formatter
import play.api.libs.json.{Json, OFormat}
import slick.lifted.MappedTo

case class UserId (value: Int) extends MappedTo[Int] {
  override def toString: String = value.toString
}

object UserId {
  implicit val formatter = Json.format[UserId]
}

/**
 * The user model
 * @param id The unique ID of the user
 * @param loginInfo The linked loginInfo
 * @param email the email of the authenticated provider
 */
final case class User(
  // TODO Switch to UUID user id
  id: Option[Long],
  loginInfo: LoginInfo,
  email: Email,
) extends Identity

object User {
  implicit val formatter: OFormat[User] = Json.format[User]
}
