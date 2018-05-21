package pcd.ass03.ex2.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Cancellable, Props}
import com.typesafe.config.{Config, ConfigFactory}
import pcd.ass03.ex2.actors.DirectoryFacilitator.Register
import pcd.ass03.ex2.actors.Sensor.Sample

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

    val samplingFrequency = Sensor.samplingFrequency
    samplingCancellable = context.system.scheduler.schedule(
      Zero,
      samplingFrequency,
      self,
      Sample)
    log.info(s"Started sampling at frequency of $samplingFrequency")
  }

  override def postStop(): Unit = samplingCancellable.cancel()

  override def receive: Receive = {
    case Sample => ???
  }

}

object Sensor {

  def apply(): Props = props()

  def props(): Props = Props(new Sensor)

  private def samplingFrequency: FiniteDuration = (1 + Random.nextInt(10)) seconds

  private def nextQuantity: Quantity = Quantity(Random.nextInt(100))

  private final case object Sample

  final case class Quantity(value: Int)

  final case object SnapshotRequest

  final case class Token(actor: ActorRef, quantity: Quantity)

  val Config: Config = ConfigFactory.parseFile(new File("src/main/resources/akka/Sensor.conf"))

}
