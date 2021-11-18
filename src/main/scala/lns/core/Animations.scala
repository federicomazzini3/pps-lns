package lns.core

import indigo.*
import lns.core.Animations.Explosion.size
import lns.scenes.game.anything.{ DynamicState, FireState }
import lns.scenes.game.enemy.EnemyState

/**
 * Factory for Animation
 */
object Animations {

  def apply(): List[Animation] =
    List(Isaac.body, Boney.body, Nerve.body, Parabite.body, Explosion.body)
  def generateFrame(r: Range)(f: Int => Frame): List[Frame]   = r.toList.map(f(_))
  def generateFramesList(l: List[Frame]): NonEmptyList[Frame] = NonEmptyList.fromList(l).get

  object Isaac {
    /*Isaac head*/
    val headWidth: Int  = 28
    val headHeight: Int = 25

    def headCrop(state: FireState | DynamicState, eyesOpen: Boolean): Rectangle = state match {
      case FireState.FIRE_LEFT | DynamicState.MOVE_LEFT if eyesOpen =>
        Rectangle(250, 25, headWidth, headHeight)
      case FireState.FIRE_LEFT | DynamicState.MOVE_LEFT =>
        Rectangle(290, 25, headWidth, headHeight)
      case FireState.FIRE_RIGHT | DynamicState.MOVE_RIGHT if eyesOpen =>
        Rectangle(90, 25, headWidth, headHeight)
      case FireState.FIRE_RIGHT | DynamicState.MOVE_RIGHT =>
        Rectangle(130, 25, headWidth, headHeight)
      case FireState.FIRE_UP | DynamicState.MOVE_UP if eyesOpen =>
        Rectangle(170, 25, headWidth, headHeight)
      case FireState.FIRE_UP | DynamicState.MOVE_UP =>
        Rectangle(210, 25, headWidth, headHeight)
      case _ if eyesOpen =>
        Rectangle(10, 25, headWidth, headHeight)
      case _ =>
        Rectangle(50, 25, headWidth, headHeight)
    }

    /*Isaac body*/
    val bodyWidth: Int  = 18
    val bodyHeight: Int = 15

    def generateBodyFrame(x: Int, y: Int): NonEmptyList[Frame] =
      generateFramesList(
        generateFrame(0 until 10)(i => Frame(Rectangle(x + (i * 32), y, bodyWidth, bodyHeight), Millis(80)))
      )

    val body: Animation = Animation
      .create(AnimationKey("isaac_body"), Cycle.create("walking_left_right", generateBodyFrame(15, 123)))
      .addCycle(Cycle.create("walking_up_down", generateBodyFrame(15, 80)))
  }

  object Boney {
    /*Boney head*/
    val headWidth: Int  = 28
    val headHeight: Int = 25

    def headCrop(state: DynamicState): Rectangle = state match {
      case DynamicState.MOVE_LEFT =>
        Rectangle(32, 0, headWidth, headHeight)
      case DynamicState.MOVE_RIGHT =>
        Rectangle(32, 0, headWidth, headHeight)
      case DynamicState.MOVE_UP =>
        Rectangle(64, 0, headWidth, headHeight)
      case _ =>
        Rectangle(0, 0, headWidth, headHeight)
    }

    /*Boney body*/
    val bodyWidth: Int  = 18
    val bodyHeight: Int = 13

    def generateBodyFrame(x: Int, y: Int): NonEmptyList[Frame] =
      generateFramesList(
        generateFrame(0 until 10)(i => Frame(Rectangle(x + (i * 32), y, bodyWidth, bodyHeight), Millis(80)))
      )

    val body: Animation = Animation
      .create(AnimationKey("boney_body"), Cycle.create("walking_left_right", generateBodyFrame(0, 55)))
      .addCycle(Cycle.create("walking_up_down", generateBodyFrame(0, 35)))
  }

  object Mask {
    /*Mask head*/
    val headWidth: Int  = 28
    val headHeight: Int = 33

    def headCrop(state: FireState | DynamicState): Rectangle = state match {
      case FireState.FIRE_LEFT | DynamicState.MOVE_LEFT =>
        Rectangle(2, 0, headWidth, headHeight)
      case FireState.FIRE_RIGHT | DynamicState.MOVE_RIGHT =>
        Rectangle(2, 32, headWidth, headHeight)
      case FireState.FIRE_UP | DynamicState.MOVE_UP =>
        Rectangle(34, 0, headWidth, headHeight)
      case _ =>
        Rectangle(34, 32, headWidth, headHeight)
    }
  }

  object Nerve {
    /*Nerve body*/
    val bodyWidth: Int  = 26
    val bodyHeight: Int = 50

    def generateBodyFrame(x: Int, y: Int): NonEmptyList[Frame] =
      generateFramesList(
        generateFrame(0 until 6)(i => Frame(Rectangle(x + (i * (bodyWidth + 6)), y, bodyWidth, bodyHeight), Millis(80)))
      )

    val body: Animation = Animation
      .create(AnimationKey("nerve_body"), Cycle.create("idle", generateBodyFrame(3, 7)))
  }

  object Parabite {
    /*Enemy body*/
    val offset                    = 309
    val bodyWidth: Int            = 26
    val bodyHeight: Int           = 32
    val hideFrameTime: Double     = 0.180
    val hideTime: Double          = hideFrameTime * 3
    val hideFrameTimeMillis: Long = (hideFrameTime * 1000).toLong

    def generateHidingFrame: NonEmptyList[Frame] =
      generateFramesList(
        List(
          Frame(Rectangle(offset + 32, 0, bodyWidth, bodyHeight), Millis(hideFrameTimeMillis)),
          Frame(Rectangle(offset + 64, 0, bodyWidth, bodyHeight), Millis(hideFrameTimeMillis)),
          Frame(Rectangle(offset + 0, 64, bodyWidth, bodyHeight - 1), Millis(hideFrameTimeMillis))
        )
      )

    def generateWakeupFrame: NonEmptyList[Frame] = generateHidingFrame.reverse

    def generateWalkingFrame(x: Int, y: Int): NonEmptyList[Frame] =
      generateFramesList(
        generateFrame(0 until 2)(i =>
          Frame(Rectangle(offset + x + (i * (bodyWidth + 6)), y, bodyWidth, bodyHeight), Millis(80))
        )
      )

    def generateIdleFrame(x: Int, y: Int): NonEmptyList[Frame] =
      generateFramesList(
        generateFrame(0 until 2)(i => Frame(Rectangle(x + (i * (bodyWidth + 6)), y, bodyWidth, bodyHeight), Millis(80)))
      )

    val body: Animation = Animation
      .create(AnimationKey("parabite_body"), Cycle.create("walking", generateWalkingFrame(0, 34)))
      .addCycle(Cycle.create("hiding", generateHidingFrame))
      .addCycle(Cycle.create("wakeup", generateWakeupFrame))
      .addCycle(Cycle.create("idle", generateIdleFrame(0, 0)))
  }

  object Explosion {

    val size: Int = 65

    def generateBodyFrame: NonEmptyList[Frame] =
      val frames = for {
        i <- Range(0, 6)
        j <- Range(0, 6)
      } yield Frame(Rectangle(size * j, size * i, size, size), Millis(10))
      generateFramesList(frames.toList)

    val body: Animation = Animation
      .create(AnimationKey("explosion_animation"), Cycle.create("idle", generateBodyFrame))
  }

}
