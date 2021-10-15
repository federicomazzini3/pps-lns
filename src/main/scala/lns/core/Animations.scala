package lns.core

import indigo.*

/*
trait AnimationRect {
  val x: Int
  val y: Int
  val width: Int
  val height: Int
}

abstract class AnimationCycle{
  val rect: AnimationRect
  val time: Millis

  def generateFrame: Int => Frame
  def get(range: Range): NonEmptyList[Frame] =
    NonEmptyList.fromList(range.toList.map(generateFrame(_))).get
}

object AnimationCycle {
  def apply(x: Int, y: Int, width: Int, height: Int, time: Millis)(f: Int => Frame): AnimationCycle =
}
 */

object Animations {
  def apply(): List[Animation]                                = List(Character.head, Character.body)
  def generateFrame(r: Range)(f: Int => Frame): List[Frame]   = r.toList.map(f(_))
  def generateFramesList(l: List[Frame]): NonEmptyList[Frame] = NonEmptyList.fromList(l).get

  object Character {
    /*Character head*/
    val headWidth: Int  = 18
    val headHeight: Int = 15

    def generateHeadFrame(x: Int, y: Int, width: Int, height: Int, time: Millis): NonEmptyList[Frame] =
      generateFramesList(generateFrame(0 until 2)(i => Frame(Rectangle(x + (i * 40), y, width, height), time)))
    val head: Animation = Animation
      .create(
        AnimationKey("character_head"),
        Cycle
          .create(
            "open_close_eyes",
            generateHeadFrame(10, 25, 28, 25, Millis(500))
          )
      )

    /*Character body*/
    val bodyWidth: Int  = 18
    val bodyHeight: Int = 15

    def generateBodyFrame(x: Int, y: Int, width: Int, height: Int, time: Millis): NonEmptyList[Frame] =
      generateFramesList(generateFrame(0 until 10)(i => Frame(Rectangle(x + (i * 32), y, width, height), time)))

    val body: Animation = Animation
      .create(
        AnimationKey("character_body"),
        Cycle
          .create(
            "walking_left_right",
            generateBodyFrame(15, 123, bodyWidth, bodyHeight, Millis(80))
          )
      )
      .addCycle(
        Cycle.create(
          "walking_up_down",
          generateBodyFrame(15, 80, bodyWidth, bodyHeight, Millis(80))
        )
      )
  }
}
