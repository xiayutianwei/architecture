package actors

import actors.ManagerActor.{NextStep, CreatePro}
import akka.actor.Actor
import common._

/**
 * Created by LiuZiwei on 2016/10/17.
 */
object ManagerActor{
  case class CreatePro(name:String,ins:List[Instruction])
  case class NextStep(name:String)
  case class Result(reg:Array[Int],FUStatus:List[List[String]],InsStatus:List[List[Int]])
}
class ManagerActor extends Actor{

  override def receive:Receive = {
    case CreatePro(name,ins) =>
      val send = sender()
      context.actorOf(ProgramActor.props(ins),name)
      send ! "ok"

    case r@NextStep(name) =>
      val send = sender()
      val child = context.child(name)
      if(child.isDefined) child.get.forward(r)
      else send ! "error"

  }

}
