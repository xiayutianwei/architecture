package controllers

import akka.actor.ActorRef
import akka.util.Timeout
import com.google.inject.name.Named
import play.api.mvc.{Action, Controller}
import org.slf4j.LoggerFactory
import com.google.inject.{Singleton,Inject}
import actors.ManagerActor._
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
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
    (managerActor ? CreatePro(name,List())).map{
      case "ok" => Ok("")
      case "error" => Ok("")
    }
  }

  def nextStep(name:String) = Action.async{
    (managerActor ? NextStep(name)).map{
      case Result(reg,fuS,inS) => Ok("")
    }
  }

}
