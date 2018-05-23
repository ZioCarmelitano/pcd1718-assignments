package pcd.ass03.ex2.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.typesafe.config.{Config, ConfigFactory}
import pcd.ass03.ex2.actors.Room._
import pcd.ass03.ex2.actors.User.Send
import pcd.ass03.ex2.view.ChatPresenter

class User(presenter: ChatPresenter) extends Actor with ActorLogging {

  private[this] lazy val room = context.actorSelection(Room.Path)

  override def receive: Receive = {
    case Joined(user: ActorRef) => log.info(s"User ${user.path.name} has joined the room")
    case Left(user: ActorRef) => log.info(s"User ${user.path.name} has left the room")
    case Commands(commands) => log.info(s"Commands are: $commands")
    case Message(content, user) =>
      log.info(s"${user.path.name} said: $content")
      presenter.receive(content, user.path.name)
    case CommandNotUnderstood(command) => log.error(s"$command is not a valid command")
    case EnterCriticalSection => log.info("Entered in critical section")
    case ExitCriticalSection => log.info("Exited from critical section")
    case CriticalSection(user) => log.warning(s"Could not send message, critical section is held by ${user.path.name}")
    case NoCriticalSection => log.error("The room is not in a critical section state")
    case Send(content) => room ! Room.createMessage(content)
  }

  override def preStart(): Unit = room ! Join

  override def postStop(): Unit = room ! Leave
}

object User {

  def apply(chatPresenter: ChatPresenter): Props = props(chatPresenter)

  def props(chatPresenter: ChatPresenter): Props = Props(new User(chatPresenter))

  final case class Send(content: String)

  val Config: Config = ConfigFactory.parseFile(new File("src/main/resources/ex2/akka/user.conf"))

}
