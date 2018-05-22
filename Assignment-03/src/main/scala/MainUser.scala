import akka.actor.ActorSystem
import pcd.ass03.ex2.actors.User
import pcd.ass03.ex2.actors.User.Send

import scala.io.StdIn

object MainUser extends App {
  val system = ActorSystem("User", User.Config)
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
