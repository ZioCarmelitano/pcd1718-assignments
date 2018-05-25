package pcd.ass03.ex2.view

import akka.actor.{ActorPath, ActorRef}
import javafx.geometry.Pos
import pcd.ass03.ex2.actors.VisibleUser.Send
import scalafx.application.Platform
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.VBox
import scalafx.scene.text.Font


class ChatPresenter(messageField: TextField,
                    sendMessage: Button,
                    chatBox: VBox) {

  private var _user = ActorRef.noSender

  def send() {
    println("Sent message: " + messageField.text.value)
    _user ! Send(messageField.text.value)
    addMessage(Pos.CENTER_RIGHT, "Me", messageField.text.value)
  }

  def receive(content: String, senderPath: ActorPath) = {
    if (senderPath != _user.path) {
      println("Received message: " + content + "\nfrom " + senderPath)
      Platform.runLater {
        addMessage(Pos.CENTER_LEFT, senderPath.name, content)
      }
    }
  }

  def addMessage(position: Pos, sender: String, text: String): Unit = {
    val senderLabel = new Label(sender) {
      font = Font(13)
      prefWidth = 490
    }
    val messageLabel = new Label(text) {
      font = Font(20)
      prefWidth = 490
    }
    messageLabel setAlignment position
    senderLabel setAlignment position

    chatBox.children addAll(senderLabel, messageLabel)
  }

  def user_(value: ActorRef): Unit = _user = value
}
