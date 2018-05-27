package pcd.ass03.ex2.view

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

object DialogUtils {
  def errorDialog(dialogTitle: String, header: String, content: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert setTitle dialogTitle
    alert setHeaderText header
    alert setContentText content
    alert showAndWait
  }
}
