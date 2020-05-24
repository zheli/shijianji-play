package v1.auth

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models._
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.{AuthTokenService, UserService}
import shared.controllers.ApiController
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

// Inspired by play-silhouette-rest-slick-reactjs-typescript & play-silhouette-seed
class SignUpController @Inject()(
  val controllerComponents: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  passwordHasherRegistry: PasswordHasherRegistry,
  userService: UserService
)(implicit ex: ExecutionContext)
    extends ApiController // Need ApiController only for ApiResponse() result.
    {

  def signUp = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(ApiResponse("auth.signIn.form.invalid", Messages("invalid.form"), form.errors))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(_) =>
            Future.successful(BadRequest(ApiResponse("auth.signUp.invalid", Messages("signup.userExists"))))

          case None =>
            val authInfo: PasswordInfo = passwordHasherRegistry.current.hash(data.password)
            val user = User(
              id = None,
              loginInfo = loginInfo,
              email = Email(data.email)
            )
            // TODO: add user activate email feature
            for {
              savedUser: User <- userService.save(user)
              _: PasswordInfo <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- silhouette.env.authenticatorService.create(loginInfo)
              cookie: Cookie <- silhouette.env.authenticatorService.init(authenticator)
              result: AuthenticatorResult <- silhouette.env.authenticatorService.embed(cookie, Created(Json.toJson(savedUser)))
            } yield result

        }
      }
    )
  }
}
