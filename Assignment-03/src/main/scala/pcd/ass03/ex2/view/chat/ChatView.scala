package pcd.ass03.ex2.view.chat

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import pcd.ass03.ex2.actors.User
import pcd.ass03.ex2.actors.User.Kill
import pcd.ass03.ex2.view.chat.ChatView.{appLogoPath, sendLogoPath}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, GridPane, VBox}
import scalafx.scene.text.Font

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/* It defines the structure of the chat view. */
class ChatView(username: String) extends PrimaryStage {

  private val appTitle = username + " - Chat Room"

  /* GUI Components creation */
  private val messageField: TextField = new TextField {
    prefWidth = 450
    onAction = _ => {
      presenter send()
      text = ""
    }
  }

  private val sendMessage: Button = new Button {
    graphic = new ImageView {
      image = new Image(this, sendLogoPath)
      fitWidth = 20.0
      fitHeight = 20.0
    }
    onAction = _ => {
      presenter send()
      messageField clear()
    }
  }

  private val chatBox = new VBox()

  private val chatBoxContainer: ScrollPane = new ScrollPane {
    prefWidth = 500
    prefHeight = 425
    content = chatBox
  }

  private val titleLabelContainer: BorderPane = new BorderPane {
    center = new Label(appTitle) {
      font = Font(size = 20)
    }
    prefHeight = 30
    prefWidth = 500
  }

  private val commandContainer: GridPane = new GridPane {
    padding = Insets(topRightBottomLeft = 5)
    add(messageField, columnIndex = 0, rowIndex = 0)
    add(sendMessage, columnIndex = 1, rowIndex = 0)
  }

  /* Creation of the associated presenter */
  private val _presenter = new ChatPresenter(messageField, sendMessage, chatBox)

  title = appTitle

  /* Scene graph creation */
  scene = new Scene(width = 500, height = 500) {
    content = new BorderPane {
      top = titleLabelContainer
      center = chatBoxContainer
      bottom = commandContainer
    }
    onCloseRequest = _ => {
      implicit val timeout: Timeout = Timeout(2 seconds)
      val future = user ? Kill
      future
        .andThen { case _ => system terminate() }
        .onComplete(_ => System exit 0)
    }
  }

  /* Setting of the app logo */
  this getIcons() add new Image(appLogoPath)

  /* Simple getter to retrieve the presenter */
  def presenter: ChatPresenter = _presenter

  /* User Actor creation */
  val system = ActorSystem("User", User.Config)
  val user: ActorRef = system.actorOf(User(presenter), username)
  presenter.user_(user)
}

/* ChatView Companion object */
object ChatView {
  //file paths
  val appLogoPath = "/ex2/logo.png"
  private val sendLogoPath = "/ex2/send_button_logo.png"
}
