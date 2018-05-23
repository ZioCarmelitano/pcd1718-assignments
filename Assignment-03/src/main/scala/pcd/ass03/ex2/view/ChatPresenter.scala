package pcd.ass03.ex2.view

import javafx.geometry.Pos
import pcd.ass03.ex2.GuiLauncher.{user, username}
import pcd.ass03.ex2.actors.User.Send
import scalafx.application.Platform
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.VBox
import scalafx.scene.text.Font


class ChatPresenter(messageField: TextField, sendMessage: Button, chatBox: VBox) {

  def send() {
    println("Sent message: " + messageField.text.value)
    user ! Send(messageField.text.value)
    addMessage(Pos.CENTER_RIGHT, "Me", messageField.text.value)
  }

  def receive(content: String, senderName: String) = {
    if (senderName != username) {
      println("Received message: " + content + "\nfrom " + senderName)
      Platform.runLater {
        addMessage(Pos.CENTER_LEFT, senderName, content)
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
}
