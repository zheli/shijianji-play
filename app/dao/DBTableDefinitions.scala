package dao

import com.mohiva.play.silhouette.api.LoginInfo
import models.UserId
import play.api.db.slick.HasDatabaseConfigProvider
import utils.MyPostgresProfile

trait DBTableDefinitions {
  self: HasDatabaseConfigProvider[MyPostgresProfile] =>

  import profile.api._

  case class DBPasswordInfo(hasher: String, password: String, salt: Option[String], loginInfoId: Long)

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "PASSWORD_INFO") {
    def hasher = column[String]("HASHER")
    def password = column[String]("PASSWORD")
    def salt = column[Option[String]]("SALT")

    // TODO loginInfoId should be unique
    def loginInfoId = column[Long]("LOGIN_INFO_ID")

    def * = (hasher, password, salt, loginInfoId).<>(DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }
  val passwordInfos = TableQuery[PasswordInfos]

  case class DBLoginInfo(id: Option[Long], providerID: String, providerKey: String)

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "LOGIN_INFO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def providerID = column[String]("PROVIDER_ID")

    def providerKey = column[String]("PROVIDER_KEY")

    def * = (id.?, providerID, providerKey).<>(DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  val loginInfos = TableQuery[LoginInfos]

  def loginInfoQuery(loginInfo: LoginInfo) = {
    loginInfos.filter(dbLoginInfo => dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)
  }

  case class DBUser(id: Option[Long], email: String)

  class Users(tag: Tag) extends Table[DBUser](tag, "USER") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def email = column[String]("EMAIL")

    def * = (id.?, email).<>((DBUser.apply _).tupled, DBUser.unapply)
  }

  case class DBUserLoginInfo(userId: Long, loginInfoId: Long)

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "USER_LOGIN_INFO") {
    def userId = column[Long]("USER_ID")

    def loginInfoId = column[Long]("LOGIN_INFO_ID")

    def * = (userId, loginInfoId).<>(DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  val users = TableQuery[Users]
  val userLoginInfos = TableQuery[UserLoginInfos]
}
