package net.martinprobson.diningphilosopher.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import net.martinprobson.diningphilosopher.actor.Philosopher.{SetTimeout, State, Timeout}

import scala.concurrent.duration.DurationInt

class Timer private(name: String, context: ActorContext[State], nextState: State) {
    private def timer(): Behavior[State] =
        Behaviors.withTimers[State] { timers =>
            Behaviors.receiveMessage {
                case SetTimeout(sendTo) =>
                    context.log.debug(s"SetTimeOut - $name")
                    timers.startSingleTimer(Timeout(sendTo), 100.millis)
                    Behaviors.same
                case Timeout(sendTo) =>
                    context.log.debug(s"$name - sending Timeout to $sendTo")
                    sendTo ! nextState
                    Behaviors.same
                case _ => Behaviors.unhandled
            }
        }
}

object Timer {
    def apply(name: String, message: State): Behavior[State] =
        Behaviors.setup { context =>
            new Timer(name, context, message).timer()
        }
}

