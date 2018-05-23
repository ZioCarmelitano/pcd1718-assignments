import akka.actor.ActorSystem
import pcd.ass03.ex2.actors.{User, VisibleUser}
import pcd.ass03.ex2.actors.VisibleUser.Send

import scala.io.StdIn

object MainUser extends App {
  val system = ActorSystem("User", VisibleUser.Config)
  try {
    val user = system.actorOf(User())

    new Thread {
      while (true) {
        val content = StdIn.readLine
        user ! Send(content)
      }
    } start
  } finally {
    system.terminate()
  }
}
