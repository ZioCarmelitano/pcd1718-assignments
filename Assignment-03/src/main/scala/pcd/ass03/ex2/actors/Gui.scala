package pcd.ass03.ex2.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, PoisonPill, Props}
import com.typesafe.config.{Config, ConfigFactory}
import DirectoryFacilitator.{Actors, ActorsRequest}
import Gui._
import Sensor.{Quantity, SnapshotRequest, Token}

import scala.util.Random

class Gui extends Actor with ActorLogging {

  private[this] var facilitator: ActorSelection = _
  private[this] var tokens = Map[ActorRef, Option[Quantity]]()

  def receivingTokens(numberOfActors: Int): Receive = {
    case Token(actor, quantity) => tokens = tokens + (actor -> Some(quantity))
      if (tokens forall { case (_, q) => q.isDefined }) {
        //
      }
  }

  private[this] val sendingRequests: Receive = {
    case Actors(actors) =>
      actors foreach {
        _ ! SnapshotRequest
      }
      tokens = actors
        .map {
          x => x -> None
        }
        .toMap
      context become receivingTokens(actors.size)
  }

  private[this] val failing: Receive = {
    case Actors(actors) =>
      actors(Random nextInt actors.size) ! PoisonPill
      context become default
  }

  private[this] val default: Receive = {
    case Fail =>
      facilitator ! ActorsRequest
      context become failing
    case
      TakeSnapshot => facilitator ! ActorsRequest
      context become sendingRequests
  }

  override def preStart(): Unit = facilitator = context.actorSelection(DirectoryFacilitator.Path)

  override def receive: Receive = default

}

object Gui {

  def apply(): Props = props()

  def props(): Props = Props(new Gui)

  final case object TakeSnapshot

  final case object Fail

  val Config: Config = ConfigFactory.parseFile(new File("src/main/resources/akka/Gui.conf"))

}
