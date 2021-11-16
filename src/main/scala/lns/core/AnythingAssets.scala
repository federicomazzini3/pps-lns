package lns.core

import scala.language.implicitConversions
import indigo.*
import indigo.shared.scenegraph.Shape
import indigoextras.geometry.{ BoundingBox, Vertex }

given Conversion[Double, Int] with
  def apply(v: Double): Int = v.toInt

trait AnythingAsset {
  val name: String
  val width: Int
  val height: Int
  val offsetY: Int
  val scale: Double

  def boundingBox(position: Vertex): BoundingBox =
    BoundingBox(
      position,
      Vertex(withScale(width), withScale(height - offsetY))
    )
  def initialBoundingBox: BoundingBox = boundingBox(Vertex(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2))

  def shotAreaOffset: Int = withScale(-offsetY)

  def withScale: Double => Double = (size: Double) => size * scale

  def asset: AssetName = AssetName(name)

  def boudingModel: Shape =
    Shape.Box(
      Rectangle(Point(0, 0), Size(width, height - offsetY)),
      Fill.Color(RGBA(1, 1, 1, 0.5))
    )

  def spriteAnimation(name: String): Sprite[Material.Bitmap] =
    Sprite(BindingKey(name + "_sprite"), 0, 0, 1, AnimationKey(name), Material.Bitmap(asset))

  def shadowModel: Shape = Shape
    .Circle(
      center = Point(width / 2, height + width / 4),
      radius = width / 3,
      Fill.Color(RGBA(0, 0, 0, 0.4))
    )
    .scaleBy(1, 0.25)

  def drawComponents(components: List[SceneNode]): Group =
    Group()
      //.addChild(boundingModel)
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

object AnythingAssets {
  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("character"), AssetPath(Assets.baseUrl + "characters/isaac.png")),
    AssetType.Image(AssetName("boney"), AssetPath(Assets.baseUrl + "enemies/boney.png")),
    AssetType.Image(AssetName("mask"), AssetPath(Assets.baseUrl + "enemies/mask.png")),
    AssetType.Image(AssetName("nerve"), AssetPath(Assets.baseUrl + "enemies/nerve.png")),
    AssetType.Image(AssetName("parabite"), AssetPath(Assets.baseUrl + "enemies/parabite.png"))
  )

  val character = new CharacterAsset()
  val boney     = new BoneyAsset()
  val mask      = new MaskAsset()
  val nerve     = new NerveAsset()
  val parabite  = new ParabiteAsset()
}
