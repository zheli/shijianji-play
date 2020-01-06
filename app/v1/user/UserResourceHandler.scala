package v1.user

import dao.UsersDAO
import javax.inject.{Inject, Provider}
import models.{User, UserId}
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying User information.
  */
case class UserResource(id: String, email: String, link: String)

object UserResource {
  implicit val format: Format[UserResource] = Json.format
}

/**
  * Controls access to the backend data, returning [[UserResource]]
  */
class UserResourceHandler @Inject()(routerProvider: Provider[UserRouter], userRepository: UserRepository, usersDao: UsersDAO)(
  implicit ec: ExecutionContext
) {

  private def createUserResource(p: User): UserResource = {
    UserResource(p.id.toString, p.email, routerProvider.get.link(p.id))
  }

  def find(implicit mc: MarkerContext): Future[Iterable[UserResource]] = {
    userRepository.list().map { UserDataList =>
      UserDataList.map(UserData => createUserResource(UserData))
    }
  }

  def create(userInput: UserFormInput)(implicit mc: MarkerContext): Future[UserResource] = {
    val data = User(UserId(999), userInput.email)
    usersDao.insert(data).map(_ => createUserResource(data))
  }
}
