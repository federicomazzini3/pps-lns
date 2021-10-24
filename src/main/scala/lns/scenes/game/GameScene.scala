package lns.scenes.game

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{ EmptyScene, Model, Substitution, Term, ViewModel }
import lns.scenes.game.character.*
import lns.scenes.game.room.{ ArenaRoom, RoomModel, RoomView }
import lns.scenes.game.room.RoomView.*

import scala.language.implicitConversions

final case class GameScene() extends EmptyScene {
  type SceneModel     = GameModel
  type SceneViewModel = GameViewModel

  def name: SceneName = GameScene.name

  def modelLens: Lens[Model, SceneModel]             = Lens(m => m.game, (m, sm) => m.copy(game = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(vm => vm.game, (vm, svm) => vm.copy(game = svm))

  override def updateModel(
      context: FrameContext[StartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {

    case FrameTick =>
      for {
        updatedCharacter <- model.character.update(context)(model.room)
        updatedGameModel <- model.copy(character = updatedCharacter)
        // updatedRoom       <- model.room.update(context)
        // updatedGameModel <- model.copy(character = updatedCharacter, room = updatedRoom)
        // updatedGameModel2 <- fun(updatedGameModel.character, updatedGameModel.room.getRobaCheFaMale())
      } yield updatedGameModel

    case event @ DungeonGenerationResult(_) =>
      println("RISULTATO GENERAZIONE")
      println(event.getResult())

      Outcome(model)

    case _ =>
      Outcome(model)
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    RoomView.draw(context, model.room, ()) |+|
      CharacterView().draw(context, model.character, ())
}

object GameScene {
  val name: SceneName = SceneName("game")
}

case class DungeonGenerationResult(sub: Substitution) extends GlobalEvent {

  def getResult() = getRooms(sub.links("L"))

  private def getRooms(baseTerm: Term): List[String] = {

    if (baseTerm.id == "[]")
      return Nil

    val room: String = baseTerm.args(0).args(2).id match {
      case "s" => "Start(" + baseTerm.args(0).args(0) + "," + baseTerm.args(0).args(1) + ")"
      case "a" => "Arena(" + baseTerm.args(0).args(0) + "," + baseTerm.args(0).args(1) + ")"
      case "i" => "Item(" + baseTerm.args(0).args(0) + "," + baseTerm.args(0).args(1) + ")"
      case "e" => "Empty(" + baseTerm.args(0).args(0) + "," + baseTerm.args(0).args(1) + ")"
      case "b" => "Boss(" + baseTerm.args(0).args(0) + "," + baseTerm.args(0).args(1) + ")"
      case _   => "no"
    }

    room :: List.concat(getRooms(baseTerm.args(1)))
  }
}
