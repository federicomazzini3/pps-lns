package lns

import indigo.*
import indigo.scenes.*
import indigoextras.subsystems.FPSCounter
import lns.core.{ Animations, Assets, Model, ViewModel }
import lns.scenes.end.EndScene
import lns.scenes.game.GameScene
import lns.scenes.loading.LoadingScene
import lns.scenes.menu.MenuScene

import scala.scalajs.js.annotation.JSExportTopLevel

/**
 * Game boot data
 */
final case class BootData(screenDimensions: Rectangle)

/**
 * Game startup data built from boot data
 */
final case class StartupData(screenDimensions: Rectangle, dungeonGenerator: Option[String] = None)

@JSExportTopLevel("IndigoGame")
object LostNSouls extends IndigoGame[BootData, StartupData, Model, ViewModel] {

  def eventFilters: EventFilters = EventFilters.BlockAll

  /**
   * Our scenes list defined in appearance order
   */
  def scenes(bootData: BootData): NonEmptyList[Scene[StartupData, Model, ViewModel]] = NonEmptyList(
    MenuScene(),
    LoadingScene(),
    GameScene(),
    EndScene()
  )

  /**
   * Loads tau-prolog engine on the client browser using ScalaJS dom extension
   */
  def loadProlog(): Unit = {
    import org.scalajs.dom
    import org.scalajs.dom.document
    import org.scalajs.dom.html.Script

    val prologScript: Script = document.createElement("SCRIPT").asInstanceOf[Script]
    prologScript.src = "assets/prolog/tau-prolog.js"
    document.head.appendChild(prologScript)
  }

  def boot(flags: Map[String, String]): Outcome[BootResult[BootData]] = {

    loadProlog()

    var currentViewport = for {
      width  <- flags.get("width")
      height <- flags.get("height")
    } yield GameViewport(width.toInt, height.toInt)

    val config = GameConfig.default.withViewport(currentViewport.getOrElse(GameViewport.at720p))

    Outcome(
      BootResult(config, BootData(config.screenDimensions))
        .withAssets(Assets.initialAssets())
        .withFonts(Assets.initialFont())
    )
  }
  def setup(bootData: BootData, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[StartupData]] =
    Outcome(
      Startup
        .Success(
          StartupData(bootData.screenDimensions, assetCollection.findTextDataByName(AssetName("dungeon_generator")))
        )
        .addAnimations(Animations())
    )

  def initialScene(bootData: BootData): Option[SceneName] = None

  def initialModel(startupData: StartupData): Outcome[Model] = Outcome(Model.initial(startupData))

  def initialViewModel(startupData: StartupData, model: Model): Outcome[ViewModel] =
    Outcome(ViewModel.initial(startupData))

  def present(context: FrameContext[StartupData], model: Model, viewModel: ViewModel): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

  def updateModel(context: FrameContext[StartupData], model: Model): GlobalEvent => Outcome[Model] = _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[StartupData],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)
}
