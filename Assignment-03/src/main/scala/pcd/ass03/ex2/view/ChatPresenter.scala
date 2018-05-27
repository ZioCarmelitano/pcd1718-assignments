package pcd.ass03.ex2.view

import akka.actor.{ActorPath, ActorRef}
import javafx.geometry.Pos
import pcd.ass03.ex2.actors.User.{LockCheck, Send}
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.VBox
import scalafx.scene.text.Font
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class ChatPresenter(messageField: TextField, sendMessage: Button, chatBox: VBox) {

  private var _user = ActorRef.noSender

  def send() {
    val content = messageField.text.value
    implicit val timeout: Timeout = Timeout(2 seconds)
    val future = _user ? LockCheck

    def send_(lock: Boolean): Unit = if (lock) {
      _user ! Send(content)
    } else {
      Platform.runLater(() => addMessage(Pos.CENTER_RIGHT, _user.path.name, content))
      _user ! Send(content)
    }

    future.onComplete {
      case Success(lock) => send_(lock.asInstanceOf[Boolean])
      case Failure(t) => Platform.runLater{
        DialogUtils errorDialog("Error in sending message...",
          "An error has occurred:", t.getMessage)
      }
    }
  }

  def receive(content: String, senderPath: ActorPath): Unit = {
    println("Received message: " + content + "\nfrom " + senderPath)
    Platform.runLater {
      addMessage(Pos.CENTER_LEFT, senderPath.name, content)
    }
  }

  def receiveInfo(info: String): Unit = {
    Platform.runLater {
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
    margin = Insets(top = 6, right = 0, bottom  = 1,  left = 0)
  }

  def user_(value: ActorRef): Unit = _user = value
}


