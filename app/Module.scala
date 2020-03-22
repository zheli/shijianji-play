import javax.inject._
import com.google.inject.AbstractModule
import dao.{UsersDAO, UsersDAOImpl}
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import v1.user._

/**
 * Sets up custom components for Play.
 *
 * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
 */
class Module(environment: Environment, configuration: Configuration) extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[UsersDAO].to[UsersDAOImpl].in[Singleton]
  }
}
