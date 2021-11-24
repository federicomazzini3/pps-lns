package lns.scenes.end
import indigo.shared.events.GlobalEvent

sealed trait EndModel

object EndModel {
  val initial: EndModel = EndModelImpl()

  private case class EndModelImpl() extends EndModel
}

case object Restart extends GlobalEvent
