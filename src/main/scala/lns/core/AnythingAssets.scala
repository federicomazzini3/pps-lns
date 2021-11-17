package lns.core

import scala.language.implicitConversions
import indigo.*
import indigo.shared.scenegraph.Shape
import indigoextras.geometry.{ BoundingBox, Vertex }

given Conversion[Double, Int] with
  def apply(v: Double): Int = v.toInt

/**
 * AnythingAsset trait from which all the assets of the "anything" elements can be extends
 */
trait AnythingAsset {
  val name: String
  val width: Int
  val height: Int
  val offsetY: Int
  val scale: Double

  def asset: AssetName = AssetName(name)

  /**
   * Bounding Box
   *
   * @param position
   *   the postiion Vertex of boundign box
   * @return
   *   BoundingBox
   */
  def boundingBox(position: Vertex): BoundingBox =
    BoundingBox(
      position,
      Vertex(withScale(width), withScale(height - offsetY))
    )

  /**
   * Offset to define the portion of the asset that can be hit by the shots, different from the bounding box to manage
   * collisions
   *
   * @return
   *   offset scaled
   */
  def shotAreaOffset: Int = withScale(-offsetY)

  /**
   * Method to scale the size of the assets in proportion to the room
   *
   * @param f
   *   function from Double to Double
   * @return
   *   size of the scaled asset
   */
  def withScale: Double => Double = (size: Double) => size * scale

  /**
   * Method to screate default Sprite animation
   *
   * @param name
   *   the name of animation
   * @return
   *   Sprite
   */
  def spriteAnimation(name: String): Sprite[Material.Bitmap] =
    Sprite(BindingKey(name + "_sprite"), 0, 0, 1, AnimationKey(name), Material.Bitmap(asset))

  /**
   * Standard Shape to view on screen boundingBox
   *
   * @return
   *   Shape
   */
  def boudingView: Shape =
    Shape.Box(
      Rectangle(Point(0, 0), Size(width, height - offsetY)),
      Fill.Color(RGBA(1, 1, 1, 0.5))
    )

  /**
   * Standard shape to view on screen shadow
   *
   * @return
   *   Shape
   */
  def shadowView: Shape = Shape
    .Circle(
      center = Point(width / 2, height + width / 4),
      radius = width / 3,
      Fill.Color(RGBA(0, 0, 0, 0.4))
    )
    .scaleBy(1, 0.25)

  /**
   * Method to draw on the screen the sprite composed of all its parts with the correct scaling, some by default, the
   * others passed as parameters and customizable by the inheriting class
   *
   * @param components
   *   A List of SceneNode
   * @return
   *   Group
   */
  def drawComponents(components: List[SceneNode]): Group =
    Group()
      //.addChild(boudingView)
      .addChild(
        Group()
          .withRef(0, offsetY)
          .addChildren(components)
      )
      .withScale(Vector2(scale, scale))
}

class CharacterAsset extends AnythingAsset {
  override val name: String  = "character"
  override val width: Int    = 28
  override val height: Int   = 33
  override val offsetY: Int  = 18
  override val scale: Double = 5
}

class BoneyAsset extends AnythingAsset {
  override val name: String  = "boney"
  override val width: Int    = 28
  override val height: Int   = 33
  override val offsetY: Int  = 13
  override val scale: Double = 5
}

class MaskAsset extends AnythingAsset {
  override val name: String  = "mask"
  override val width: Int    = 28
  override val height: Int   = 33
  override val offsetY: Int  = 0
  override val scale: Double = 5
}

class NerveAsset extends AnythingAsset {
  override val name: String  = "nerve"
  override val width: Int    = 26
  override val height: Int   = 50
  override val offsetY: Int  = 30
  override val scale: Double = 5
}

class ParabiteAsset extends AnythingAsset {
  override val name: String  = "parabite"
  override val width: Int    = 26
  override val height: Int   = 33
  override val offsetY: Int  = 13
  override val scale: Double = 5
}

class StoneAsset extends AnythingAsset {
  override val name: String  = "stone"
  override val width: Int    = 145
  override val height: Int   = 180 //original dimension 128
  override val offsetY: Int  = 35
  override val scale: Double = 1.048
}

class AltarAsset extends AnythingAsset {
  override val name: String  = "altar"
  override val width: Int    = 27
  override val height: Int   = 23
  override val offsetY: Int  = 0
  override val scale: Double = 5
}

class ArrowAsset extends AnythingAsset {
  override val name: String  = "arrow"
  override val width: Int    = 32
  override val height: Int   = 32
  override val offsetY: Int  = 0
  override val scale: Double = 5
}

object AnythingAssets {
  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("character"), AssetPath(Assets.baseUrl + "characters/isaac.png")),
    AssetType.Image(AssetName("boney"), AssetPath(Assets.baseUrl + "enemies/boney.png")),
    AssetType.Image(AssetName("mask"), AssetPath(Assets.baseUrl + "enemies/mask.png")),
    AssetType.Image(AssetName("nerve"), AssetPath(Assets.baseUrl + "enemies/nerve.png")),
    AssetType.Image(AssetName("parabite"), AssetPath(Assets.baseUrl + "enemies/parabite.png")),
    AssetType.Image(AssetName("stone"), AssetPath(Assets.baseUrl + "elements/stone.png")),
    AssetType.Image(AssetName("altar"), AssetPath(Assets.baseUrl + "items/altar.png")),
    AssetType.Image(AssetName("arrow"), AssetPath(Assets.baseUrl + "items/arrow.png"))
  )
}
