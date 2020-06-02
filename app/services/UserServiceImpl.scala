package services

import java.time.Clock

import com.mohiva.play.silhouette.api.LoginInfo
import dao.{UsersDAO, UsersDAOImpl}
import javax.inject.Inject
import models.{User, UserId}

import scala.concurrent.{ExecutionContext, Future}

/**
 *
 * Handles actions to users.
 *
 * @param usersDAO The user DAO implementation.
 * @param clock
 * @param ex
 */
class UserServiceImpl @Inject() (usersDAO: UsersDAO, clock: Clock)(
  implicit ex: ExecutionContext) extends UserService {
  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param userId The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  override def retrieve(userId: UserId): Future[Option[User]] = usersDAO.find(userId)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  override def save(user: User): Future[User] = usersDAO.save(user)

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = usersDAO.find(loginInfo)
}
