package pcd.ass03.ex2.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props, Terminated}
import com.typesafe.config.{Config, ConfigFactory}
import pcd.ass03.ex2.actors.Room._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class Room(private[this] val timeout: FiniteDuration) extends Actor with ActorLogging {

  private[this] implicit lazy val dispatcher: ExecutionContext = context.system.dispatcher

  private[this] var users = List[ActorRef]()

  private[this] var counter = 0

  override def receive: Receive = noCriticalSection

  private[this] val default: Receive = {
    case Join =>
      broadcast(Joined(sender))
      sender ! commands
      sender ! RoomCounter(counter)
      users = users :+ sender
      context watch sender
      log.info(s"User ${sender.path.name} has joined the chat")
    case Leave => remove(sender)
    case Terminated(actor) => remove(actor)
    case Help => sender ! commands
    case cnu: CommandNotUnderstood => sender ! cnu
  }

  private[this] val noCriticalSection: Receive = default orElse {
    case EnterCriticalSection =>
      val cancellable = context.system.scheduler.scheduleOnce(timeout, self, ExitCriticalSection)
      context become criticalSection(CriticalSection(sender), cancellable)
      broadcast(EnterCriticalSection)
      log.info(s"User ${sender.path.name} has started the critical section")
    case ExitCriticalSection => sender ! NoCriticalSection
    case message: Message =>
      counter += 1
      val msg = (message, counter)
      broadcast(msg)
  }

  private[this] def criticalSection(cs: CriticalSection, cancellable: Cancellable): Receive = default orElse {
    case ExitCriticalSection if sender == cs.user || sender == self =>
      cancellable.cancel()
      context become noCriticalSection
      broadcast(ExitCriticalSection)
      log.info("Exited from critical section")
    case message: Message if sender == cs.user =>
      counter += 1
      val msg = (message, counter)
      broadcast(msg)
    case Message(_, _, _) | EnterCriticalSection | ExitCriticalSection => sender ! cs
  }

  private[this] def remove(user: ActorRef): Unit = {
    users = users filterNot {
      _ == user
    }
    context unwatch user
    broadcast(Left(user))
    log.info(s"User ${user.path.name} has left the chat")
  }

  private[this] def broadcast(message: Any): Unit = {
    users
      .foreach {
        _ ! message
      }
    log.debug(s"$message sent to users")
  }

}

object Room {

  def apply(timeout: FiniteDuration): Props = props(timeout)

  def props(timeout: FiniteDuration): Props = Props(new Room(timeout))

  val Config: Config = ConfigFactory.parseFile(new File("src/main/resources/ex2/akka/room.conf"))

  val Path = "akka.tcp://Room@127.0.0.1:2552/user/Room"

  final case object Join

  final case object Leave

  final case class RoomCounter(counter: Int)

  final case class Joined(user: ActorRef)

  final case class Left(user: ActorRef)

  final case class Message(content: String, user: ActorRef, userClock: Int)

  final case object EnterCriticalSection

  final case object ExitCriticalSection

  final case object Help

  final case class CommandNotUnderstood(command: String)

  final case class Commands(commands: Set[String])

  private val commandMap = Map(":enter-cs" -> EnterCriticalSection, ":exit-cs" -> ExitCriticalSection, ":help" -> Help)
  private val commands = Commands(commandMap.keySet)

  final case class CriticalSection(user: ActorRef)

  final case object NoCriticalSection

  def createMessage(content: String)(implicit user: ActorRef, userClock: Int): Any = content match {
    case command if commandMap contains command => commandMap(command)
    case command if command.startsWith(":") => CommandNotUnderstood(command)
    case c => Message(c, user, userClock)
  }

}
