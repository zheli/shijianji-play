package v1.user

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import utils.RequestMarkerContext

import scala.concurrent.{ExecutionContext, Future}

case class UserFormInput(email: String)

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

  private val form: Form[UserFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "email" -> email
      )(UserFormInput.apply)(UserFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = UserAction.async { implicit request =>
    logger.trace("index: ")
    userResourceHandler.list.map { users =>
      Ok(Json.toJson(users))
    }
  }

  def findByEmail(email: String): Action[AnyContent] = UserAction.async { implicit request =>
    logger.trace(s"findByEmail: ${email}")
    userResourceHandler.find(email).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => Ok("{}")
    }
  }

  def process: Action[AnyContent] = UserAction.async { implicit request =>
    logger.trace(s"process: $request")
    processJsonPost()
  }

  private def processJsonPost[A]()(
    implicit request: UserRequest[A]): Future[Result] = {
    def failure(badForm: Form[UserFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: UserFormInput) = {
      userResourceHandler.create(input).map { post =>
        Created(Json.toJson(post)).withHeaders(LOCATION -> post.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
