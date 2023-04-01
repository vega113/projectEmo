package dao

import anorm._
import dao.model.User

import java.sql.Connection
import javax.inject.Inject

class UserDao @Inject()() {

  def findAll()(implicit connection: Connection): List[User] = {
    SQL("SELECT * FROM users").as(User.parser.*)
  }

  def findById(id: Int)(implicit connection: Connection): Option[User] = {
    SQL("SELECT * FROM users WHERE id = {id}").on("id" -> id).as(User.parser.singleOpt)
  }

  def insert(user: User)(implicit connection: Connection): Option[Long] = {
    val id: Option[Long] = SQL("INSERT INTO users (username, email, password) VALUES ({username}, {email}, {password})")
      .on(
        "username" -> user.username,
        "email" -> user.email,
        "password" -> user.password
      ).executeInsert()
    id
  }

  def update(user: User)(implicit connection: Connection): Int = {
    SQL("UPDATE users SET username = {username}, email = {email}, password = {password} WHERE id = {id}")
      .on(
        "id" -> user.id,
        "username" -> user.username,
        "email" -> user.email,
        "password" -> user.password
      ).executeUpdate()
  }

  def delete(id: Int)(implicit connection: Connection): Int = {
    SQL("DELETE FROM users WHERE id = {id}").on("id" -> id).executeUpdate()
  }
}
