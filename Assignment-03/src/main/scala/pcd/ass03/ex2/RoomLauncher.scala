package pcd.ass03.ex2

import akka.actor.ActorSystem
import pcd.ass03.ex2.actors.Room

import scala.concurrent.duration._
import scala.io.StdIn

/* It launches the room application that let users to connect to the chat*/
object RoomLauncher extends App {
  val system = ActorSystem("Room", Room.Config)
  try {
    system.actorOf(Room(30 seconds), "Room")
    println("Press ENTER to terminate")
    StdIn.readLine
  } finally {
    system.terminate()
  }
}
