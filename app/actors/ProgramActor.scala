package actors

import akka.actor.{Actor, Props}
import com.sun.org.apache.bcel.internal.classfile.ConstantString
import common.Constants
import common.InsOp.InsOp
import common._
import org.slf4j.LoggerFactory
import ManagerActor._

import scala.collection.mutable.ListBuffer
/**
 * Created by LiuZiwei on 2016/10/17.
 */
object ProgramActor{
  def props(instructions:List[Instruction]) = Props(new ProgramActor(instructions))
}
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
    } //初始化指令集状态
   MyFUStatus.update(0,FUStatus("Integer",1,false))
    MyFUStatus.update(1,FUStatus("Mult1",10,false))
  MyFUStatus.update(2,FUStatus("Mult2",10,false))
    MyFUStatus.update(3,FUStatus("Add",2,false))
    MyFUStatus.update(4,FUStatus("Divide",40,false)) //初始化功能单元状态
  for(i<- 0 until 31){RegStatus.update(i,-2)}
    log.info(s"${self.path} starting... instruction num:${instructions.length}")
  }
  @throws[Exception]
  override def postStop():Unit = {
    log.info(s"${self.path} stopping...")
  }


  def getFUNumOpt(op:InsOp) = { //为每个指令分配功能单元，若处于忙的状态则进入等待
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

  def getRegQStatus(i:Int) ={ //查看寄存器是否处于忙的状态
    if(i == -1){
      -1
    }else{
      RegStatus(i)
    }
  }

  def getOpString(i:Int) = {
    i match{
      case 0 => "integer"
      case 1 => "mult1"
      case 2 => "mult2"
      case 3 => "add"
      case 4 => "divide"
      case _ => ""
    }
  }

  def putInstruction(i:Int,clock:Int) = { //指令进入流水线
    val oldInsStatus = MyInsStatus(i)
    val insOpt = getFUNumOpt(oldInsStatus.instruction.op)
    if (insOpt.isDefined) { //若功能单元可以被分配
      MyInsStatus.update(i, InsStatus(oldInsStatus.instruction, oldInsStatus.times.updated(0, clock),insOpt.get)) //更新指令执行状态
      val oldStatus = MyFUStatus(insOpt.get)
      val instruction = oldInsStatus.instruction
      val qj = getRegQStatus(instruction.j)
      val qk = getRegQStatus(instruction.k)
      val rj = if(qj >= 0) false else true
      val rk = if(qk >= 0) false else true
      MyFUStatus.update(insOpt.get, FUStatus(oldStatus.name,oldStatus.time, true, getOpString(insOpt.get),
        oldInsStatus.instruction.dest, instruction.j, instruction.k,
        qj,qk,rj,rk
      )) //更新功能单元状态，置成忙状态并放入指令
      if(instruction.dest != -1) RegStatus.update(instruction.dest,insOpt.get)
      true
    }else false
  }

  def canRead(i:Int) = { //判断源寄存器是否可读
    val oldInsStatus = MyInsStatus(i)
    val oldFuStatus = MyFUStatus(oldInsStatus.FUNum)
    if(oldFuStatus.Rj && oldFuStatus.Rk){ //若源寄存器都处于准备好的状态则可读
      true
    }else{
      false
    }
  }

  def ReadSource(i:Int,clock:Int) = { //读源寄存器
    val oldInsStatus = MyInsStatus(i)
    val oldFuStatus = MyFUStatus(oldInsStatus.FUNum)
    if(oldFuStatus.Rj && oldFuStatus.Rk){
     MyInsStatus.update(i,oldInsStatus.copy(times = oldInsStatus.times.updated(1,clock))) //更新指令执行读操作周期
    }
  }

  def Exe(i:Int,clock:Int) = { //功能单元执行操作
    val oldInsStatus = MyInsStatus(i)
    val oldFuStatus = MyFUStatus(oldInsStatus.FUNum)
    val time = oldFuStatus.time - 1 //功能单元剩余执行周期减一
    if(time == 0){ //若已执行完，更新指令执行周期，且将功能单元状态还原
      MyInsStatus.update(i,oldInsStatus.copy(times = oldInsStatus.times.updated(2,clock)))
      val times = if(oldInsStatus.FUNum == 1 || oldInsStatus.FUNum == 2) Constants.MultTime
      else if(oldInsStatus.FUNum == 3) Constants.AddTime
      else if(oldInsStatus.FUNum == 4) Constants.DivTime
      else Constants.LdTime
      MyFUStatus.update(oldInsStatus.FUNum,oldFuStatus.copy(time = times))
    }else{ //只更新功能单元周期
      MyFUStatus.update(oldInsStatus.FUNum,oldFuStatus.copy(time = time))
    }
  }

  def canWrite(i:Int):Boolean = { //判断是否可以进行写操作
    val dest = MyInsStatus(i).instruction.dest
    if(dest == -1){ //若目的地址不为寄存器直接可以进行
      true
    }else { //否则判断之前的指令是否有源寄存器与此寄存器冲突
      for (j <- 0 until i) {
        if(MyInsStatus(j).instruction.j == dest || MyInsStatus(j).instruction.k == dest) {
          if(MyInsStatus(j).times.count(_ > 0) <2) return false
        }
      }
      true
    }
  }

  def WriteRes(i:Int,clock:Int) = { //进行写操作
    val oldInsStatus = MyInsStatus(i)
    val dest = oldInsStatus.instruction.dest
    val oldFuStatus = MyFUStatus(oldInsStatus.FUNum)
    MyInsStatus.update(i,oldInsStatus.copy(times=oldInsStatus.times.updated(3,clock)))
    MyFUStatus.update(oldInsStatus.FUNum,oldFuStatus.copy(busy=false,Op="",Fi= -1,Fj= -1,Fk= -1,Qj= -1,Qk= -1,Rj= false,Rk=false))
    RegStatus.update(dest,-2) //更新指令执行状态和占用的功能单元以及寄存器状态
    for(j<- 0 until 5){
      val old = MyFUStatus(j)
      if(old.Fj == dest) MyFUStatus.update(j,old.copy(Qj= -1,Rj=true))
      if(old.Fk == dest) MyFUStatus.update(j,old.copy(Qk= -1,Rk=true))
    } //将其他待读的寄存器置为准备好状态
  }


  def getNextState(time:List[Int]) = { //判断当前指令进行到第几个操作
    time.count(i => i > 0)
  }

  def bool2String(t:Boolean) = {
    if(t) "Yes"
    else "No"
  }

  def FU2String(i:Int) = {
    i match{
      case 0 => "integer"
      case 1 => "mult1"
      case 2 => "mult2"
      case 3 => "add"
      case 4 => "divide"
      case _ => ""
    }
  }
  def FuStatesToString(f:FUStatus) = {
    List(f.time.toString,f.name,bool2String(f.busy),f.Op,"F"+f.Fi.toString,"F"+f.Fj.toString,"F"+f.Fk.toString,FU2String(f.Qj),FU2String(f.Qk),bool2String(f.Rj),bool2String(f.Rk))
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
        val needExe = new ListBuffer[(Int,Int)]() // 指令编号，需要的操作 1读数 2执行 3写回
        val canPut = if(alreadyPutInstru+1<instructions.length && putInstruction(alreadyPutInstru+1,clock)) true else false
        for(i<- 0 to alreadyPutInstru){ //判断已经进入流水线的指令接下来的操作
          getNextState(MyInsStatus(i).times) match{
            case 1 => //读数
              if(canRead(i)) needExe.append((i,1))
            case 2 => //执行
              needExe.append((i,2))
            case 3 => //写回
              if(canWrite(i)) needExe.append((i,3))
            case _ =>

          }
        }
        needExe.foreach{e => //执行可执行的操作
          e._2 match{
            case 1 =>
              ReadSource(e._1,clock)
            case 2 =>
              Exe(e._1,clock)
            case 3 =>
              WriteRes(e._1,clock)
            case _ =>
          }
        }
        if(canPut) alreadyPutInstru = alreadyPutInstru + 1
      }
      send ! Result(RegStatus.map(FU2String(_)),MyFUStatus.map(f => FuStatesToString(f)).toList,MyInsStatus.map(i => i.times).toList)


    case ReStart(_) => //重新开始，将所有状态还原
      val send = sender()
      for(i<- instructions.indices){
        MyInsStatus.update(i,InsStatus(instructions(i)))
      }
      MyFUStatus.update(0,FUStatus("Integer",1,false))
      MyFUStatus.update(1,FUStatus("Mult1",10,false))
      MyFUStatus.update(2,FUStatus("Mult2",10,false))
      MyFUStatus.update(3,FUStatus("Add",2,false))
      MyFUStatus.update(4,FUStatus("Divide",40,false))
      for(i<- 0 until 31){RegStatus.update(i,-2)}
      clock = 0
      alreadyPutInstru = -1
      send ! Result(RegStatus.map(FU2String(_)),MyFUStatus.map(f => FuStatesToString(f)).toList,MyInsStatus.map(i => i.times).toList)


  }

}


