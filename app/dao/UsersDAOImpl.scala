package dao

import javax.inject.{Inject, Singleton}
import models.{User, UserId}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.{Logger, MarkerContext}
import utils.MyPostgresProfile

import scala.concurrent.{ExecutionContext, Future}


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


  def insert(user: User): Future[User] = {
    val dbUser = DBUser(id=None, email=user.email.value)
    // Output user id after insert, see https://stackoverflow.com/questions/31443505/slick-3-0-insert-and-then-get-auto-increment-value
    val insertQuery = users returning users.map(_.id) into ((user, id) => user.copy(id = Some(id)))
    val action = insertQuery += dbUser
    db.run(action)
    // TODO fix this
    Future(user)
  }

  def findByEmail(email: String)(implicit mc: MarkerContext): Future[Option[User]] = {
    logger.trace(s"find: $email")
    db.run(users.filter(_.email === email).result.headOption)
    // TODO fix this
    Future(None)
  }

  def find(userId: UserId)(implicit mc: MarkerContext): Future[Option[User]] = {
    logger.debug(s"find: $userId")
//    db.run(users.filter(_.id === userId).result.headOption)
    // TODO fix this
    Future(None)
  }

  def list(): Future[Seq[User]] = {
//    db.run(users.result)
    // TODO fix this
    Future(Seq.empty[User])
  }
}
