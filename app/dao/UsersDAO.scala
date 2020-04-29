package dao

import models.UserId
import play.api.db.slick.HasDatabaseConfig
import utils.MyPostgresProfile

trait UsersDAO { self: HasDatabaseConfig[MyPostgresProfile] =>
  import profile.api._

  case class DBUser(id: Option[Long], email: String)

  class Users(tag: Tag) extends Table[DBUser](tag, "USER") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("EMAIL")

    def * = (id.?, email) <> ((DBUser.apply _).tupled, DBUser.unapply)
  }

  case class DBUserLoginInfo(userId: UserId, loginInfoId: Long)

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "USER_LOGIN_INFO") {
    def userId = column[UserId]("USER_ID")
    def loginInfoId = column[Long]("LOGIN_INFO_ID")

    def * = (userId, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  val users = TableQuery[Users]
  val userLoginInfos = TableQuery[UserLoginInfos]

}
