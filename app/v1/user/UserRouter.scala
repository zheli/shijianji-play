package v1.user

import javax.inject.Inject
import models.UserId
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class UserRouter @Inject() (controller: UserController) extends SimpleRouter {
  val prefix = "/v1/Users"

  def link(id: Option[Long]): String = {
    import io.lemonlabs.uri.dsl._
    val url = prefix / id.toString
    url.toString
  }

  override def routes: Routes = {
    case GET(p"/" ? q_?"email=$email") => email match {
      case Some(email) =>
        controller.findByEmail(email)
      case None =>
        controller.index
    }

    case POST(p"/") =>
      controller.process
  }
}
