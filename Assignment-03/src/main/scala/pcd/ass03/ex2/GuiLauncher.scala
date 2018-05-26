package pcd.ass03.ex2

import akka.actor.{ActorRef, ActorSystem}
import pcd.ass03.ex2.actors.User
import pcd.ass03.ex2.view.ChatView
import scalafx.application.JFXApp

import scala.io.StdIn

object GuiLauncher extends JFXApp {

  // Name insertion
  println("Insert your name: ")
  val username: String = StdIn.readLine

  // GUI Init
  val view = new ChatView
  stage = view

  // User Actor creation
  val system = ActorSystem("User", User.Config)
  val user: ActorRef = system.actorOf(User(view.presenter), username)

}
