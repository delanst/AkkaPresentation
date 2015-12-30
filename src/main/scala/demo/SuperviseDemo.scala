package demo

import akka.actor.SupervisorStrategy.{Resume, Escalate, Restart}
import akka.actor._

import scala.concurrent.duration._

case class B1(message: String)
case class B2(message: String)
case class B3(message: String)

/**
 * Demonstration of the supervisor strategy. The B1Actor receives a B1 messages that in his turn sends a B2 message
 * to the B2Actor. B2Actor throws every 2 times an UnsupportedOperationException. This exception is caught by
 * the supervisorStrategy. It will that resume the actor as if nothiing happened. Other operations
 * - Restart -> restart the actor
 * - Escalate -> escalates the error to the sender
 * The OneForOneStrategy defines that the actor will handle the child actor that is in error state.
 * The AllForOneStrategy would apply the strategy to all child actors.
 */

class B1Actor extends Actor with ActorLogging {

  val b2Actor = context.actorOf(Props(new B2Actor(self)),"b2")

  override def receive: Receive = {
    case B1(message) =>
      log.info(message)
      b2Actor ! B2(message)
    case _ => log.error("Unexpected message type")
  }

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _ : UnsupportedOperationException =>
        log.error("Strategy caught the UnsupportedOperationException")
        Resume  // Resume, Restart and Escalate
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  @throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {

  }
}

class B2Actor(a2 : ActorRef) extends Actor with ActorLogging {
  var counter = 0
  override def receive: Receive = {
    case B2(message) =>
      log.info("counter=" + counter)
      if (counter % 2 == 0) {
        counter = counter + 1
        throw new UnsupportedOperationException
    } else {
        counter=counter+1
        log.info("B2 = " + message)
      }
    case _ => log.error("Unexpected message type")
  }
}

object SuperviseDemoApp extends App {

  val system = ActorSystem("SuperviseDemo")

  val a1Actor = system.actorOf(Props[B1Actor],"a1")

  a1Actor ! B1("This is the first message")
  a1Actor ! B1("This is the second message")

}
