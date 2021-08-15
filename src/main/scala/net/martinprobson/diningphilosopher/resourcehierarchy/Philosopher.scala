package net.martinprobson.diningphilosopher.resourcehierarchy

import com.typesafe.scalalogging.{LazyLogging, Logger}
import net.martinprobson.diningphilosopher.resourcehierarchy.Philosopher.sleep

import java.util.concurrent.{Executors, ThreadFactory}
import scala.annotation.tailrec
import scala.concurrent.duration.{Duration, DurationDouble, DurationInt, MINUTES, SECONDS}

class Philosopher(
  val name: String,
  private var state: String,
  private val leftFork: Fork,
  private val rightFork: Fork
) extends Runnable with LazyLogging {

  private val log = Logger[Philosopher]
  private val EATING_TIME                   = 100.millis
  private val THINKING_TIME                 = 100.millis
  private def setState(state: String): Unit = this.state = state
  def getState: String                      = state

  def eating(): Unit = {
    log.info(s"$name is eating.")
    setState("Eating")
  }
  def thinking(): Unit = setState("Thinking")

  override def toString =
    s"Philosopher(name=$name, state=$state, leftFork=$leftFork, rightFork=$rightFork)"

  override def run(): Unit = process()

  @tailrec
  private def process(): Unit = {
    /*
    The code fragment below implements the "Resource hierarchy solution".
    Without this code, the program will deadlock right away.
     */
    val (fork1, fork2) =
      if (leftFork.number < rightFork.number) (leftFork, rightFork)
      else (rightFork, leftFork)
    while (fork1.inUse) sleep(100.millis)
    fork1.pickUp(this)
    while (fork2.inUse) sleep(100.millis)
    fork2.pickUp(this)
    eating()
    sleep(EATING_TIME)
    fork1.putDown()
    fork2.putDown()
    thinking()
    sleep(THINKING_TIME)
    process()

  }
}

object Philosopher extends App {
  def apply(name: String, leftFork: Fork, rightFork: Fork): Philosopher =
    new Philosopher(name, "Thinking", leftFork, rightFork)

  private def sleep(duration: Duration): Unit =
    Thread.sleep(duration.toMillis)

  val forks = (1 to 5).toVector.map{ i => Fork(i)}
  val philosophers = List("Descartes","Nietzsche","Kant","Hume","Plato").zipWithIndex.map{ case (name,i) =>
    Philosopher(name,forks(i),forks((i+1) % 4))
  }
  val pool = Executors.newFixedThreadPool(5)
  philosophers.foreach(pool.submit(_))
}
