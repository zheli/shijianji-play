package v1.user

import javax.inject.{Inject, Provider}
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
class UserResourceHandler @Inject()(
    routerProvider: Provider[UserRouter],
    UserRepository: UserRepository)(implicit ec: ExecutionContext) {

  private def createUserResource(p: UserData): UserResource = {
    UserResource(p.id.toString, p.email, routerProvider.get.link(p.id))
  }

  def find(implicit mc: MarkerContext): Future[Iterable[UserResource]] = {
    UserRepository.list().map { UserDataList =>
      UserDataList.map(UserData => createUserResource(UserData))
    }
  }
}
