package lns.core

import indigo.*
import lns.scenes.game.anything.{ DynamicState, FireState }

object Animations {

  def apply(): List[Animation]                                = List(Character.head, Character.body)
  def generateFrame(r: Range)(f: Int => Frame): List[Frame]   = r.toList.map(f(_))
  def generateFramesList(l: List[Frame]): NonEmptyList[Frame] = NonEmptyList.fromList(l).get

  object Character {
    /*Character head*/
    val headWidth: Int  = 28
    val headHeight: Int = 25

    def generateHeadFrame(x: Int, y: Int): NonEmptyList[Frame] =
      generateFramesList(
        List(
          Frame(Rectangle(x + 40, y, headWidth, headHeight), Millis(250)),
          Frame(Rectangle(x, y, headWidth, headHeight), Millis(250))
        )
      )

    val head: Animation = Animation
      .create(AnimationKey("character_head"), Cycle.create("down_shot", generateHeadFrame(10, 25)))
      .addCycle(Cycle.create("right_shot", generateHeadFrame(90, 25)))
      .addCycle(Cycle.create("up_shot", generateHeadFrame(170, 25)))
      .addCycle(Cycle.create("left_shot", generateHeadFrame(250, 25)))

    def headCrop(state: FireState | DynamicState, eyesOpen: Boolean): Rectangle = state match {
      case FireState.FIRE_LEFT | DynamicState.MOVE_LEFT if eyesOpen =>
        Rectangle(250, 25, Character.headWidth, Character.headHeight)
      case FireState.FIRE_LEFT | DynamicState.MOVE_LEFT =>
        Rectangle(290, 25, Character.headWidth, Character.headHeight)
      case FireState.FIRE_RIGHT | DynamicState.MOVE_RIGHT if eyesOpen =>
        Rectangle(90, 25, Character.headWidth, Character.headHeight)
      case FireState.FIRE_RIGHT | DynamicState.MOVE_RIGHT =>
        Rectangle(130, 25, Character.headWidth, Character.headHeight)
      case FireState.FIRE_UP | DynamicState.MOVE_UP if eyesOpen =>
        Rectangle(170, 25, Character.headWidth, Character.headHeight)
      case FireState.FIRE_UP | DynamicState.MOVE_UP =>
        Rectangle(210, 25, Character.headWidth, Character.headHeight)
      case _ if eyesOpen =>
        Rectangle(10, 25, Character.headWidth, Character.headHeight)
      case _ =>
        Rectangle(50, 25, Character.headWidth, Character.headHeight)
    }

    /*Character body*/
    val bodyWidth: Int  = 18
    val bodyHeight: Int = 15

    def generateBodyFrame(x: Int, y: Int): NonEmptyList[Frame] =
      generateFramesList(
        generateFrame(0 until 10)(i => Frame(Rectangle(x + (i * 32), y, bodyWidth, bodyHeight), Millis(80)))
      )

    val body: Animation = Animation
      .create(AnimationKey("character_body"), Cycle.create("walking_left_right", generateBodyFrame(15, 123)))
      .addCycle(Cycle.create("walking_up_down", generateBodyFrame(15, 80)))
  }
}
