package pcd.ass03.dsn.actors

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import pcd.ass03.dsn.actors.DirectoryFacilitator._

import scala.concurrent.duration._

class DirectoryFacilitator extends Actor with ActorLogging {

  import DirectoryFacilitator.Implicits.Timeout

  private[this] val actors = scala.collection.mutable.ListBuffer[ActorRef]()

  override def receive: Receive = {
    case Register(actor) => actors += actor
      context watch actor
      log.info(s"Actor ${actor.path.name} has been added to the registered actors")
    case Terminated(actor) => actors -= actor
      context unwatch actor
      log.info(s"Actor ${actor.path.name} has been removed to the registered actors")
    case x: Any => actors.foreach {
      _ ? x
    }
  }

}

object DirectoryFacilitator {

  def props(): Props = Props(new DirectoryFacilitator)

  final case class Register(actor: ActorRef)

  val Config: Config = ConfigFactory.parseFile(new File("src/main/resources/akka/DirectoryFacilitator.conf"))
  val Path = "akka.tcp://DirectoryFacilitator@127.0.0.1:2552/user/DirectoryFacilitator"

  object Implicits {
    implicit val Timeout: Timeout = 1 second
  }

}
