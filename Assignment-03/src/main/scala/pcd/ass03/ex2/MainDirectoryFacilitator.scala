package pcd.ass03.ex2

import akka.actor.ActorSystem
import pcd.ass03.ex2.actors.DirectoryFacilitator

import scala.io.StdIn

object MainDirectoryFacilitator extends App {
  val system = ActorSystem("DirectoryFacilitator", DirectoryFacilitator.Config)
  try {
    system.actorOf(DirectoryFacilitator.props(), "DirectoryFacilitator")

    println("Press ENTER to terminate")
    StdIn.readLine
  } finally {
    system.terminate
  }
}
