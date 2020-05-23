package services

import java.util.UUID

import com.mohiva.play.silhouette.api.util.Clock
import dao.AuthTokenDAO
import javax.inject.Inject
import models.AuthToken
import org.joda.time.DateTimeZone

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Handles actions to auth tokens.
  * See AuthTokenService in play-silhouette-seed
  */
trait AuthTokenService {

  /**
    * Creates a new auth token and saves it in the backing store.
    *
    * @param userID The user ID for which the token should be created.
    * @param expiry The duration a token expires.
    * @return The saved auth token.
    */
  def create(userID: Long, expiry: FiniteDuration = 5 minutes): Future[AuthToken]
}

/**
  * Handles actions to auth tokens.
  *
  * @param authTokenDAO The auth token DAO implementation.
  * @param clock        The clock instance.
  * @param ex           The execution context.
  */
class AuthTokenServiceImpl @Inject()(
  authTokenDAO: AuthTokenDAO,
  clock: Clock
)(implicit ex: ExecutionContext)
    extends AuthTokenService {
  override def create(userID: Long, expiry: FiniteDuration): Future[AuthToken] = {
    val token = AuthToken(UUID.randomUUID(), userID, clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt))
    authTokenDAO.save(token)
  }
}
