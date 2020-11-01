package test

import org.scalatestplus.play.guice._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._

trait TestDbSpec {
  this: GuiceFakeApplicationFactory =>
  override def fakeApplication(): Application = {
    GuiceApplicationBuilder()
      .configure(
        Map(
          "slick.dbs.default.db.url" -> "jdbc:postgresql://localhost/shijianji_play_test"
//          "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;MODE=PostgreSQL"
        )
      )
      .build()
  }
}
