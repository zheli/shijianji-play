package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json._

/**
 * A token to authenticate a user against an endpoint for a short time period.
 *
 * @param id The unique token ID.
 * @param userID The unique ID of the user the token is associated with.
 * @param expiry The date-time the token expires.
 */
case class AuthToken(
  id: UUID,
  userID: Long,
  expiry: DateTime
)

object AuthToken {
  implicit val DateTimeWriter: Writes[DateTime] = new Writes[DateTime] {
    override def writes(dateTime: DateTime) = JsString(dateTime.toString)
  }
  implicit val writer: OWrites[AuthToken] = Json.writes[AuthToken]
}
