package pcd.ass03.ex2.view

import java.util.regex.Pattern

import akka.actor.ActorSystem
import pcd.ass03.ex2.actors.{Room, User}
import scalafx.application.Platform
import pcd.ass03.ex2.view.DialogUtils.errorDialog
import pcd.ass03.ex2.view.LoginValidator.validateInput
import scalafx.scene.control.TextField

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/* It handles the event actions triggered by LoginView components */
class LoginPresenter(usernameField: TextField) {

  def login(): Unit = {
    def validateAndLogin(username: String): Unit = Try(validateInput(username)) match {
      case Success(_) => checkRoomAndLogin(username)
      case Failure(t) => errorDialog(dialogTitle = "Input Error",
        header = "An error occurred in username validation",
        content = t.getMessage)
    }

    validateAndLogin(usernameField.text.value)
  }

  /* Check that the room is reachable and if so, launch the chat view */
  def checkRoomAndLogin(username: String): Unit = {
    val fs: FiniteDuration = 5.seconds // Timeout for the resolveOne call
    ActorSystem("User", User.Config).actorSelection(Room.Path).resolveOne(fs).onComplete {
      case Failure(_) => Platform runLater errorDialog(dialogTitle = "Connection Error",
        header = "An error occurred connecting to chat room",
        content = "Check your connection and the reachability to the chat room")
      case _ => Platform runLater (() => new ChatView(username) show)
    }
  }
}

object LoginValidator {

  def validateInput(username: String): Try[Unit] = username match {
    case specialCharsCheck() => Success()
    case noInput() => throw NoInputException("Please insert your username")
    case _ => throw IllegalCharsException("The input contains a special char not permitted (e.g. whitespace)")
  }

  private def notContainSpecialChars(s: String): Boolean = {
    val pattern = Pattern compile "[\\w]+"
    val matcher = pattern.matcher(s)
    matcher matches
  }

  /* Extractor object used to enable pattern matching */
  private object specialCharsCheck {
    def unapply(str: String): Boolean = notContainSpecialChars(str)
  }

  private object noInput {
    def unapply(str: String): Boolean = str.isEmpty
  }

  final case class IllegalCharsException(error: String) extends Exception(error)
  final case class NoInputException(error: String) extends Exception(error)
}
