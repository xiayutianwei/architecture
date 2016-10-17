package actors

import akka.actor.{Props,Actor}
import com.sun.org.apache.bcel.internal.classfile.ConstantString
import common.Constants
import common.InsOp.InsOp
import common._
import org.slf4j.LoggerFactory
import ManagerActor._
/**
 * Created by LiuZiwei on 2016/10/17.
 */
class ProgramActor(
                  instructions:List[Instruction]
                    ) extends Actor{
  val log = LoggerFactory.getLogger(this.getClass)
  val  MyFUStatus = new Array[FUStatus](5)
  val MyInsStatus = new Array[InsStatus](instructions.length)



  val RegStatus = new Array[Int](31)


  var clock:Int = 0

  @throws[Exception]
  override def preStart():Unit = {
    for(i<- instructions.indices){
      MyInsStatus.update(i,InsStatus(instructions(i)))
    }
   MyFUStatus.update(0,FUStatus(0))
    MyFUStatus.update(1,FUStatus(10))
  MyFUStatus.update(2,FUStatus(10))
    MyFUStatus.update(3,FUStatus(2))
    MyFUStatus.update(4,FUStatus(40))
  for(i<- 0 until 31){RegStatus.update(i,-2)}
    log.info(s"${self.path} starting... instruction num:${instructions.length}")
  }
  @throws[Exception]
  override def postStop():Unit = {
    log.info(s"${self.path} stopping...")
  }


  def getFUNumOpt(op:InsOp) = {
    op match{
      case InsOp.ADD => if(MyFUStatus(FUNum.Add.id).busy) None else Some(FUNum.Add.id)
      case InsOp.DIVD => if(MyFUStatus(FUNum.Divide.id).busy) None else Some(FUNum.Divide.id)
      case InsOp.LD => if(MyFUStatus(FUNum.Interger.id).busy) None else Some(FUNum.Interger.id)
      case InsOp.MULT =>
        if(MyFUStatus(FUNum.Mult1.id).busy){
          if(MyFUStatus(FUNum.Mult2.id).busy){
            None
          }else{
            Some(FUNum.Mult2.id)
          }
        }else{
          Some(FUNum.Mult1.id)
        }
    }
  }

  def getRegQStatus(i:Int) ={
    if(i == -1){
      -1
    }else{
      RegStatus(i)
    }
  }

  def putInstruction(i:Int,clock:Int) = {
    val oldInsStatus = MyInsStatus(i)
    val insOpt = getFUNumOpt(oldInsStatus.instruction.op)
    if (insOpt.isDefined) {
      MyInsStatus.update(i, InsStatus(oldInsStatus.instruction, oldInsStatus.times.updated(0, clock),insOpt.get))
      val oldStatus = MyFUStatus(insOpt.get)
      val instruction = oldInsStatus.instruction
      val qj = getRegQStatus(instruction.j)
      val qk = getRegQStatus(instruction.k)
      val rj = if(qj >= 0) false else true
      val rk = if(qk >= 0) false else true
      MyFUStatus.update(insOpt.get, FUStatus(oldStatus.time, true, "",
        oldInsStatus.instruction.dest, instruction.j, instruction.k,
        qj,qk,rj,rk
      ))
      if(instruction.dest != -1) RegStatus.update(instruction.dest,insOpt.get)
      true
    }else false
  }


  def ReadSource(i:Int,clock:Int) = {
    val oldInsStatus = MyInsStatus(i)
    val oldFuStatus = MyFUStatus(oldInsStatus.FUNum)
    if(oldFuStatus.Rj && oldFuStatus.Rk){
     MyInsStatus.update(i,oldInsStatus.copy(times = oldInsStatus.times.updated(1,clock)))
    }
  }

  def Exe(i:Int,clock:Int) = {
    val oldInsStatus = MyInsStatus(i)
    val oldFuStatus = MyFUStatus(oldInsStatus.FUNum)
    val time = oldFuStatus.time - 1
    if(time == 0){
      MyInsStatus.update(i,oldInsStatus.copy(times = oldInsStatus.times.updated(2,clock)))
      val times = if(oldInsStatus.FUNum == 1 || oldInsStatus.FUNum == 2) Constants.MultTime
      else if(oldInsStatus.FUNum == 3) Constants.AddTime
      else if(oldInsStatus.FUNum == 4) Constants.DivTime
      else 0
      MyFUStatus.update(oldInsStatus.FUNum,oldFuStatus.copy(time = times))
    }else{
      MyFUStatus.update(oldInsStatus.FUNum,oldFuStatus.copy(time = time))
    }
  }

  def WriteRes(i:Int,clock:Int) = {
    val oldInsStatus = MyInsStatus(i)
    val dest = oldInsStatus.instruction.dest
    val oldFuStatus = MyFUStatus(oldInsStatus.FUNum)
    MyInsStatus.update(i,oldInsStatus.copy(times=oldInsStatus.times.updated(3,clock)))
    MyFUStatus.update(oldInsStatus.FUNum,oldFuStatus.copy(busy=false,Op="",Fi= -1,Fj= -1,Fk= -1,Qj= -1,Qk= -1,Rj= false,Rk=false))
    RegStatus.update(dest,-2)
    for(j<- 0 until 5){
      val old = MyFUStatus(j)
      if(old.Fj == dest) MyFUStatus.update(j,old.copy(Qj= -1,Rj=true))
      if(old.Fk == dest) MyFUStatus.update(j,old.copy(Qk= -1,Rk=true))
    }
  }


  def getNextState(time:List[Int]) = {
    time.count(i => i > 0)
  }
  var alreadyPutInstru = -1
  override def receive:Receive = {
    case NextStep(_) =>
      val send = sender()
      clock = clock + 1
      if(clock == 1){
          putInstruction(0,clock)
        alreadyPutInstru = 0
      }else{
        if(putInstruction(alreadyPutInstru+1,clock)) alreadyPutInstru = alreadyPutInstru+1
        for(i<- 0 to alreadyPutInstru){
          getNextState(MyInsStatus(i).times) match{
            case 1 => //读数
              ReadSource(i,clock)
            case 2 => //执行
              Exe(i,clock)
            case 3 => //写会
              WriteRes(i,clock)
          }
        }
      }
//      send !

  }

}


