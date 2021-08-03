import Philosopher.sleep

import java.util.concurrent.Executors
import scala.annotation.tailrec
import scala.concurrent.duration.{Duration, DurationInt}

class Philosopher(
  val name: String,
  private var state: String,
  private val leftFork: Fork,
  private val rightFork: Fork
) extends Runnable {

  private val EATING_TIME                   = 2.seconds
  private val THINKING_TIME                 = 2.seconds
  private def setState(state: String): Unit = this.state = state

  def eating(): Unit = {
    println(s"Philosopher $name is eating.")
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
    while (fork1.inUse) sleep(1.seconds)
    fork1.pickUp(this)
    while (fork2.inUse) sleep(1.seconds)
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

  val fork = Vector(
    Fork(1),
    Fork(2),
    Fork(3),
    Fork(4),
    Fork(5)
  )
  val philosopher = List(
    Philosopher("Philosopher-1", fork(0), fork(4)),
    Philosopher("Philosopher-2", fork(1), fork(0)),
    Philosopher("Philosopher-3", fork(2), fork(1)),
    Philosopher("Philosopher-4", fork(3), fork(2)),
    Philosopher("Philosopher-5", fork(4), fork(3))
  )
  val pool = Executors.newFixedThreadPool(5)
  philosopher.foreach(pool.submit(_))
}
