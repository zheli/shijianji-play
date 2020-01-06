package dao

import javax.inject.{Inject, Singleton}
import models.{User, UserId}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import utils.MyPostgresProfile

import scala.concurrent.{ExecutionContext, Future}

trait UsersComponent { self: HasDatabaseConfig[MyPostgresProfile] =>
  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[UserId]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")

    def * = (id, email) <> ((User.apply _).tupled, User.unapply)
  }
}

@Singleton
class UsersDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends UsersComponent
    with HasDatabaseConfigProvider[MyPostgresProfile] {
  import profile.api._

  val users = TableQuery[Users]

//  /** Construct the Map[String,String] needed to fill a select options set */
//  def options(): Future[Seq[(String, String)]] = {
//    val query = (for {
//      company <- users
//    } yield (company.id, company.name)).sortBy(/*name*/_._2)
//
//    db.run(query.result).map(rows => rows.map { case (id, name) => (id.toString, name) })
//  }

  /** Insert a new user */
  def insert(user: User): Future[Int] =
    db.run(users += user)

//  /** Insert new users */
//  def insert(users: Seq[Company]): Future[Unit] =
//    db.run(this.users ++= users).map(_ => ())

}
