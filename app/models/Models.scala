package models

import slick.lifted.MappedTo

case class UserId (value: Int) extends MappedTo[Int] {
  override def toString: String = value.toString
}

final case class User(id: UserId, email: Email)
