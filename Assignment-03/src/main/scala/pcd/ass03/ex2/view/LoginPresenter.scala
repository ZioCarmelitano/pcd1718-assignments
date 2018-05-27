package pcd.ass03.ex2.view

import java.util.regex.{Matcher, Pattern}

import pcd.ass03.ex2.view.DialogUtils.errorDialog
import pcd.ass03.ex2.view.LoginValidator.validateInput
import scalafx.scene.control.TextField

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/* It handles the event actions triggered by LoginView components */
class LoginPresenter(usernameField: TextField) {

  def login() = {
    def login_(username: String): Unit = validateInput(username) match {
      case Success(_) => new ChatView(username) show
      case Failure(t) => errorDialog("Input Error",
        "An error occurred in username validation", t.getMessage)
    }
    login_(usernameField.text.value)
  }
}


object LoginValidator{

  def validateInput(username: String): Try[Unit] = username match {
    case containsSpecialChars(_) =>
      Failure(IllegalCharsException("The input contains a special char not permitted (e.g. whitespace)"))
    case _  => Success()
  }

  private def containsSpecialChars(s: String): Boolean ={
    val pattern = Pattern compile "\\s|#"
    val matcher = pattern.matcher(s)
    matcher find
  }

  /* Extractor object used to enable pattern matching */
  private object containsSpecialChars {
    def unapply(str: String): Option[Unit] = if (containsSpecialChars(str)) Some() else None
  }

  final case class IllegalCharsException(error: String) extends Exception(error)
}


