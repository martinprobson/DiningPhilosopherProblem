package net.martinprobson.diningphilosopher.actor

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import net.martinprobson.diningphilosopher.actor.Fork.{ForkRequest, Put, Take}
import net.martinprobson.diningphilosopher.actor.Philosopher.{Busy, State, Taken}

class Fork private (name: String, context: ActorContext[ForkRequest]) {

    private def busy(): Behavior[ForkRequest] = Behaviors.receiveMessage[ForkRequest] {
        case Take(requester, position) =>
            context.log.debug(s"$name - Take - Busy - $position")
            requester ! Busy(context.self)
            Behaviors.same
        case Put =>
            context.log.debug(s"$name - Put Busy")
            idle()
        case _ => Behaviors.unhandled
    }
    private def idle(): Behavior[ForkRequest] = Behaviors.receiveMessage[ForkRequest] {
        case Take(requester, position) =>
            context.log.debug(s"$name - Take Idle - $position")
            requester ! Taken(context.self, position)
            busy()
        case Put =>
            context.log.info(s"$name - Put Idle ")
            Behaviors.same
        case _ => Behaviors.unhandled
    }
}

object Fork {
    sealed trait ForkPosition
    object Left extends ForkPosition
    object Right extends ForkPosition

    sealed trait ForkRequest
    case class Take(requester: ActorRef[State], position: ForkPosition) extends ForkRequest
    object Put extends ForkRequest

    def apply(name: String): Behavior[ForkRequest] = {
        Behaviors.setup { context =>
            new Fork(name, context).idle()
        }
    }

}

