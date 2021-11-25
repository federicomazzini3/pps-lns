package lns.scenes.game.bosses

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.{ Loki, Parabite }
import lns.core.anythingAssets.LokiAsset
import lns.scenes.game.enemies.*

/**
 * Boonie enemy view elements builder
 */
trait Loki extends LokiAsset {

  val bodySprite: Sprite[Material.Bitmap] = spriteAnimation("loki_body")

  /**
   * Builds the body view
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   body view Sprite
   */
  def bodyView(model: BossModel, viewModel: BossViewModel): Sprite[Material.Bitmap] =
    bodyAnimation(model, viewModel)
      .withRef(0, 0)
      .moveTo(0, 0)

  /**
   * Calculates current animation frame for the defence animation
   * @param timer
   *   current animation timer from X to 0
   * @return
   *   anim frame
   */
  def getFrame(timer: Double): Int =
    Math.floor((Loki.hideTime - timer) / Loki.hideFrameTime).toInt

  /**
   * Plays the animation cycle related to [[EnemyState]]
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   the updated body Sprite
   */
  def bodyAnimation(model: BossModel, viewModel: BossViewModel): Sprite[Material.Bitmap] =
    bodySprite.changeCycle(CycleLabel("move")).play()
  /*
    model.status.head match {
      case (EnemyState.Attacking, _) => bodySprite.changeCycle(CycleLabel("attack")).play()
      case (EnemyState.Hiding, _) =>
        viewModel.animationTimer match {
          case 0 => bodySprite.changeCycle(CycleLabel("defence")).jumpToLastFrame()
          case t =>
            bodySprite
              .changeCycle(CycleLabel("defence"))
              .jumpToFrame(getFrame(t))
        }
      case _ => bodySprite.changeCycle(CycleLabel("move")).play()
    }
   */
}
