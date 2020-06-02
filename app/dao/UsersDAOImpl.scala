package dao

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.{Inject, Singleton}
import models.{Email, User, UserId}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.{Logger, MarkerContext}
import utils.MyPostgresProfile

import scala.concurrent.{ExecutionContext, Future}

// See UserSerivceImpl.scala in play-silhouette-rest-lick-reactjs-typescript project
@Singleton
class UsersDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends UsersDAO
    with DBTableDefinitions
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

  /**
    * Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  override def save(user: User): Future[User] = {
    val dbUser = DBUser(id = None, email = user.email.value)
    val dbLoginInfo = DBLoginInfo(id = None, providerID = user.loginInfo.providerID, providerKey = user.loginInfo.providerKey)
    // We don't have the LoginInfo id so we try to get it first.
    // If there is no LoginInfo yet for this user we retrieve the id on insertion.
    val loginInfoAction = {
      val retrieveLoginInfo = loginInfos
        .filter(
          info =>
            info.providerID === user.loginInfo.providerID &&
              info.providerKey === user.loginInfo.providerKey
        )
        .result
        .headOption
      val insertLoginInfo = loginInfos
        .returning(loginInfos.map(_.id))
        .into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        loginInfo <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo)
      } yield loginInfo
    }
    // combine database actions to be run sequentially
    val actions = (for {
      userToSave <- (users.returning(users.map(_.id)).into((user, id) => user.copy(id = Some(id)))).insertOrUpdate(dbUser)
      loginInfo <- loginInfoAction
      _ <- userLoginInfos += DBUserLoginInfo(userToSave.get.id.get, loginInfo.id.get)
    } yield ()).transactionally
    db.run(actions).flatMap { _ =>
      // stupid way to get a user with id when saving
      find((user.loginInfo)).map(_.get)
    }
  }

  def findByEmail(email: String): Future[Option[User]] = {
    // TODO fix this
    Future(None)
  }

  override def find(userId: UserId): Future[Option[User]] = {
    logger.debug(s"find: $userId")
    // TODO fix this
    Future(None)
  }

  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- userLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- users.filter(_.id === dbUserLoginInfo.userId)
    } yield dbUser
    db.run(userQuery.result.headOption).map { maybeDbUser =>
      maybeDbUser.map { user =>
        User(
          user.id,
          loginInfo,
          Email(user.email)
        )
      }
    }
  }

  override def list(): Future[Seq[User]] = {
    val actions = for {
      user <- users
      userLoginInfo <- userLoginInfos if user.id === userLoginInfo.userId
      loginInfo <- loginInfos if userLoginInfo.loginInfoId === loginInfo.id
    } yield (user, loginInfo)
    db.run(actions.result).map { result =>
      result.map {
        case (dbUser: DBUser, dbLoginInfo: DBLoginInfo) =>
          User(
            id = dbUser.id,
            loginInfo = LoginInfo(providerID = dbLoginInfo.providerID, providerKey = dbLoginInfo.providerKey),
            email = Email(dbUser.email)
          )
      }
    }
  }
}
