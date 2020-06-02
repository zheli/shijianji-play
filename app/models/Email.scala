package models

import play.api.libs.json._
import slick.lifted.MappedTo

/*
 * Always validate email address before saving it
 */
case class Email(value: String) extends MappedTo[String]

object Email {
  implicit val formatter = Format[Email](
    __.read[String].map(email => Email(email)),
    new Writes[Email] {
      def writes(email: Email) = JsString(email.value)
    }
  )
}
