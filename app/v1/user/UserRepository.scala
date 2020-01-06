//package v1.user
//
//import javax.inject.{Inject, Singleton}
//import models.{User, UserId}
//import play.api.{Logger, MarkerContext}
//import storage.RepositoryExecutionContext
//
//import scala.concurrent.Future
//
//trait UserRepository {
//  def list()(implicit ec: MarkerContext): Future[Iterable[User]]
//}
//
//@Singleton
//class UserRepositoryImpl @Inject()()(implicit ec: RepositoryExecutionContext)
//  extends UserRepository {
//  private val logger = Logger(this.getClass)
//
//  private val userList = List(
//    User(UserId(1), "test@test.com")
//  )
//
//  override def list()(implicit mc: MarkerContext): Future[List[User]] = {
//    Future {
//      logger.trace(s"list: ")
//      userList
//    }
//  }
//}
//
