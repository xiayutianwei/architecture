package common

/**
 * Created by LiuZiwei on 2016/10/17.
 */
trait Status

case class InsStatus(instruction:Instruction,times:List[Int] = List(-1,-1,-1,-1),FUNum:Int = -1) extends Status
case class FUStatus(time:Int=0,busy:Boolean=false,Op:String="",Fi:Int= -1,Fj:Int= -1,Fk:Int= -1,Qj:Int= -1,Qk:Int= -1,Rj:Boolean=false,Rk:Boolean=false)


case object FUNum extends Enumeration{
  type FUNum = Value
  val Interger = Value(0)
  val Mult1 = Value(1)
  val Mult2 = Value(2)
  val Add = Value(3)
  val Divide = Value(4)
}




