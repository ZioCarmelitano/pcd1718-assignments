import akka.actor.ActorSystem
import pcd.ass03.ex2.actors.Room

import scala.concurrent.duration._
import scala.io.StdIn

object MainRoom extends App {
  val system = ActorSystem("Room", Room.Config)
  try {
    val room = system.actorOf(Room(30 seconds), "Room")
    println("Press ENTER to terminate")
    StdIn.readLine
  } finally {
    system.terminate()
  }
}
