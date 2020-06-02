package dao

//import utils.withMyDatabase
import com.typesafe.config.{Config, ConfigFactory}
import models.User
import org.scalatest.Ignore
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.Logger
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.test.Helpers._
import play.api.test._
import play.test.WithApplication
import test.TestDbSpec
import v1.user.UserController

import scala.concurrent.Await
import scala.concurrent.duration._

@Ignore
class UsersDAOSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with TestDbSpec {

  "UsersController " must {

    "provide an Application" in {
      val controller = inject[UserController]
      val userIndex = controller.index().apply(FakeRequest(GET, "/"))
      status(userIndex) mustBe OK
      contentType(userIndex) mustBe Some("application/json")
      contentAsJson(userIndex) mustBe JsArray()
    }

    "create users" in {
      val controller = inject[UserController]
      val requestBody = Json.obj(
        "email" -> "ha@ha.com"
      )
      val createUser = controller.process().apply(FakeRequest(POST, "/").withJsonBody(requestBody))
      status(createUser) mustBe OK
      contentAsString(createUser) mustBe None
      contentType(createUser) mustBe Some("application/json")
      val users = controller.index().apply(FakeRequest(GET, "/"))
      contentAsJson(users) mustBe None
    }
  }
}
