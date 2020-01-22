package v1.user

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.db.evolutions.{Evolutions, ThisClassLoaderEvolutionsReader}
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{AnyContentAsEmpty, RequestHeader, Result}
import play.api.db.{DBApi, Database}
import play.api.test._
import play.api.test.Helpers._
import play.api.test.CSRFTokenHelper._
import utils.MyPostgresProfile
import v1.user.UserResource

import scala.concurrent.Future

class UserRouterSpec extends PlaySpec with BeforeAndAfter with GuiceOneAppPerSuite with Injecting {
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

  "UserRouter" should {

    "Return an empty list when there are no users" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/v1/users").withHeaders(
        HOST -> "localhost:9000",
        ACCEPT -> "application/json"
      )
      val home: Future[Result] = route(app, request).get

      val users: Seq[UserResource] = Json.fromJson[Seq[UserResource]](contentAsJson(home)).get
      users mustBe empty
    }
  }
}

//class UserRouterSpec extends PlaySpec with GuiceOneAppPerTest {
////  override def fakeApplication(): Application = {
////    GuiceApplicationBuilder().configure(
////      Map(
////        "slick.dbs.default.db.url" -> "jdbc:postgresql://localhost/shijianji_play_test"
////      )
////    ).build()
////  }
//
//  "User route" should {
//    "List all users" in {
//      val request = FakeRequest(GET, "/v1/users").withHeaders(HOST -> "localhost:9000").withCSRFToken
//      val users:Future[Result] = route(app, request).get
//
//      val users: Seq[UserResource] = Json.fromJson[Seq[UserResource]](contentAsJson(users)).get
//      users.filter(_.id == "1").head mustBe (PostResource("1","/v1/users/1", "title 1", "blog post 1" ))
//    }
////    import java.net._
////    val Some(result) = route(app, FakeRequest(GET, "/v1/users"))
////    val url = new URL("http://localhost:" + port + "/v1/users")
////    val con = url.openConnection().asInstanceOf[HttpURLConnection]
////    try con.getResponseCode mustBe 404
////    finally con.disconnect()
//  }
//}
