package pcd.ass03.ex2.actors

import akka.actor.{Actor, ActorLogging, ActorSelection, Props}
import GUI._

class GUI extends Actor with ActorLogging {

  var facilitator: ActorSelection = _

  override def preStart(): Unit = {
    facilitator = context.actorSelection(DirectoryFacilitator.Path)
  }

  override def receive: Receive = {
    case TakeSnapshot => ???
  }

}

object GUI {

  def props(): Props = Props(new GUI)

  private final case object TakeSnapshot

}
