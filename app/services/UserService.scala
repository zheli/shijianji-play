package services

import com.mohiva.play.silhouette.api.services.IdentityService
import models.{User, UserId}

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {
  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param userId The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(userId: UserId): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]
}
