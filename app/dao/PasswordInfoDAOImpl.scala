package dao

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import utils.MyPostgresProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
 * An implementation of the auth info DAO which stores the data in database.
 */
class PasswordInfoDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  (
    implicit val classTag: ClassTag[PasswordInfo],
    ex: ExecutionContext
  ) extends DelegableAuthInfoDAO[PasswordInfo] with HasDatabaseConfigProvider[MyPostgresProfile] with DBTableDefinitions {

  import profile.api._

  protected def passwordInfoQuery(loginInfo: LoginInfo) = for {
    dbLoginInfo <- loginInfoQuery(loginInfo)
      dbPasswordInfo <- passwordInfos if dbPasswordInfo.loginInfoId == dbLoginInfo.id
  } yield dbPasswordInfo

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    db.run(passwordInfoQuery(loginInfo).result.headOption).map { dbPasswordInfoOption =>
      dbPasswordInfoOption.map(
        dbPasswordInfo =>
          PasswordInfo(dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt)
      )
    }
  }

  override def add(
    loginInfo: LoginInfo,
    authInfo: PasswordInfo
  ): Future[PasswordInfo] = ???

  override def update(
    loginInfo: LoginInfo,
    authInfo: PasswordInfo
  ): Future[PasswordInfo] = ???

  override def save(
    loginInfo: LoginInfo,
    authInfo: PasswordInfo
  ): Future[PasswordInfo] = ???

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???
}
