package pcd.ass03.dsn.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.duration._

class DirectoryFacilitator extends Actor with ActorLogging {

  import DirectoryFacilitator.Implicits._
  import DirectoryFacilitator._

  private[this] val actors = scala.collection.mutable.ListBuffer[ActorRef]()

  override def preStart(): Unit = {
    context.system.scheduler.schedule(Frequency, Frequency, self, RequestPing)
  }

  override def receive: Receive = {
    case Register(actor: ActorRef) => actors += actor
    case RequestPing => actors.foreach { actor =>
      (actor ? PingRequest)
        .mapTo[PingResponse]
        .failed
        .onComplete(x => actors -= actor)
    }
    case x: Any => actors.foreach {
      _ ? x
    }
  }

}

object DirectoryFacilitator {
  def props(): Props = Props(new DirectoryFacilitator)

  final case class Register(actor: ActorRef)

  final case object PingRequest

  final case class PingResponse()

  private final case object RequestPing

  val Path = "akka.tcp://DirectoryFacilitator@127.0.0.1:2552/user/DirectoryFacilitator"

  private val Frequency = 1 second

  object Implicits {
    implicit val Timeout: Timeout = Timeout(1 second)
  }

}
