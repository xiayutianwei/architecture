package actors

import actors.ManagerActor._
import akka.actor.{Actor, Props, Terminated}
import common._
import org.slf4j.LoggerFactory
import com.google.inject.{Inject, Singleton}
/**
 * Created by LiuZiwei on 2016/10/17.
 */
object ManagerActor{
  case class CreatePro(name:String,ins:List[Instruction])
  case class NextStep(name:String)
  case class Result(reg:Array[String],FUStatus:List[List[String]],InsStatus:List[List[Int]])
  case class ReStart(name:String)
  def props = Props[ManagerActor]
}
@Singleton
class ManagerActor @Inject() extends Actor{

  val log = LoggerFactory.getLogger(this.getClass)
  @throws[Exception]
  override def preStart():Unit = {
    log.info(s"${self.path} starting...")
  }
  @throws[Exception]
  override def postStop():Unit = {
    log.info(s"${self.path} stopping...")
  }
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

    case r@ReStart(name) =>
      val send = sender()
      val child = context.child(name)
      if(child.isDefined) child.get.forward(r)
      else send ! "error"

    case Terminated(child) =>
      log.info(s"${child.path} terminated...")

  }

}
