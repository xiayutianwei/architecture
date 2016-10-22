package common

import common.InsOp.InsOp

/**
 * Created by LiuZiwei on 2016/10/17.
 */
trait Instruction {
  implicit val op:InsOp = InsOp.other
 implicit val dest:Int = -1
 implicit val j:Int = -1
  implicit val k:Int = -1
  override def toString:String
}
case object InsOp extends Enumeration{

  type InsOp = Value
  val other = Value(-1)
  val LD = Value(1)
  val ADD = Value(2)
  val MULT = Value(4)
  val DIVD = Value(5)
}

case class LD(override val op:InsOp = InsOp.LD,override val dest:Int,override val j:Int,override val k:Int)extends Instruction{
  override def toString:String = {
    s"LD F$dest"
  }
}
case class ADDD(override val op:InsOp = InsOp.ADD,override val dest:Int,override val j:Int,override val k:Int) extends Instruction{
  override def toString = {
    s"ADDD F$dest F$j F$k"
  }
}
case class SUBD(override val op:InsOp = InsOp.ADD,override val dest:Int,override val j:Int,override val k:Int) extends Instruction{
  override def toString = {
    s"SUBD F$dest F$j F$k"
  }
}
case class MULT(override val op:InsOp = InsOp.MULT,override val dest:Int,override val j:Int,override val k:Int) extends Instruction{
  override def toString = {
    s"MULT F$dest F$j F$k"
  }
}
case class DIVD(override val op:InsOp = InsOp.DIVD,override val dest:Int,override val j:Int,override val k:Int) extends Instruction{
  override def toString = {
    s"DIVD F$dest F$j F$k"
  }
}









