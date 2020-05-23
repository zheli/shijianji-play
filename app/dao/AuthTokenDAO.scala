package dao

import java.util.UUID

import dao.AuthTokenDAOImpl._
import models.AuthToken

import scala.collection.mutable
import scala.concurrent.Future

/**
 * Manage AuthToken object in Database
 */
trait AuthTokenDAO {
  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken]
}

class AuthTokenDAOImpl extends AuthTokenDAO {
  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  override def save(token: AuthToken): Future[AuthToken] = {
    tokens += (token.id -> token)
    Future.successful(token)
  }
}

// TODO use postgres to save token
/**
 * The companion object.
 */
object AuthTokenDAOImpl {

  /**
   * The list of tokens.
   */
  val tokens: mutable.HashMap[UUID, AuthToken] = mutable.HashMap()
}