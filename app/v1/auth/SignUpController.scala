package v1.auth

import com.mohiva.play.silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import javax.inject.Inject
import models._
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request}
import services.{AuthTokenService, UserService}
import shared.controllers.ApiController
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

// Inspired by play-silhouette-seed
//class SignUpController @Inject() (
//  components: SilhouetteControllerComponents
//)(implicit ex: ExecutionContext) extends SilhouetteController(components) {
//
//}

// Inspired by play-silhouette-rest-slick-reactjs-typescript & play-silhouette-seed
class SignUpController @Inject() (
  val controllerComponents: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  passwordHasherRegistry: PasswordHasherRegistry,
  userService: UserService
)(implicit ex: ExecutionContext) extends ApiController {
  import models.AuthToken._

  def signUp = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(ApiResponse("auth.signIn.form.invalid", Messages("invalid.form"), form.errors))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Future.successful(BadRequest(ApiResponse("auth.signUp.invalid", Messages("signup.userExists"))))

          case None =>
            val authInfo: PasswordInfo = passwordHasherRegistry.current.hash(data.password)
            val user = User(
              id = None,
              loginInfo = loginInfo,
              email = Email(data.email)
            )
            for {
              savedUser: User <- userService.save(user)
              authInfo: PasswordInfo <- authInfoRepository.add(loginInfo, authInfo)
              authToken: AuthToken <- authTokenService.create(savedUser.id.get)
            } yield {
              Created(ApiResponse("auth.signUp.successful", Messages("signup.successful"), Json.toJson(authToken)))
            }

        }
      }
    )
  }
}