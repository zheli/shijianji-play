package dao

import com.mohiva.play.silhouette.api.LoginInfo
import models.{User, UserId}
import play.api.db.slick.HasDatabaseConfig
import utils.MyPostgresProfile

import scala.concurrent.Future

trait UsersDAO {
  self: HasDatabaseConfig[MyPostgresProfile] =>

  import profile.api._

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]]
}
