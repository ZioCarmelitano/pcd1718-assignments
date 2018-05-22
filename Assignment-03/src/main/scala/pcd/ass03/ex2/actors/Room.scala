package pcd.ass03.ex2.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props, Terminated}
import com.typesafe.config.{Config, ConfigFactory}
import pcd.ass03.ex2.actors.Room._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class Room(private[this] val timeout: FiniteDuration) extends Actor with ActorLogging {

  private[this] var users = List[ActorRef]()

  override def receive: Receive = noCriticalSection

  private[this] val default: Receive = {
    case Join =>
      broadcast(Joined(sender))
      sender ! Commands
      users = users :+ sender
      context watch sender
      log.info(s"User ${sender.path.name} has joined the chat")
    case Leave => remove(sender)
    case Terminated(actor) => remove(actor)
    case cnu: CommandNotUnderstood => sender ! cnu
  }

  private[this] val noCriticalSection: Receive = default orElse {
    case EnterCS =>
      val cancellable = context.system.scheduler.scheduleOnce(timeout, self, ExitCS)
      context become criticalSection(CriticalSection(sender), cancellable)
      broadcast(EnterCS)
      log.info(s"User ${sender.path.name} has started the critical section")
    case ExitCS => sender ! NoCriticalSection
    case message: Message => broadcast(message)
  }

  private[this] def criticalSection(cs: CriticalSection, cancellable: Cancellable): Receive = default orElse {
    case ExitCS if sender == cs.user || sender == self =>
      cancellable.cancel()
      context become noCriticalSection
      broadcast(ExitCS)
      log.info("Exited from critical section")
    case message: Message if sender == cs.user => broadcast(message)
    case Message(_, _) | EnterCS | ExitCS => sender ! cs
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
    users foreach {
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
  final case class Joined(user: ActorRef)
  final case class Left(user: ActorRef)

  final case class Message(content: String, user: ActorRef)
  final case object EnterCS
  final case object ExitCS
  final case class CommandNotUnderstood(command: String)

  private val commandMap = Map(":enter-cs" -> EnterCS, ":exit-cs" -> ExitCS)
  private val commands = Commands(commandMap.keySet)

  final case class Commands(commands: Set[String])

  final case class CriticalSection(user: ActorRef)
  final case object NoCriticalSection

  def createMessage(content: String)(implicit user: ActorRef): Any = content match {
    case command if commandMap contains command => commandMap(command)
    case command if command.startsWith(":") => CommandNotUnderstood(command)
    case c => Message(c, user)
  }

}
