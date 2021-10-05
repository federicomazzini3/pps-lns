package lns.scenes.menu

sealed trait MenuModel

object MenuModel {
  val initial: MenuModel = MenuModelImpl()

  private case class MenuModelImpl() extends MenuModel
}