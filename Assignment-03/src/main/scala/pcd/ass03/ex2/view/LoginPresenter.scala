package pcd.ass03.ex2.view

import scalafx.scene.control.TextField

class LoginPresenter(usernameField: TextField) {

  def login() ={
    new ChatView(usernameField.text.value) show
  }
}
