package modules


import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import actors._
import play.api.{Configuration, Environment}
/**
 * Created by LiuZiwei on 2016/10/17.
 */
class InitModules(
                   environment: Environment,
                   configuration: Configuration)  extends AbstractModule with AkkaGuiceSupport{
  override def configure():Unit = {
    bindActor[ManagerActor]("Config-ManagerActor")
  }
}
