package pcd.ass03.ex2.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props, Terminated}
import com.typesafe.config.{Config, ConfigFactory}
import pcd.ass03.ex2.actors.Room._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class Room(private[this] val timeout: FiniteDuration) extends Actor with ActorLogging {

  private[this] var users = List[ActorRef]()

  private[this] val default: Receive = {
    case Join => add(sender)
    case Leave => remove(sender)
    case Terminated(actor) => remove(actor)
    case cnu: CommandNotUnderstood => sender ! cnu
  }

  private[this] val noCriticalSection: Receive = default orElse {
    case EnterCS => enterCS(sender)
    case ExitCS => sender ! NoCriticalSection
    case message: Message => broadcast(message)
  }

  private[this] def criticalSection(cs: CriticalSection, cancellable: Cancellable): Receive = default orElse {
    case ExitCS if sender == cs.user =>
      cancellable.cancel()
      exitCS()
    case message: Message if sender == cs.user => broadcast(message)
    case TimeoutExpired =>
      log.info(s"Timeout of $timeout expired")
      exitCS()
    case Message(_, _) | EnterCS | ExitCS => sender ! cs
  }

  private[this] def enterCS(user: ActorRef): Unit = {
    val cancellable = context.system.scheduler.scheduleOnce(timeout, self, TimeoutExpired)
    context become criticalSection(CriticalSection(user), cancellable)
    log.info(s"User ${user.path.name} entered in critical section")
    broadcast(EnterCS)
  }

  private[this] def exitCS(): Unit = {
    context become noCriticalSection
    log.info("Exited from critical section")
    broadcast(ExitCS)
  }

  override def receive: Receive = noCriticalSection

  private[this] def add(user: ActorRef): Unit = {
    broadcast(Joined(sender))
    users = users :+ sender
    context watch sender
    log.info(s"User ${user.path.name} has joined the chat")
  }

  private[this] def remove(user: ActorRef): Unit = {
    users = users filterNot {
      _ == user
    }
    context unwatch user
    broadcast(Left(user))
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

  final case class Message private(content: String, user: ActorRef)

  final case object EnterCS
  final case object ExitCS
  final case class CommandNotUnderstood(command: String)

  final case class CriticalSection(user: ActorRef)

  final case object NoCriticalSection

  private final case object TimeoutExpired

  def createMessage(content: String)(implicit user: ActorRef): Any = content match {
    case ":enter-cs" => EnterCS
    case ":exit-cs" => ExitCS
    case command: String if command.startsWith(":") => CommandNotUnderstood(command)
    case c => Message(c, user)
  }

}
