package dao

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import utils.MyPostgresProfile

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.reflect.ClassTag

/**
  * An implementation of the auth info DAO which stores the data in database.
  * Use https://github.com/sbrunk/play-silhouette-slick-seed/blob/master/app/models/daos/PasswordInfoDAO.scala as reference
  */
class PasswordInfoDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit val classTag: ClassTag[PasswordInfo],
  ex: ExecutionContext
) extends DelegableAuthInfoDAO[PasswordInfo]
    with HasDatabaseConfigProvider[MyPostgresProfile]
    with DBTableDefinitions {
  val logger = Logger(getClass)

  import profile.api._

  protected def passwordInfoQuery(loginInfo: LoginInfo) = {
    for {
      dbLoginInfo: LoginInfos <- loginInfoQuery(loginInfo)
      // Not sure why this query doesn't work
      // dbPasswordInfo <- passwordInfos if dbPasswordInfo.loginInfoId == dbLoginInfo.id
      dbPasswordInfo <- passwordInfos.filter(_.loginInfoId === dbLoginInfo.id)
    } yield {
      logger.debug(s"dbLoginInfo $dbLoginInfo")
      logger.debug(s"dbPasswordInfo $dbPasswordInfo")
      dbPasswordInfo
    }
  }

  // Use subquery workaround instead of join to get authinfo because slick only supports selecting
  // from a single table for update/delete queries (https://github.com/slick/slick/issues/684).
  protected def passwordInfoSubQuery(loginInfo: LoginInfo) =
    passwordInfos.filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))

  protected def addAction(loginInfo: LoginInfo, authInfo: PasswordInfo) =
    loginInfoQuery(loginInfo).result.head.flatMap { dbLoginInfo =>
      passwordInfos +=
        DBPasswordInfo(authInfo.hasher, authInfo.password, authInfo.salt, dbLoginInfo.id.get)
    }.transactionally

  protected def updateAction(loginInfo: LoginInfo, authInfo: PasswordInfo) =
    passwordInfoQuery(loginInfo)
      .map(dbPasswordInfo => (dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt))
      .update((authInfo.hasher, authInfo.password, authInfo.salt))

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    db.run(passwordInfoQuery(loginInfo).result.headOption).map { dbPasswordInfoOption =>
      dbPasswordInfoOption.map(
        dbPasswordInfo => PasswordInfo(dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt)
      )
    }
  }

  override def add(
    loginInfo: LoginInfo,
    authInfo: PasswordInfo
  ): Future[PasswordInfo] = {
    // TODO check if authInfo for a loginInfo already exists?
    db.run(addAction(loginInfo, authInfo)).map(_ => authInfo)
  }

  override def update(
    loginInfo: LoginInfo,
    authInfo: PasswordInfo
  ): Future[PasswordInfo] = db.run(updateAction(loginInfo, authInfo)).map(_ => authInfo)

  override def save(
    loginInfo: LoginInfo,
    authInfo: PasswordInfo
  ): Future[PasswordInfo] = {
    val query = loginInfoQuery(loginInfo).joinLeft(passwordInfos).on(_.id === _.loginInfoId)
    val action = query.result.head.flatMap {
      case (_, Some(_)) => updateAction(loginInfo, authInfo)
      case (_, None) => addAction(loginInfo, authInfo)
    }
    db.run(action).map(_ => authInfo)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???
}
