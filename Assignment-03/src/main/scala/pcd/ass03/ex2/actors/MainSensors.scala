package pcd.ass03.ex2.actors

import akka.actor.{ActorSystem, PoisonPill}

import scala.io.StdIn

object MainSensors extends App {
  val system = ActorSystem("Sensors", Sensor.Config)
  try {
    val sensor = system.actorOf(Sensor.props())

    println("Press ENTER to terminate")
    StdIn.readLine
    sensor ! PoisonPill
  } finally {
    system.terminate
  }
}
