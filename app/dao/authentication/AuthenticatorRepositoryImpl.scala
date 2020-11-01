package dao.authentication

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import dao.DBTableDefinitions
import play.api.db.slick._
import utils._

import scala.concurrent.duration._
import scala.concurrent._

/**
  *
  */
class AuthenticatorRepositoryImpl(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ex: ExecutionContext)
    extends AuthenticatorRepository[BearerTokenAuthenticator]
    with HasDatabaseConfigProvider[MyPostgresProfile]
    with AuthenticatorDBTableDefinitions
    with DBTableDefinitions {

  import profile.api._

  override def find(id: String): Future[Option[BearerTokenAuthenticator]] = {
    val action = for {
      (auth, login) <- bearerTokenAuthenticators.filter(_.authenticatorId === id).join(loginInfos).on(_.loginInfoId === _.id)
    } yield (auth, login)
    db.run(action.result).map {
      case Seq((dbAuth, dbLogin)) =>
        Some(
          BearerTokenAuthenticator(
            id = dbAuth.authenticatorId,
            loginInfo = dbLogin.toLoginInfo,
            lastUsedDateTime = zonedDateTimeToJodaDateTime(dbAuth.lastUsedDateTime),
            expirationDateTime = zonedDateTimeToJodaDateTime(dbAuth.expirationDateTime),
            idleTimeout = dbAuth.idleTimeout.map(FiniteDuration(_, NANOSECONDS))
          )
        )
      case Seq() => None
      case _ => throw new RuntimeException("Found more than one bearerTokenAuthenticators!")
    }
  }

  override def add(authenticator: BearerTokenAuthenticator): Future[BearerTokenAuthenticator] = {
    // TODO: check if authenticator exists before adding it to avoid duplicates
    val action = {
      val lastUsedDateTime = jodaDateTimeToZonedDateTime(authenticator.lastUsedDateTime)
      val expiredDateTime = jodaDateTimeToZonedDateTime(authenticator.expirationDateTime)
      loginInfoQuery(authenticator.loginInfo).result.head.flatMap { dbLoginInfo =>
        bearerTokenAuthenticators +=
          DBBearerTokenAuthenticator(
            authenticatorId = authenticator.id,
            loginInfoId = dbLoginInfo.id.get,
            lastUsedDateTime = lastUsedDateTime,
            expirationDateTime = expiredDateTime,
            idleTimeout = authenticator.idleTimeout.map(_.toNanos)
          )
      }.transactionally
    }
    db.run(action).map(_ => authenticator)
  }

  override def update(authenticator: BearerTokenAuthenticator): Future[BearerTokenAuthenticator] = {
    val query = bearerTokenAuthenticators.filter(_.authenticatorId === authenticator.id).map { auth =>
      (auth.loginInfoId, auth.lastUsedDateTime, auth.expirationDateTime, auth.idleTimeout)
    }
    val action = loginInfoQuery(authenticator.loginInfo).result.head.flatMap { dbLoginInfo =>
      query.update(
        dbLoginInfo.id.get,
        jodaDateTimeToZonedDateTime(authenticator.lastUsedDateTime),
        jodaDateTimeToZonedDateTime(authenticator.expirationDateTime),
        authenticator.idleTimeout.map(_.toNanos)
      )
    }
    db.run(action).map(_ => authenticator)
  }

  override def remove(id: String): Future[Unit] = {
    val action = bearerTokenAuthenticators.filter(_.authenticatorId === id).delete
    db.run(action).map(_ => ())
  }
}
