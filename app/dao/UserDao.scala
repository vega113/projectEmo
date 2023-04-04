package dao

import anorm._
import dao.model.User
import dao.model.User._

import java.sql.Connection
import javax.inject.Inject

class UserDao @Inject()() {

  def findByUsername(username: String)(implicit conn: Connection): Option[User] = {
    SQL("SELECT * FROM users WHERE username = {username}")
      .on("username" -> username)
      .as(parser.singleOpt)
  }

  def findById(userId: Int)(implicit conn: Connection): Option[User] = {
    SQL("SELECT * FROM users WHERE user_id = {userId}")
      .on("userId" -> userId)
      .as(parser.singleOpt)
  }

  def findByEmail(email: String)(implicit conn: Connection): Option[User] = {
    SQL("SELECT * FROM users WHERE email = {email}")
      .on("email" -> email)
      .as(parser.singleOpt)
  }

  // get all users
  def findAll()(implicit conn: Connection): List[User] = {
    SQL("SELECT * FROM users")
      .as(parser.*)
  }

  def insert(user: User)(implicit conn: Connection): Option[Long] = {
    SQL("INSERT INTO users (username, password, first_name, last_name, email, is_password_hashed, created) " +
      "VALUES ({username}, {password}, {firstName}, {lastName}, {email}, {isPasswordHashed}, {created})")
      .on("username" -> user.username,
        "password" -> user.password,
        "firstName" -> user.firstName,
        "lastName" -> user.lastName,
        "email" -> user.email,
        "isPasswordHashed" -> user.isPasswordHashed,
        "created" -> user.created)
      .executeInsert()
  }

  def update(user: User)(implicit conn: Connection): Int = {
    SQL("UPDATE users SET username = {username}, password = {password}, first_name = {firstName}, last_name = {lastName}, email = {email}, " +
      "is_password_hashed = {isPasswordHashed}, created = {created} WHERE user_id = {userId}")
      .on("userId" -> user.userId,
        "username" -> user.username,
        "password" -> user.password,
        "firstName" -> user.firstName,
        "lastName" -> user.lastName,
        "email" -> user.email,
        "isPasswordHashed" -> user.isPasswordHashed,
        "created" -> user.created)
      .executeUpdate()
  }

  def delete(userId: Int)(implicit conn: Connection): Int = {
    SQL("DELETE FROM users WHERE user_id = {userId}")
      .on("userId" -> userId)
      .executeUpdate()
  }
}
