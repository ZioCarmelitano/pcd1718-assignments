package pcd.ass03.ex2.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSelection, Cancellable, Props}
import com.typesafe.config.{Config, ConfigFactory}
import DirectoryFacilitator.{Actors, Register}
import Sensor.{Quantity, SendQuantity}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Zero
import scala.concurrent.duration._
import scala.util.Random

class Sensor extends Actor with ActorLogging {

  private[this] var facilitator: ActorSelection = _
  private[this] var samplingCancellable: Cancellable = _

  override def preStart(): Unit = {
    facilitator = context.actorSelection(DirectoryFacilitator.Path)
    facilitator ! Register(self)

    val samplingFrequency = Sensor samplingFrequency()
    samplingCancellable = context.system.scheduler.schedule(
      Zero,
      samplingFrequency,
      self,
      SendQuantity)
    log.info(s"Started sampling at frequency of $samplingFrequency")
  }

  override def postStop(): Unit = samplingCancellable.cancel()

  override def receive: Receive = {
    case SendQuantity => facilitator ! Sensor.nextQuantity
      log.info("Sent quantity!")
  }

}

object Sensor {

  def props(): Props = Props(new Sensor)

  private def samplingFrequency(): FiniteDuration = (1 + Random.nextInt(10)) seconds

  private def nextQuantity(): Quantity = Quantity(Random.nextInt(100))

  private final case object SendQuantity
  final case class Quantity(value: Int)

  val Config: Config = ConfigFactory.parseFile(new File("src/main/resources/akka/Sensor.conf"))

}
