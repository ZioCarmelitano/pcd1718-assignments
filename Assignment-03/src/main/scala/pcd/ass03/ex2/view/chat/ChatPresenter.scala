package pcd.ass03.ex2.view.chat

import akka.actor.{ActorPath, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import javafx.geometry.Pos
import pcd.ass03.ex2.actors.Room.CriticalSection
import pcd.ass03.ex2.actors.User.{LockCheck, Send}
import pcd.ass03.ex2.view.chat.MessageValidator.validateInput
import pcd.ass03.ex2.view.utils.DialogUtils.errorDialog
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.VBox
import scalafx.scene.text.Font

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


class ChatPresenter(messageField: TextField, sendMessage: Button, chatBox: VBox) {

  private var _user = ActorRef.noSender

  def send() {

    def validateAndSend(message: String): Unit = Try(validateInput(message)) match {
      case Success(_) => send_(message)
      case Failure(t) => errorDialog(dialogTitle = "Input Error",
        header = "An error occurred in message validation",
        content = t.getMessage)
    }

    def send_(message: String): Unit = {
      implicit val timeout: Timeout = Timeout(2 seconds)
      val future = _user ? LockCheck
      future.onComplete {
        case Success(CriticalSection(u)) =>
          if (u == _user) _user ! Send(message)
          else Platform runLater errorDialog("Error in sending message...",
            "Critical section is held by " + u.path.name,
            "You cannot send a message while critical section is enabled")
        case Success(_) => _user ! Send(message)
        case Failure(t) => Platform runLater {
          errorDialog("Error in sending message...",
            "An error has occurred:", t.getMessage)
        }
      }
    }

    val content = messageField.text.value
    validateAndSend(content)
  }

  def receive(content: String, senderPath: ActorPath): Unit = {
    println("Received message: " + content + "\nfrom " + senderPath)
    Platform runLater {
      val pos = if (senderPath == _user.path) Pos.CENTER_RIGHT else Pos.CENTER_LEFT
      addMessage(pos, senderPath.name, content)
    }
  }

  def receiveInfo(info: String): Unit = {
    Platform runLater {
      addInfoMessage(info)
    }
  }

  def addMessage(position: Pos, sender: String, text: String): Unit = text match {
    case ":enter-cs" => addInfoMessage(sender + " entered in critical section!")
    case ":exit-cs" => addInfoMessage(sender + " exited from critical section")
    case _ => addUserMessage(position, sender, text)
  }

  private def addUserMessage(position: Pos, sender: String, text: String) = {
    val senderLabel = createInfoLabel(sender)
    val messageLabel = new Label(text) {
      font = Font(size = 20)
      prefWidth = 490
    }
    messageLabel setAlignment position
    senderLabel setAlignment position
    chatBox.children addAll(senderLabel, messageLabel)
  }

  private def addInfoMessage(info: String): Unit = {
    val senderLabel = createInfoLabel(info)
    senderLabel setAlignment Pos.CENTER_RIGHT
    chatBox.children add senderLabel
  }

  private def createInfoLabel(info: String) = new Label(info) {
    font = Font(size = 13)
    prefWidth = 490
    margin = Insets(top = 6, right = 0, bottom = 1, left = 0)
  }

  def user_(value: ActorRef): Unit = _user = value
}

object MessageValidator {
  val MaxMessageSize = 50

  def validateInput(content: String): Try[Unit] = content match {
    case CharLimitExceeded() => throw MessageLimitException("You have exceeded the max length allowed for messages")
    case _ => Success()
  }

  object CharLimitExceeded {
    def unapply(str: String): Boolean = str.length > MaxMessageSize
  }

  final case class MessageLimitException(error: String) extends Exception(error)

}
