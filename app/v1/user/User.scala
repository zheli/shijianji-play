package v1.user

class UserId private (val value: Int) extends AnyVal {
  override def toString: String = value.toString
}

object UserId {
  def apply(raw: String) = {
    require(raw != null)
    new UserId(Integer.parseInt(raw))
  }

  def apply(id: Int) = new UserId(id)
}

final case class User(id: UserId, email: String)
