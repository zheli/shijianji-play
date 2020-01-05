package v1.user

import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}
import utils.RequestMarkerContext

case class UserControllerComponents @Inject()(
  postActionBuilder: UserActionBuilder,
  postResourceHandler: PostResourceHandler,
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

  def PostAction: PostActionBuilder = pcc.postActionBuilder

  def postResourceHandler: PostResourceHandler = pcc.postResourceHandler
}

class UserController extends UserBaseController {

}
