package modules

import com.google.inject.Provides
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.crypto.{Crypter, Signer}
import com.mohiva.play.silhouette.api.repositories.{AuthInfoRepository, AuthenticatorRepository}
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{RequestPart, _}
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators.{
  BearerTokenAuthenticator,
  BearerTokenAuthenticatorService,
  BearerTokenAuthenticatorSettings
}
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import dao.authentication.AuthenticatorRepositoryImpl
import dao._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import services.{AuthTokenService, AuthTokenServiceImpl, UserService, UserServiceImpl}
import utils.DefaultEnv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

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
    // Should it use `in[Singleton]` as well?
    bind[UsersDAO].to[UsersDAOImpl]
    bind[UserService].to[UserServiceImpl]
    // Silhouette related
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[Clock].toInstance(Clock())
    bind[java.time.Clock].toInstance(java.time.Clock.systemUTC())
    // Silhouette authentication
    bind[PasswordHasher].toInstance(new BCryptSha256PasswordHasher)
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAOImpl]
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
    authenticatorService: AuthenticatorService[BearerTokenAuthenticator],
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
    * @param passwordInfoDAOImpl The implementation of the delegable password auth info DAO.
    * @return The auth info repository instance.
    */
  @Provides
  def provideAuthInfoRepository(
    passwordInfoDAOImpl: DelegableAuthInfoDAO[PasswordInfo]
  ): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDAOImpl)
  }

  /**
    * Provides the password hasher registry.
    *
    * @return The password hasher registry.
    */
  @Provides
  def providePasswordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry = {
    PasswordHasherRegistry(passwordHasher, Seq.empty)
  }

  /**
    * Provides the credentials provider.
    *
    * @param authInfoRepository     The auth info repository implementation.
    * @param passwordHasherRegistry the password hasher registry.
    * @return The credentials provider.
    */
  @Provides
  def provideCredentialsProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry
  ): CredentialsProvider =
    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)

  /**
    * Provides the signer for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The signer for the authenticator.
    */
  @Provides
  @Named("authenticator-signer")
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")

    new JcaSigner(config)
  }

  /**
    * Provides the crypter for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the authenticator.
    */
  @Provides @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  // For reading bearer token settings
  implicit val requestPartReader: ValueReader[Option[Option[Seq[RequestPart.Value]]]] =
    ValueReader.relative { cfg =>
      if (cfg.getIsNull(".")) {
        Some(Some(Seq(RequestPart.Headers)))
      } else {
        Some(cfg.as[Option[Seq[RequestPart.Value]]])
      }
    }

  @Provides
  def provideAuthenticatorRepository(dbConfigProvider: DatabaseConfigProvider): AuthenticatorRepository[BearerTokenAuthenticator] = {
    new AuthenticatorRepositoryImpl(dbConfigProvider)
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
    bearerTokenAuthenticatorRepository: AuthenticatorRepository[BearerTokenAuthenticator],
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock
  ): AuthenticatorService[BearerTokenAuthenticator] = {
    // TODO fix requestPartReader so that we can use the line below
//    val bearerTokenSettings = configuration.underlying.as[BearerTokenAuthenticatorSettings](path = "silhouette.authenticator.token")
    val bearerTokenSettings = BearerTokenAuthenticatorSettings(
      fieldName = configuration.underlying.as[String](path = "silhouette.authenticator.token.fieldName"),
      authenticatorIdleTimeout =
        configuration.underlying.as[Option[FiniteDuration]](path = "silhouette.authenticator.token.authenticatorIdleTimeout"),
      authenticatorExpiry = configuration.underlying.as[FiniteDuration](path = "silhouette.authenticator.token.authenticatorExpiry")
    )
    new BearerTokenAuthenticatorService(bearerTokenSettings, bearerTokenAuthenticatorRepository, idGenerator, clock)
  }

  @Provides
  def providePasswordInfoDAO(dbConfigProvider: DatabaseConfigProvider) = {
    new PasswordInfoDAOImpl(dbConfigProvider)
  }
}
