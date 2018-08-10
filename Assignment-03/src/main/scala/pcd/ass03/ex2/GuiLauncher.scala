package pcd.ass03.ex2

import pcd.ass03.ex2.view.login.LoginView
import scalafx.application.JFXApp

/* It launches the ScalaFX Application creating the LoginView as first stage */
object GuiLauncher extends JFXApp {
  stage = new LoginView
}
