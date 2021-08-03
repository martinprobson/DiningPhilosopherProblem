class Fork(
  val number: Int,
  private var available: Boolean,
  private var heldBy: Option[Philosopher]
) {

  def isAvailable: Boolean = available

  def inUse: Boolean = !available

  def pickUp(philosopher: Philosopher): Unit = {
    heldBy = Some(philosopher)
    available = false
  }

  def putDown(): Unit = {
    heldBy = None
    available = true
  }

  override def toString: String = {

    val h = heldBy match {
      case None    => "None"
      case Some(p) => p.name
    }
    s"Fork(name=$number, available=$available, heldBy=$h"
  }
}

object Fork {
  def apply(number: Int): Fork = new Fork(number, true, None)
}
