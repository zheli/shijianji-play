package models

import slick.lifted.MappedTo

/*
 * Always validate email address before saving it
 */
case class Email (value: String) extends MappedTo[String]
