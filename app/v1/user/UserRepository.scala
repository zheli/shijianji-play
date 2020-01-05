package v1.user

import javax.inject.{Inject, Singleton}
import play.api.{Logger, MarkerContext}
import storage.RepositoryExecutionContext

import scala.concurrent.Future

class UserId private (val value: Int) extends AnyVal {
  override def toString: String = value.toString
}
object UserId {
  def apply(raw: String) = {
    require(raw != null)
    new UserId(Integer.parseInt(raw))
  }

  def apply(id: Int) = new UserId(id)
}

final case class UserData(id: UserId, email: String)

trait UserRepository {
  def list()(implicit ec: MarkerContext): Future[Iterable[UserData]]
}

@Singleton
class UserRepositoryImpl @Inject()()(implicit ec: RepositoryExecutionContext)
  extends UserRepository {
  private val logger = Logger(this.getClass)

  private val userList = List(
    UserData(UserId(1), "test@test.com")
  )

  override def list()(implicit mc: MarkerContext): Future[List[UserData]] = {
    Future {
      logger.trace(s"list: ")
      userList
    }
  }
}

