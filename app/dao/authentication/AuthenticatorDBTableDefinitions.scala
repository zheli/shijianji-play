package dao.authentication

import java.time.OffsetDateTime

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
    lastUsedDateTime: OffsetDateTime,
    expirationDateTime: OffsetDateTime,
    idleTimeout: Option[Long]
  )

  class BearerTokenAuthenticators(tag: Tag) extends Table[DBBearerTokenAuthenticator](tag, "BEARER_TOKEN_AUTHENTICATOR") {
    def authenticatorId = column[String]("AUTHENTICATOR_ID")

    def loginInfoId = column[Long]("LOGIN_INFO_ID")

    def lastUsedDateTime = column[OffsetDateTime]("LAST_USED_DATETIME")

    def expirationDateTime = column[OffsetDateTime]("EXPIRATION_DATETIME")

    def idleTimeout = column[Option[Long]]("IDLE_TIMEOUT")

    override def * =
      (authenticatorId, loginInfoId, lastUsedDateTime, expirationDateTime, idleTimeout)
        .<>(DBBearerTokenAuthenticator.tupled, DBBearerTokenAuthenticator.unapply)
  }

  val bearerTokenAuthenticators = TableQuery[BearerTokenAuthenticators]
}
