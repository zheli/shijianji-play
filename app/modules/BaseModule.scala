package modules

import com.google.inject.Provides
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{Clock, FingerprintGenerator, IDGenerator, PasswordInfo}
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import dao.{UsersDAO, UsersDAOImpl}
import javax.inject._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.mvc.{Cookie, CookieHeaderEncoding}
import services.{UserService, UserServiceImpl}
import utils.DefaultEnv

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * The Guice `Main` module.
 * Sets up custom components for Play.
 * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
 */
//class modules.Module(environment: play.api.Environment, configuration: Configuration) extends ScalaModule {
class BaseModule extends ScalaModule {
  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[UsersDAO].to[UsersDAOImpl].in[Singleton] // TODO: not sure if Singleton is needed
    bind[UserService].to[UserServiceImpl]
    bind[Clock].toInstance(Clock())
    bind[java.time.Clock].toInstance(java.time.Clock.systemUTC())
  }

  /**
   * Provides the Silhouette environment.
   *
   * @param userService          The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus             The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    userService: UserService,
    authenticatorService: AuthenticatorService[CookieAuthenticator],
    eventBus: EventBus
  ): Environment[DefaultEnv] = {
    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
   * Provides the auth info repository.
   *
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @return The auth info repository instance.
   */
  @Provides
  def provideAuthInfoRepository(
    passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo]
  ): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDAO)
  }

  // For reading CookieAuthenticatorSettings
  implicit val sameSiteReader: ValueReader[Option[Cookie.SameSite]] =
    ValueReader.relative { cfg =>
      if (cfg.getIsNull(".")) {
        None
      } else {
        Some(Cookie.SameSite.parse(cfg.as[String]).getOrElse(throw new RuntimeException("Invalid SameSite value")))
      }
    }

  /**
   * Provides the authenticator service.
   *
   * @param signer The signer implementation.
   * @param crypter The crypter implementation.
   * @param cookieHeaderEncoding Logic for encoding and decoding `Cookie` and `Set-Cookie` headers.
   * @param fingerprintGenerator The fingerprint generator implementation.
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    @Named("authenticator-signer") signer: Signer,
    @Named("authenticator-crypter") crypter: Crypter,
    cookieHeaderEncoding: CookieHeaderEncoding,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock): AuthenticatorService[CookieAuthenticator] = {

    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(
      config, None, signer, cookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, idGenerator, clock
    )
  }
}
