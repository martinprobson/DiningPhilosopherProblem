package net.martinprobson.diningphilosopher.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import net.martinprobson.diningphilosopher.actor.Philosopher.Eat

object Controller {
    sealed trait ControllerEvent

    object Start extends ControllerEvent

    object Stop extends ControllerEvent

    def apply(): Behavior[ControllerEvent] =
        Behaviors.setup[ControllerEvent] { context =>
            val forks = (1 to 5).toArray.map( i => context.spawn(Fork(s"Fork-$i"),s"Fork-$i"))
            val philosophers = List("Descartes","Nietzsche","Kant","Hume","Plato").zipWithIndex.map{ case (name,i) =>
                context.spawn(Philosopher(name,forks(i),forks((i+1) % 4)),name)
            }
            Behaviors.receive[ControllerEvent] { (context, message) =>
                message match {
                    case Start =>
                        philosophers.foreach(_ ! Eat)
                        Behaviors.same
                    case Stop => Behaviors.stopped
                }
            }
        }
}
