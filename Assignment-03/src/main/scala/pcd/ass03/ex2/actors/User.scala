package pcd.ass03.ex2.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import pcd.ass03.ex2.actors.Room._
import pcd.ass03.ex2.actors.User.{apply => _, _}
import pcd.ass03.ex2.view.chat.ChatPresenter

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Success

class User(private[this] val presenter: ChatPresenter) extends Actor with ActorLogging {

  private[this] implicit val dispatcher: ExecutionContext = context.system.dispatcher

  private[this] lazy val room = context.actorSelection(Room.Path)
  private[this] var lock = false

  private[this] var clock: VectorClock = VectorClock()
  private[this] var roomCounter = 0
  private[this] var userCounter = 0

  private[this] var holdBackQueue = mutable.PriorityQueue[(Message, Int)]()
  private[this] implicit val holdBackOrdering: Ordering[(Message, Int)] = Ordering.by(_._2)

  private[this] var pendingQueue = List[Message]()

  def causalMessageOrdering(message: Message): Unit = {
    if (message.userClock == clock(message.user) + 1) {
      log.info(s"Received causal order $message")
      showMessage(message.content, message.user)
      clock = clock + (message.user -> (clock(message.user) + 1))
      var deliverableMessage = pendingQueue.find(x => x.userClock == clock(x.user) + 1)
      while (deliverableMessage.isDefined) {
        val msg = deliverableMessage.get
        showMessage(msg.content, msg.user)
        clock = clock + (msg.user -> (clock(msg.user) + 1))
        pendingQueue = pendingQueue.filterNot(_ == msg)
        deliverableMessage = pendingQueue.find(x => x.userClock == clock(x.user) + 1)
      }
    } else {
      log.info(s"In else causal order: $message")
      pendingQueue = pendingQueue :+ message
    }
  }

  override def receive: Receive = {

    case Joined(user) =>
      log.info(s"User ${user.path.name} has joined the room")
      presenter.receiveInfo(s"User ${user.path.name} has joined the room")
      clock = clock + (user -> 0)
      user ! UserCounter(userCounter)

    case Left(user) =>
      log.info(s"User ${user.path.name} has left the room")
      presenter.receiveInfo(s"User ${user.path.name} has left the room")
      clock = clock.filterKeys(_ != user)

    case Commands(availableCommands) =>
      log.info(s"Commands are: $availableCommands")
      presenter.receiveInfo("Available commands are:\n" + availableCommands.mkString("\n"))

    case RoomCounter(c) => roomCounter = c

    case UserCounter(c) => clock = clock + (sender -> c)

    case (message: Message, c: Int) =>
      if (c == roomCounter + 1) {
        log.info(s"Received total order $message")
        roomCounter += 1
        causalMessageOrdering(message)
        var deliverableMessage = holdBackQueue.find(_._2 == roomCounter + 1)
        while (deliverableMessage.isDefined) {
          val (message, _) = deliverableMessage.get
          roomCounter += 1
          causalMessageOrdering(message)
          holdBackQueue = holdBackQueue.filterNot(_._1 == message)
          deliverableMessage = holdBackQueue.find(_._2 == roomCounter + 1)
        }
      } else {
        log.info(s"In else total order: $message")
        holdBackQueue += ((message, c))
      }

    case CommandNotUnderstood(command) =>
      log.error(s"$command is not a valid command")
      presenter.receiveInfo(s"$command is not a valid command")

    case EnterCriticalSection =>
      log.info("Entered in critical section")
      presenter.receiveInfo("Entered in critical section")
      lock = true

    case ExitCriticalSection =>
      log.info("Exited from critical section")
      presenter.receiveInfo("Exited from critical section")
      lock = false

    case CriticalSection(user) =>
      log.warning(s"Could not send message, critical section is held by ${user.path.name}")
      presenter.receiveInfo(s"Could not send message, critical section is held by ${user.path.name}")

    case LockCheck =>
      val presenter = sender
      implicit val timeout: Timeout = Timeout(2 seconds)
      val future = room ? LockCheck
      future.onComplete {
        case Success(NoCriticalSection) => presenter ! NoCriticalSection
        case Success(c: CriticalSection) => presenter ! c
      }

    case NoCriticalSection =>
      log.error("The room is not in a critical section state")
      presenter.receiveInfo("The room is not in a critical section state")

    case Send(content) =>
      if (!(Room.commands.commands contains content)) userCounter += 1
      implicit val counter: Int = userCounter
      //context.system.scheduler.scheduleOnce((1 + Random.nextInt(10)) seconds) {
      log.info(s"Sending $content")
      room ! Room.createMessage(content)
    //}

    case Kill => context.stop(self)
  }

  private[this] def showMessage(content: String, user: ActorRef): Unit = {
    log.info(s"${user.path.name} said: $content")
    presenter.receive(content, user.path)
  }

  override def preStart(): Unit = {
    room ! Join
    clock = clock + (self -> 0)
  }

  override def postStop(): Unit = room ! Leave

}

object User {

  type VectorClock = Map[ActorRef, Int]

  def VectorClock(): VectorClock = Map[ActorRef, Int]()

  def apply(chatPresenter: ChatPresenter): Props = props(chatPresenter)

  def props(chatPresenter: ChatPresenter): Props = Props(new User(chatPresenter))

  final case class UserCounter(counter: Int)

  final case class Send(content: String)

  final case object LockCheck

  final case object Kill

  val Config: Config = ConfigFactory.parseFile(new File("src/main/resources/ex2/akka/user.conf"))

}
