package pcd.ass03.ex2.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.typesafe.config.{Config, ConfigFactory}
import pcd.ass03.ex2.actors.Room._
import pcd.ass03.ex2.actors.User.{LockCheck, Matrix, Send}
import pcd.ass03.ex2.view.ChatPresenter

class User(presenter: ChatPresenter) extends Actor with ActorLogging {

  private[this] lazy val room = context.actorSelection(Room.Path)
  private[this] var lock = false

  implicit private[this] var matrix = Matrix()
  private[this] var pendingQueue = List[Message]()

  override def receive: Receive = {
    case Users(actors) =>
      // Create the new matrix initialized with zeros
      matrix = (actors :+ self).toStream
        .map {
          x => x -> (actors :+ self).map {
            _ -> 0
          }.toMap
        }
        .toMap
      log.info(s"$matrix")

    case Joined(user) =>
      log.info(s"User ${user.path.name} has joined the room")
      presenter.receiveInfo(s"User ${user.path.name} has joined the room")
      val users = matrix.keySet + user
      // Add a row for the newly joined user
      matrix = matrix + (user -> users.map {
        _ -> 0
      }.toMap)
      // Add a column for the newly joined user
      matrix = matrix map {
        x => (x._1, x._2 + (user -> 0))
      }
      log.info(s"$matrix")

    case Left(user) =>
      log.info(s"User ${user.path.name} has left the room")
      presenter.receiveInfo(s"User ${user.path.name} has left the room")
      // Delete the row corresponding to the user that has left
      matrix = matrix filterNot {
        _._1 == user
      }
      // Delete the columns corresponding to the user that has left
      matrix = matrix.map {
        x =>
          (x._1, x._2.filterNot {
            _._1 == user
          })
      }
      log.info(s"$matrix")

    case Commands(commands) => log.info(s"Commands are: $commands")

    case Message(content, user, userMatrix) =>
      var deliverableMessage = pendingQueue.find(m => isDeliverable(m.user, m.userMatrix))
      while (deliverableMessage.isDefined) {
        val message = deliverableMessage.get
        showMessage(message.content, message.user)
        pendingQueue = pendingQueue filterNot(_ == message)
        deliverableMessage = pendingQueue.find(m => isDeliverable(m.user, m.userMatrix))
      }
      if (isDeliverable(user, userMatrix)) {
        showMessage(content, user)
      } else {
        pendingQueue = pendingQueue :+ Message(content, user, userMatrix)
      }
      matrix = max(userMatrix)

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

    case LockCheck => sender ! lock

    case NoCriticalSection => log.error("The room is not in a critical section state")
    case Send(content) =>
      // Update the the row corresponding the user that sends the message
      matrix = matrix + (self -> matrix(self).map {
        x => (x._1, x._2 + 1)
      })
      room ! Room.createMessage(content)
  }

  private def showMessage(content: String, user: ActorRef) = {
    log.info(s"${user.path.name} said: $content")
    presenter.receive(content, user.path)
  }

  // W[j, i] == M[j, i] and \forall k != j, M[k, i] >= W[k, i]
  private def isDeliverable(user: ActorRef, userMatrix: Matrix) = {
    userMatrix(user)(self) == matrix(user)(self) + 1 && matrix.toStream.filterNot(_._1 == user)
      .forall {
        case (k, _) => matrix(k)(self) >= userMatrix(k)(self)
      }
  }

  private[this] def max(userMatrix: Matrix): Map[ActorRef, Map[ActorRef, Int]] =
    matrix.toStream
      .map {
        case (k, v) => k -> (v.toSeq ++ userMatrix(k).toSeq).toStream
          .groupBy(_._1)
          .mapValues(_.map(_._2).max)
          .map(identity)
      }
      .toMap

  override def preStart(): Unit = room ! Join

  override def postStop(): Unit = room ! Leave

}

object User {

  type Matrix = Map[ActorRef, Map[ActorRef, Int]]

  def Matrix(): Matrix = Map[ActorRef, Map[ActorRef, Int]]()

  def apply(chatPresenter: ChatPresenter): Props = props(chatPresenter)

  def props(chatPresenter: ChatPresenter): Props = Props(new User(chatPresenter))

  final case class Send(content: String)

  final case object LockCheck

  val Config: Config = ConfigFactory.parseFile(new File("src/main/resources/ex2/akka/user.conf"))

}
