package v1.user

import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import utils.RequestMarkerContext

import scala.concurrent.ExecutionContext

case class UserControllerComponents @Inject()(
  userActionBuilder: UserActionBuilder,
  userResourceHandler: UserResourceHandler,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: scala.concurrent.ExecutionContext
) extends ControllerComponents
/**
 * Exposes actions and handler to the PostController by wiring the injected state into the base class.
 */
class UserBaseController(pcc: UserControllerComponents) extends BaseController with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = pcc

  def UserAction: UserActionBuilder = pcc.userActionBuilder

  def userResourceHandler: UserResourceHandler = pcc.userResourceHandler
}

/**
 * Takes HTTP requests and produces JSON.
 */
class UserController @Inject()(cc: UserControllerComponents)(implicit ec: ExecutionContext) extends UserBaseController(cc) {
  private val logger = Logger(getClass)

  def index: Action[AnyContent] = UserAction.async { implicit request =>
    logger.trace("index: ")
    userResourceHandler.find.map { posts =>
      Ok(Json.toJson(posts))
    }
  }
}
