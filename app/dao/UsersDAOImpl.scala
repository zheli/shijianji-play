package dao

import javax.inject.{Inject, Singleton}
import models.{Email, User, UserId}
import play.api.{Logger, MarkerContext}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import utils.MyPostgresProfile

import scala.concurrent.{ExecutionContext, Future}

trait UsersDAO { self: HasDatabaseConfig[MyPostgresProfile] =>
  import profile.api._

  case class DBUser(id: Option[Long], email: String)

  class Users(tag: Tag) extends Table[DBUser](tag, "USER") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("EMAIL")

    def * = (id.?, email) <> ((DBUser.apply _).tupled, DBUser.unapply)
  }

  case class DBLoginInfo(id: Option[Long], providerID: String, providerKey: String)

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "LOGIN_INFO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def providerId = column[String]("PROVIDER_ID")
    def providerKey = column[String]("PROVIDER_KEY")

    def * = (id.?, providerId, providerKey) <> (DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  case class DBUserLoginInfo(userId: UserId, loginInfoId: Long)

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "USER_LOGIN_INFO") {
    def userId = column[UserId]("USER_ID")
    def loginInfoId = column[Long]("LOGIN_INFO_ID")

    def * = (userId, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  case class DBPasswordInfo(hasher: String, password: String, salt: Option[String], loginInfoId: Long)

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "PASSWORD_INFO") {
    def hasher = column[String]("HASHER")
    def password = column[String]("PASSWORD")
    def salt = column[Option[String]]("SALT")
    def loginInfoId = column[Long]("LOGIN_INFO_ID")

    def * = (hasher, password, salt, loginInfoId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  val users = TableQuery[Users]
  val loginInfos = TableQuery[LoginInfos]
  val userLoginInfos = TableQuery[UserLoginInfos]
  val passwordInfos = TableQuery[PasswordInfos]

}

@Singleton
class UsersDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends UsersDAO
    with HasDatabaseConfigProvider[MyPostgresProfile] {
  private val logger = Logger(this.getClass)

  import profile.api._
//  /** Construct the Map[String,String] needed to fill a select options set */
//  def options(): Future[Seq[(String, String)]] = {
//    val query = (for {
//      company <- users
//    } yield (company.id, company.name)).sortBy(/*name*/_._2)
//
//    db.run(query.result).map(rows => rows.map { case (id, name) => (id.toString, name) })
//  }

  val insertQuery = users returning users.map(_.id) into ((user, id) => user.copy(id = id))

  def insert(user: User): Future[User] = {
    // Output user id after insert, see https://stackoverflow.com/questions/31443505/slick-3-0-insert-and-then-get-auto-increment-value
    val action = insertQuery += user
    db.run(action)
  }

  def findByEmail(email: String)(implicit mc: MarkerContext): Future[Option[User]] = {
    logger.trace(s"find: $email")
    db.run(users.filter(_.email === Email(email)).result.headOption)
  }

  def find(userId: UserId)(implicit mc: MarkerContext): Future[Option[User]] = {
    logger.trace(s"find: $userId")
    db.run(users.filter(_.id === userId).result.headOption)
  }

  def list(): Future[Seq[User]] = db.run(users.result)
}
