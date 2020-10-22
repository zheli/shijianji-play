package utils

import com.github.tminglei.slickpg._

/**
  * Enable Postgres specific features
  */
trait MyPostgresProfile
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgPlayJsonSupport
    with PgNetSupport
    with PgLTreeSupport
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport {

  override val pgjson = "jsonb"

  override val api = new API with ArrayImplicits with DateTimeImplicits with PlayJsonImplicits with NetImplicits with LTreeImplicits
  with RangeImplicits with HStoreImplicits with SearchImplicits with SearchAssistants {}
}

object MyPostgresProfile extends MyPostgresProfile
