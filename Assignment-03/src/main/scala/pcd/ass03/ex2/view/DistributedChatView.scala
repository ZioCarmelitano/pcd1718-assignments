package pcd.ass03.ex2.view

import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ScrollPane, TextField}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, GridPane, VBox}
import scalafx.scene.text.Font

class DistributedChatView extends PrimaryStage{

  private val appTitle = "Distributed Chat"
  private val appLogoPath = "/distributed_chat_logo.png"
  private val sendLogoPath = "/send_button_logo.png"

  private val messageField: TextField = new TextField{
    prefWidth = 450
    onAction = (_) => presenter send()
  }

  private val sendMessage: Button = new Button{
    onAction = (_) => presenter send()
    graphic = new ImageView {image = new Image(this, sendLogoPath)
      fitWidth = 20.0
      fitHeight = 20.0
    }
  }

  private val chatBox = new VBox()

  private val chatBoxContainer: ScrollPane = new ScrollPane {
    prefWidth = 500
    prefHeight = 425
    content = chatBox
  }

  private val titleLabelContainer: BorderPane = new BorderPane{
    center = new Label(appTitle){
      font = Font(20)
    }
    prefHeight = 30
    prefWidth = 500
  }

  private val commandContainer: GridPane = new GridPane {
    padding = Insets(5)
    add(messageField, 0, 0)
    add(sendMessage, 1, 0)
  }

  private val presenter = new ChatPresenter(messageField, sendMessage, chatBox)

  title = appTitle

  private val VIEW_WIDTH = 500
  private val VIEW_HEIGHT = 500

  scene = new Scene(VIEW_WIDTH,VIEW_HEIGHT){
    content = new BorderPane {
      top = titleLabelContainer
      center = chatBoxContainer
      bottom = commandContainer
    }
  }

  this getIcons() add new Image(appLogoPath)
}
