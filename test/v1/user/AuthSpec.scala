package v1.user

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.test._
import dao.UsersDAOImpl
import models.{Email, User}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.db.DBApi
import play.api.db.evolutions.{Evolutions, ThisClassLoaderEvolutionsReader}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import test.TestDbSpec
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
  * Test auth endpoint
  */
class AuthSpec extends PlaySpec with BeforeAndAfter with GuiceOneAppPerSuite with Injecting with TestDbSpec {

  import ThisClassLoaderEvolutionsReader.evolutions

  before {
    info("Running before() to setup")
    val db = inject[DBApi].database("default")
    Evolutions.applyEvolutions(db)
  }

  after {
    info("Running after() to clean up")
    val db = inject[DBApi].database("default")
    Evolutions.cleanupEvolutions(db)
  }

  implicit val ec = inject[ExecutionContext]

  def withAcceptJsonHeader[A](request: FakeRequest[A]) = request.withHeaders(
    HOST -> "localhost:9000",
    ACCEPT -> "application/json"
  )

  "auth" should {
    "create a new user from a successful post request" in {
      val usersDao = inject[UsersDAOImpl]
      val userEmail = "ha@ha.com"
      val requestBody = Json.obj("email" -> userEmail, "password" -> "test123")
      val request = withAcceptJsonHeader(FakeRequest(POST, "/v1/auth/sign-up")).withJsonBody(requestBody)
      val requestResult: Future[Result] = route(app, request).get
      cookies(requestResult) must not be empty
      (contentAsJson(requestResult) \ "email").as[String] mustEqual userEmail

      val users = await(usersDao.list())
      users.length mustBe 1
      users.head.email mustBe Email(userEmail)
    }
  }
}
