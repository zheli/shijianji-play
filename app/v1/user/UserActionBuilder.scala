package v1.user

import javax.inject.Inject
import play.api.Logger
import play.api.http.HttpVerbs
import play.api.i18n.MessagesApi
import play.api.mvc._
import utils.RequestMarkerContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * A wrapped request for User resources.
 *
 * This is commonly used to hold request-specific information like
 * security credentials, and useful shortcut methods.
 */
trait UserRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
class UserRequest[A](request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with UserRequestHeader

/**
 * The action builder for the User resource.
 *
 * This is the place to put logging, metrics, to augment
 * the request with contextual data, and manipulate the
 * result.
 */
class UserActionBuilder @Inject()(messagesApi: MessagesApi, playBodyParsers: PlayBodyParsers)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] with RequestMarkerContext with HttpVerbs {
  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type UserRequestBlock[A] = UserRequest[A] => Future[Result]

  private val logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: UserRequestBlock[A]): Future[Result] = {
    implicit val markerContext = requestHeaderToMarkerContext(request)
    logger.trace(s"invokeBlock: ")

    val futureResult = block(new UserRequest(request, messagesApi))

    futureResult.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> "max-age: 100")

        case _ =>
          result
      }
    }
  }
}
