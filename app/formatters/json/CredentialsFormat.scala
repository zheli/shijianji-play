package formatters.json

import com.mohiva.play.silhouette.api.util.Credentials
import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * formatter for silhouette Credentials model
  */
object CredentialsFormat {
  implicit val emailAsIdentifierFormat = ((__ \ "email").format[String] ~
    (__ \ "password").format[String])(Credentials.apply, unlift(Credentials.unapply))
}
