package actors

/**
 * Created by LiuZiwei on 2016/10/17.
 */
object ManagerActor{
  case class CreatePro(name:String)
  case class NextStep(name:String)
  case class Result(reg:Array[Int],FUStatus:List[List[String]],InsStatus:List[List[Int]])
}
class ManagerActor {

}
