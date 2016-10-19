package controllers

import akka.actor.ActorRef
import akka.util.Timeout
import com.google.inject.name.Named
import play.api.mvc.{Action, Controller}
import org.slf4j.LoggerFactory
import com.google.inject.{Inject, Singleton}
import actors.ManagerActor._
import akka.pattern.ask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import common._

import scala.concurrent.Future
/**
 * Created by LiuZiwei on 2016/10/17.
 */
@Singleton
class ProgramController @Inject()(
                                   @Named("Config-ManagerActor") managerActor:ActorRef
                                  ) extends Controller{

  private val log = LoggerFactory.getLogger(this.getClass)
  implicit val timeout = Timeout(15.seconds)

  def create(name:String) = Action.async{
    val ins = List(LD(dest=6,j= -1,k= -1),LD(dest=2,j= -1,k= -1),MULT(dest=0,j=2,k=4),SUBD(dest=8,j=2,k=6),
      DIVD(dest=10,j=0,k=6),ADDD(dest=6,j=8,k=2))
    (managerActor ? CreatePro(name,ins)).map{
      case "ok" => Ok(views.html.Index(name,Nil,Nil,Nil))
      case "error" => Ok("error")
    }
  }

  def reStart(name:String) = Action.async{
    (managerActor ? ReStart(name)).map{
      case Result(reg,fuS,inS) =>
        Ok(views.html.Index(name,reg.toList,fuS,inS))
    }
  }

  def nextStep(name:String) = Action.async{
    (managerActor ? NextStep(name)).map{
      case Result(reg,fuS,inS) =>
//        Ok("reg" + reg.mkString("  ")+"\r\n\r\n"+
//        "fuS"+fuS.map(f => f.mkString("  ")).mkString("\r\n")+"\r\n\r\n"+
//        "inS"+inS.map(i => i.mkString("  ")).mkString("\r\n")
//      )
      Ok(views.html.Index(name,reg.toList,fuS,inS))
    }
  }

}
