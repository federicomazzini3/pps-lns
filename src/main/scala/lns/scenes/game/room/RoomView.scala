package lns.scenes.game.room

import indigo.shared.FrameContext
import indigo.shared.datatypes.{ Rectangle, Vector2 }
import indigo.shared.materials.Material
import indigo.shared.scenegraph.{ Graphic, Group, SceneUpdateFragment }
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.Rooms
import lns.core.Assets.*
import lns.scenes.game.anything.SolidModel
import lns.scenes.game.room.RoomModel
import lns.scenes.game.room.door.{ DoorState, DoorView, Location }
import lns.scenes.game.shot.{ ShotModel, ShotView }
import lns.scenes.game.enemy.boney.{ BoneyModel, BoneyView }
import lns.scenes.game.enemy.mask.{ MaskModel, MaskView }
import lns.scenes.game.element.ElementView
import lns.scenes.game.enemy.nerve.{ NerveModel, NerveView }
import lns.scenes.game.enemy.parabite.{ ParabiteModel, ParabiteViewModel, ParabiteView }

object RoomView {

  def draw(context: FrameContext[StartupData], model: RoomModel, viewModel: RoomViewModel): Group =
    view(context, model, viewModel)

  def view(context: FrameContext[StartupData], model: RoomModel, viewModel: RoomViewModel): Group =
    Group()
      .addChild(RoomGraphic(context.startUpData))
      .addChild(doorView(context.startUpData, model, viewModel))
      .addChild(anythingView(context, model, viewModel))

  def doorView(startupData: StartupData, model: RoomModel, viewModel: RoomViewModel): Group =
    DoorView.view(startupData, model.doors, ())

  def anythingView(context: FrameContext[StartupData], model: RoomModel, viewModel: RoomViewModel): Group =
    model.anythings.foldLeft(Group())((s1, s2) =>
      s1.addChild(
        s2._2 match {
          case shot: ShotModel   => ShotView().draw(context, shot, ())
          case enemy: BoneyModel => BoneyView().draw(context, enemy, ())
          case enemy: MaskModel  => MaskView().draw(context, enemy, ())
          case enemy: NerveModel => NerveView().draw(context, enemy, ())
          case enemy: ParabiteModel =>
            ParabiteView()
              .draw(context, enemy, viewModel.anythings(s2._1).asInstanceOf[ParabiteViewModel]) // TODO: risolvere!
          case solid: SolidModel => ElementView().draw(context, solid, ())
          case _                 => Group()
        }
      )
    )
}

object RoomGraphic {

  def apply(startupData: StartupData): Graphic[Material.Bitmap] =
    Graphic(
      Rectangle(
        0,
        0,
        Rooms.roomSize,
        Rooms.roomSize
      ),
      1,
      Material.Bitmap(Rooms.EmptyRoom.name)
    )
}
