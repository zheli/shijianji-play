package v1.auth

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the submission of the sign up
 */
object SignUpForm {
  val form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param email The email of the user.
   * @param password The password of the user.
   */
  case class Data(
    email: String,
    password: String
  )
}
