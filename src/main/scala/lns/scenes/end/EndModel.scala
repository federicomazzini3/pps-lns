package lns.scenes.end

sealed trait EndModel

object EndModel {
  val initial: EndModel = EndModelImpl()

  private case class EndModelImpl() extends EndModel
}
