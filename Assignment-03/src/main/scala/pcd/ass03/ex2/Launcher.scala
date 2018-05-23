package pcd.ass03.ex2

import akka.actor.{ActorRef, ActorSystem}
import pcd.ass03.ex2.actors.VisibleUser
import pcd.ass03.ex2.view.DistributedChatView
import scalafx.application.JFXApp

import scala.io.StdIn

object Launcher extends JFXApp{

  // Name insertion
  println("Insert your name: ")
  val username: String = StdIn.readLine

  // GUI Init
  val view = new DistributedChatView
  stage = view

  // User Actor creation
  val system = ActorSystem("User", VisibleUser.Config)
  val user: ActorRef = system.actorOf(VisibleUser(view.presenter), username)
}

