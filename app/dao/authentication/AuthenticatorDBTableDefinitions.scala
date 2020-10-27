package dao.authentication

import java.time.{OffsetDateTime, ZonedDateTime}

import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import play.api.db.slick.HasDatabaseConfigProvider
import utils.MyPostgresProfile

trait AuthenticatorDBTableDefinitions {
  self: HasDatabaseConfigProvider[MyPostgresProfile] =>

  import profile.api._

  /**
    *
    * @param authenticatorId
    * @param loginInfoId
    * @param lastUsedDateTime
    * @param expirationDateTime
    * @param idleTimeout timeout in nanoseconds
    */
  case class DBBearerTokenAuthenticator(
    authenticatorId: String,
    loginInfoId: Long,
    lastUsedDateTime: ZonedDateTime,
    expirationDateTime: ZonedDateTime,
    idleTimeout: Option[Long]
  )

  class BearerTokenAuthenticators(tag: Tag) extends Table[DBBearerTokenAuthenticator](tag, "BEARER_TOKEN_AUTHENTICATOR") {
    def authenticatorId = column[String]("AUTHENTICATOR_ID")

    def loginInfoId = column[Long]("LOGIN_INFO_ID")

    def lastUsedDateTime = column[ZonedDateTime]("LAST_USED_DATETIME")

    def expirationDateTime = column[ZonedDateTime]("EXPIRATION_DATETIME")

    def idleTimeout = column[Option[Long]]("IDLE_TIMEOUT")

    override def * =
      (authenticatorId, loginInfoId, lastUsedDateTime, expirationDateTime, idleTimeout)
        .<>(DBBearerTokenAuthenticator.tupled, DBBearerTokenAuthenticator.unapply)
  }

  val bearerTokenAuthenticators = TableQuery[BearerTokenAuthenticators]
}
