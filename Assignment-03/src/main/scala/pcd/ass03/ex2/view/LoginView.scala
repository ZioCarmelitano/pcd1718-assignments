package pcd.ass03.ex2.view

import pcd.ass03.ex2.view.ImagePaths.appLogoPath
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.image.Image
import scalafx.scene.layout.BorderPane

class LoginView extends PrimaryStage{

  /* GUI components creation */
  private val usernameLabel = new Label("Username")
  private val userField: TextField = new TextField{
    prefWidth = 200
    margin = Insets(10)
  }
  private val loginButton: Button = new Button("Login")

  /* Login presenter creation */
  private val loginPresenter:LoginPresenter = new LoginPresenter(userField)

  title = "Chat Login"

  /* Login scene creation */
  private val LOGIN_WIDTH = 280
  private val LOGIN_HEIGHT = 100

  scene = new Scene(LOGIN_WIDTH,LOGIN_HEIGHT){
    private val pane: BorderPane = new BorderPane {
      top = new BorderPane{
        center = usernameLabel
      }
      center = userField
      bottom = new BorderPane{
        center = loginButton
      }
    }
    pane.layoutX.value = 30
    content = pane
  }

  /* GUI components listeners settings */
  userField.onAction = (_) => loginPresenter.login()
  loginButton.onAction = (_) => loginPresenter.login()

  /* Logo image setting in Login stage */
  this getIcons() add new Image(appLogoPath)
}
