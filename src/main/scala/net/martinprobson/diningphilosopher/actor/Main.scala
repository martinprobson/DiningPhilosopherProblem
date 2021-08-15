package net.martinprobson.diningphilosopher.actor

import akka.actor.typed.ActorSystem
import scala.concurrent.duration.DurationInt

object Main extends App {

    import Controller._

    val controller = ActorSystem(Controller(), "Controller")
    controller ! Start
    Thread.sleep(1.minutes.toMillis)
    controller ! Stop
}

