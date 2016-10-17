package modules

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

import actors._
/**
 * Created by LiuZiwei on 2016/10/17.
 */
class InitModules extends AbstractModule with AkkaGuiceSupport{
  override def configure() = {
    bindActor[ManagerActor]("Config-ManagerActor")
  }
}
