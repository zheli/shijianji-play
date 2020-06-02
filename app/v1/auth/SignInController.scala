package v1.auth

import java.time.Clock

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.json.CredentialsFormat
import javax.inject.Inject
import models.User
import net.ceedubs.ficus.Ficus._
import org.joda.time.DateTime
import play.api.Configuration
import play.api.i18n.Messages
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Cookie, Request, RequestHeader, Result}
import services.UserService
import shared.controllers.ApiController
import utils.DefaultEnv

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

// Inspired by play-silhouette-seed
class SignInController @Inject()(
  val controllerComponents: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  credentialsProvider: CredentialsProvider,
  configuration: Configuration,
  clock: Clock
)(implicit ex: ExecutionContext)
    extends ApiController {

  implicit val credentialsFormat = CredentialsFormat.emailAsIdentifierFormat

  /**
    * Sign in a user.
    *
    * @return A Play result.
    */
  def signIn = silhouette.UnsecuredAction.async(parse.json[Credentials]) { implicit request =>
    //  def signIn = silhouette.UnsecuredAction.async { implicit request =>
    logger.debug(s"Request: $request")
    val credentials = Credentials(request.body.identifier, request.body.password)
    credentialsProvider
      .authenticate(credentials)
      .flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            silhouette.env.authenticatorService
              .create(loginInfo)
              .map {
                case authenticator => authenticator
              }
              .flatMap { authenticator: CookieAuthenticator =>
                //TODO enable eventBus
                //silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService
                  .init(authenticator)
                  .flatMap { token: Cookie =>
                    silhouette.env.authenticatorService
                      .embed(token, Ok(""))
                  }
              }

          case None =>
            Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
      .recover {
        case _: ProviderException =>
          BadRequest
      }
  }

  /**
    * Handles the active user.
    *
    * @param user       The active user.
    * @param loginInfo  The login info for the current authentication.
    * @param rememberMe True if the cookie should be a persistent cookie, false otherwise.
    * @param request    The current request header.
    * @return A Play result.
    */
  private def handleActiveUser(
    user: User,
    loginInfo: LoginInfo,
    rememberMe: Boolean
  )(implicit request: RequestHeader): Future[Result] = {
    import models.User._

    silhouette.env.authenticatorService
      .create(loginInfo)
      .map(configureAuthenticator(rememberMe, _))
      .flatMap { authenticator =>
        silhouette.env.eventBus.publish(LoginEvent(user, request))
        silhouette.env.authenticatorService.init(authenticator).flatMap { cookie =>
          silhouette.env.authenticatorService.embed(
            cookie,
            Ok(
              ApiResponse(
                "auth.signIn.successful",
                Messages("auth.signed.in"),
                Json.toJson(user)
              )
            )
          )
        }
      }
  }

  /**
    * Changes the default authenticator config if the remember me flag was activated during sign-in.
    *
    * @param rememberMe    True if the cookie should be a persistent cookie, false otherwise.
    * @param authenticator The authenticator instance.
    * @return The changed authenticator if the remember me flag was activated, otherwise the unchanged authenticator.
    */
  private def configureAuthenticator(rememberMe: Boolean, authenticator: DefaultEnv#A): DefaultEnv#A = {
    if (rememberMe) {
      val c = configuration.underlying
      val configPath = "silhouette.authenticator.rememberMe"
      val authenticatorExpiry = c.as[FiniteDuration](s"$configPath.authenticatorExpiry").toMillis
      val instant = clock.instant().plusMillis(authenticatorExpiry)
      val expirationDateTime = new DateTime(instant.toEpochMilli)

      authenticator.copy(
        expirationDateTime = expirationDateTime,
        idleTimeout = c.getAs[FiniteDuration](s"$configPath.authenticatorIdleTimeout"),
        cookieMaxAge = c.getAs[FiniteDuration](s"$configPath.cookieMaxAge")
      )
    } else {
      authenticator
    }
  }
}
