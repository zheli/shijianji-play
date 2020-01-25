package v1.user

import dao.UsersDAO
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.db.evolutions.{Evolutions, ThisClassLoaderEvolutionsReader}
import play.api.libs.json.{JsArray, JsObject, JsResult, JsValue, Json}
import play.api.mvc.{AnyContent, AnyContentAsEmpty, AnyContentAsJson, RequestHeader, Result}
import play.api.db.{DBApi, Database}
import play.api.test._
import play.api.test.Helpers._
import play.api.test.CSRFTokenHelper._
import utils.MyPostgresProfile
import v1.user.UserResource

import scala.concurrent.Future

class UserRouterSpec extends PlaySpec with BeforeAndAfter with GuiceOneAppPerSuite with Injecting {
  import ThisClassLoaderEvolutionsReader.evolutions

  def withAcceptJsonHeader[A](request: FakeRequest[A]) = request.withHeaders(
    HOST -> "localhost:9000",
    ACCEPT -> "application/json"
  )

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

  "UserRouter" should {

    "Return an empty list when there are no users" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = withAcceptJsonHeader(FakeRequest(GET, "/v1/users"))
      val requestResult: Future[Result] = route(app, request).get
      val users: Seq[UserResource] = Json.fromJson[Seq[UserResource]](contentAsJson(requestResult)).get
      users mustBe empty
    }

    "Create a new user from a successful post request" in {
      val userEmail = "ha@ha.com"
      val requestBody = Json.obj("email" -> userEmail)
      val request: FakeRequest[AnyContentAsJson] = withAcceptJsonHeader(FakeRequest(POST, "/v1/users")).withJsonBody(requestBody)
      val requestResult = route(app, request).get
      await(requestResult)

      val usersDao = inject[UsersDAO]
      val users = await(usersDao.list())

      users.length mustBe 1
      users.head.email mustBe "ha@ha.com"
    }

    "Create multiple users" in {
      val emails: Seq[String] = for (n <- 1 to 3 ) yield s"ha$n@ha.com"

      emails.foreach { email =>
        val requestBody = Json.obj("email" -> email)
        val request: FakeRequest[AnyContentAsJson] = withAcceptJsonHeader(FakeRequest(POST, "/v1/users")).withJsonBody(requestBody)
        val requestResult = route(app, request).get
        await(requestResult)
      }

      val usersDao = inject[UsersDAO]
      val users = await(usersDao.list())

      users.length mustBe emails.length
      users.map(_.email) mustBe emails
    }
  }

}
