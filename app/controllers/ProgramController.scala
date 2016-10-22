package controllers

import akka.actor.ActorRef
import akka.util.Timeout
import com.google.inject.name.Named
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import org.slf4j.LoggerFactory
import com.google.inject.{Inject, Singleton}
import actors.ManagerActor._
import akka.pattern.ask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import common._

import scala.concurrent.Future
import scala.io.Source

/**
 * Created by LiuZiwei on 2016/10/17.
 */
@Singleton
class ProgramController @Inject()(
                                   @Named("Config-ManagerActor") managerActor:ActorRef
                                  ) extends Controller{

  private val log = LoggerFactory.getLogger(this.getClass)
  implicit val timeout = Timeout(15.seconds)


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

  def create = Action.async(parse.multipartFormData){implicit request =>
    request.body.file("instruction").map{tmpFile =>
      val name = request.body.dataParts.get("name").getOrElse(Seq("")).head
      val file = tmpFile.ref.file
      val source = Source.fromFile(file)
      val lines = source.getLines().toList
      source.close()
      val ins = lines.flatMap{line =>
        val ins = line.split(" ")
        if(ins.length != 4){
          None
        }else {
          val op = ins.head
          val dest = ins(1).drop(1).toInt
          val j = ins(2)
          val k = ins(3)
          op match{
            case "LD" =>
              Some(LD(InsOp.LD,dest,-1,-1))
            case "ADDD" =>
              Some(ADDD(InsOp.ADD,dest,j.drop(1).toInt,k.drop(1).toInt))
            case "SUBD" =>
              Some(ADDD(InsOp.ADD,dest,j.drop(1).toInt,k.drop(1).toInt))
            case "DIVD" =>
              Some(DIVD(InsOp.DIVD,dest,j.drop(1).toInt,k.drop(1).toInt))
            case "MULT" =>
              Some(MULT(InsOp.MULT,dest,j.drop(1).toInt,k.drop(1).toInt))
            case _ =>
              None
          }
        }
      }

      (managerActor ? CreatePro(name,ins)).map{
        case "ok" => Ok(views.html.Index(name,Nil,Nil,Nil))
        case "error" => Ok("error")
      }
    }.getOrElse {
      Future.successful(
        Ok("no file"))
    }
  }

  def createPage = Action{
    Ok(views.html.Create())
  }

}
