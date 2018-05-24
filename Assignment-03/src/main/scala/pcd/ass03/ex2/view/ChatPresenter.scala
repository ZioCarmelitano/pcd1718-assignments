package pcd.ass03.ex2.view

import javafx.geometry.Pos
import pcd.ass03.ex2.GuiLauncher.user
import pcd.ass03.ex2.actors.User.Send
import scalafx.application.Platform
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.VBox
import scalafx.scene.text.Font


class ChatPresenter(messageField: TextField, sendMessage: Button, chatBox: VBox) {

  def send(): Unit = {
    val content = messageField.text.value
    println("Sent content: " + content)
    user ! Send(content)
    addMessage(Pos.CENTER_RIGHT, "Me", content)
  }

  def receive(content: String, senderName: String): Unit = {
    println(s"Received message: $content\nFrom: $senderName")
    Platform.runLater {
      addMessage(Pos.CENTER_LEFT, senderName, content)
    }
  }

  def addMessage(position: Pos, sender: String, content: String): Unit = {
    val senderLabel = new Label(sender) {
      font = Font(13)
      prefWidth = 490
    }
    val messageLabel = new Label(content) {
      font = Font(20)
      prefWidth = 490
    }
    messageLabel setAlignment position
    senderLabel setAlignment position
    chatBox.children addAll(senderLabel, messageLabel)
  }
}
