package lns.scenes.loading

import indigo.*
import indigo.scenes.*
import indigoextras.subsystems.*
import indigo.scenes.SceneEvent.JumpTo
import lns.StartupData
import lns.core.{ Assets, EmptyScene, Model, ScalaPrologSession, ViewModel }
import lns.scenes.game.{ DungeonGenerationResult, GameScene }
import lns.scenes.loading.LoadingModel

import scala.language.implicitConversions
import scala.scalajs.js

final case class LoadingScene() extends EmptyScene {
  type SceneModel     = LoadingModel
  type SceneViewModel = Unit

  def name: SceneName = LoadingScene.name

  def modelLens: Lens[Model, SceneModel] = Lens(m => m.loading, (m, sm) => m.copy(loading = sm))

  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(_ => (), (vm, _) => vm)

  override def subSystems: Set[SubSystem] = Set(AssetBundleLoader)

  override def updateModel(
      context: FrameContext[StartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case FrameTick =>
      model match {
        case LoadingModel.NotStarted =>
          LoadingModel
            .InProgress(0)
            .addGlobalEvents(
              AssetBundleLoaderEvent.Load(BindingKey("Loading"), Assets.secondary())
            )

        case LoadingModel.AwaitPrologConsult(session) =>
          session.consultResult match {
            case Some(true) => session.query("generateDungeon(30,L)."); LoadingModel.AwaitPrologQuery(session)
            case _          => model
          }
        case LoadingModel.AwaitPrologQuery(session) =>
          session.queryResult match {
            case Some(true) => session.answer(); LoadingModel.AwaitPrologAnswer(session)
            case _          => model
          }

        case LoadingModel.AwaitPrologAnswer(session) =>
          session.answerResult match {
            case Some(substitution) =>
              LoadingModel.Complete
                .addGlobalEvents(JumpTo(GameScene.name), DungeonGenerationResult(substitution))
            case _ => model
          }
        case _ =>
          model
      }

    case AssetBundleLoaderEvent.LoadProgress(_, percent, _, _) =>
      LoadingModel.InProgress(percent)

    case AssetBundleLoaderEvent.Success(_) =>
      val session = ScalaPrologSession()
      session.consult(context.startUpData.dungeonGenerator.get)

      LoadingModel.AwaitPrologConsult(session)

    case AssetBundleLoaderEvent.Failure(_, _) =>
      LoadingModel.Error

    case _ =>
      model
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    LoadingView.draw(
      context.startUpData.screenDimensions,
      model
    )

}

object LoadingScene {
  val name: SceneName = SceneName("loading")
}
