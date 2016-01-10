package demo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

/**
 * Demonstration of the become operation. The become changes the behaviour of the actor based on a given condition.
 */

case class Born()
case class Dead()
case class Status()
case class Shoot(hits : Int)

/**
  * The dead or alive actor that has several messages that can be received in order to set determine if
  * someone is dead or alive. Following messages can be received
  * <li>Born alive person
  * <li>Dead a dead person
  * <li>Status current status of the person (dead or alive)
  * <li>Shoot(hits) number of shots that hit a person
  */

class DeadOrAliveActor extends Actor with ActorLogging {

  def alive : Receive = {
    case Shoot(hits) =>
      if(hits > 2)
        context.become(dead)
      else if(hits == 0)
        context.become(alive)
    case Status => log.info("Alive")
  }

  def dead : Receive = {
    case Shoot(hits) =>
      if(hits == 0)
        context.become(alive)
    case Status => log.info("Dead")
  }

  override def receive: Receive = {
    case Born => context.become(alive)
    case Status => log.info("I am nothing")
  }
}

object DeadOrAlive extends App {

  val system = ActorSystem("DeadOrAlive")

  val deadOrAlive = system.actorOf(Props[DeadOrAliveActor],name = "deadoralive")

  deadOrAlive ! Status
  deadOrAlive ! Born
  deadOrAlive ! Status

  deadOrAlive ! Shoot(5)
  deadOrAlive ! Status

  deadOrAlive ! Shoot(1)
  deadOrAlive ! Status

  deadOrAlive ! Shoot(0)
  deadOrAlive ! Status
}




