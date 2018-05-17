package pcd.ass03.dsn.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSelection, Cancellable, Props}
import com.typesafe.config.{Config, ConfigFactory}
import pcd.ass03.dsn.actors.DirectoryFacilitator.Register
import pcd.ass03.dsn.actors.Sensor.{Quantity, SendQuantity}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Zero
import scala.concurrent.duration._
import scala.util.Random

class Sensor extends Actor with ActorLogging {

  private[this] var facilitator: ActorSelection = _
  private[this] var samplingCancellable: Cancellable = _

  override def preStart(): Unit = {
    facilitator = context.system.actorSelection(DirectoryFacilitator.Path)
    facilitator ! Register(self)
    val samplingFrequency = (1 + Random.nextInt(10)).seconds
    samplingCancellable = context.system.scheduler.schedule(
      Zero,
      samplingFrequency,
      self,
      SendQuantity)
    log.info(s"Started sampling at frequency of $samplingFrequency")
  }

  override def postStop(): Unit = samplingCancellable.cancel()

  override def receive: Receive = {
    case SendQuantity => facilitator ! nextQuantity
      log.info(s"Sent quantity!")
  }

  private def nextQuantity: Quantity = Quantity(Random.nextInt(100))

}

object Sensor {

  def props(): Props = Props(new Sensor)

  final case class Quantity(value: Int)

  private final case object SendQuantity

  val Config: Config = ConfigFactory.parseFile(new File("src/main/resources/akka/Sensor.conf"))

}
