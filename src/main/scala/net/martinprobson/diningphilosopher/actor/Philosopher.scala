package net.martinprobson.diningphilosopher.actor

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import net.martinprobson.diningphilosopher.actor.Fork.{ForkPosition, ForkRequest, Put, Take}
import net.martinprobson.diningphilosopher.actor.Philosopher.{Busy, Eat, SetTimeout, State, Taken, Think}

class Philosopher private(name: String,
                          context: ActorContext[State],
                          eatingTimer: ActorRef[State],
                          thinkingTimer: ActorRef[State],
                          left: ActorRef[ForkRequest],
                          right: ActorRef[ForkRequest]) {

    private def eat(): Behavior[State] = Behaviors.receiveMessage[State] { message =>
        context.log.info(s"$name is eating")
        message match {
            case Think =>
                left ! Put
                right ! Put
                thinkingTimer ! SetTimeout(context.self)
                think()
            case _ => Behaviors.unhandled
        }
    }

    private def rightWaiting(): Behavior[State] = Behaviors.receiveMessage[State] {
        case Taken(_, Fork.Right) => eatingTimer ! SetTimeout(context.self)
            eat()
        case Busy(_) => left ! Put
            context.self ! Eat
            think()
        case _ => Behaviors.unhandled
    }

    private def leftWaiting(): Behavior[State] = Behaviors.receiveMessage[State] {
        case Taken(_, Fork.Left) => eatingTimer ! SetTimeout(context.self)
            eat()
        case Busy(_) => right ! Put
            context.self ! Eat
            think()
        case _ => Behaviors.unhandled
    }

    private def denied(): Behavior[State] = Behaviors.receiveMessage[State] {
        case Taken(fork, _) => fork ! Put
            context.self ! Eat
            think()
        case Busy(_) =>
            context.self ! Eat
            think()
        case _ => Behaviors.unhandled
    }

    private def hungry(): Behavior[State] = Behaviors.receiveMessage[State] { message =>
        context.log.debug(s"$name is hungry")
        message match {
            case Taken(_, Fork.Left) => rightWaiting()
            case Taken(_, Fork.Right) => leftWaiting()
            case Busy(_) => denied()
            case _ => Behaviors.unhandled
        }
    }

    private def think(): Behavior[State] = Behaviors.receiveMessage[State] { message =>
        context.log.debug(s"$name is thinking")
        message match {
            case Eat =>
                left ! Take(context.self, Fork.Left)
                right ! Take(context.self, Fork.Right)
                hungry()
            case _ => Behaviors.unhandled
        }
    }
}


object Philosopher {

    sealed trait State

    object Eat extends State

    object Think extends State

    case class Timeout(sendTo: ActorRef[State]) extends State

    case class SetTimeout(sendTo: ActorRef[State]) extends State

    case class Taken(fork: ActorRef[ForkRequest], position: ForkPosition) extends State

    case class Busy(sendTo: ActorRef[ForkRequest]) extends State

    def apply(name: String, leftFork: ActorRef[ForkRequest], rightFork: ActorRef[ForkRequest]): Behavior[State] =
        Behaviors.setup { context =>
            val eatingTimer = context.spawn(Timer(s"eating timer for $name", Think), "eatingTimer")
            val thinkingTimer = context.spawn(Timer(s"thinking timer for $name", Eat), "thinkingTimer")
            new Philosopher(name, context, eatingTimer, thinkingTimer, leftFork, rightFork).think()
        }
}



