package pcd.ass03.dsn.actors

import akka.actor.{Actor, ActorLogging, ActorSelection, Props}

import scala.util.Random
import scala.concurrent.duration._

class Sensor extends Actor with ActorLogging {

  import Sensor.{Quantity, SendQuantity, Fail}
  import DirectoryFacilitator.{PingRequest, PingResponse, Register}

  private[this] val samplingFrequency = Random.nextInt(10) seconds
  private[this] val stopDelay = Random.nextInt(10) minutes
  private[this] var facilitator: ActorSelection = _

  override def preStart(): Unit = {
    facilitator = context.system.actorSelection(DirectoryFacilitator.Path)
    facilitator ! Register(self)
    context.system.scheduler.schedule(samplingFrequency, samplingFrequency, self, SendQuantity)
  }

  override def receive: Receive = {
    case PingRequest => sender ! PingResponse
    case SendQuantity => facilitator ! nextQuantity
    case Fail => context.system.stop(self)
  }

  private def nextQuantity: Quantity = Quantity(Random.nextInt(100))
}

object Sensor {

  def props(): Props = Props(new Sensor)

  final case class Quantity(value: Int)

  private final case object SendQuantity

  final case object Fail

}
