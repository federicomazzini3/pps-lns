package lns.core.anythingAssets

import indigo.*
import indigo.shared.scenegraph.Shape
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.core.Assets

import scala.language.implicitConversions

given Conversion[Double, Int] with
  def apply(v: Double): Int = v.toInt

/**
 * AnythingAsset trait from which all the assets of the "anything" elements can be extends
 */
trait AnythingAsset {
  val name: Option[String]
  val width: Int
  val height: Int
  val offsetY: Int
  val scale: Double

  def asset: AssetName = name match {
    case Some(n) => AssetName(n)
    case _       => throw new Exception("no asset name")
  }

  /**
   * Bounding Box
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
   * @return
   *   offset scaled
   */
  def shotAreaOffset: Int = withScale(-offsetY)

  /**
   * Method to scale the size of the assets in proportion to the room
   * @param f
   *   function from Double to Double
   * @return
   *   size of the scaled asset
   */
  def withScale: Double => Double = (size: Double) => size * scale

  /**
   * Method to screate default Sprite animation
   * @param name
   *   the name of animation
   * @return
   *   Sprite
   */
  def spriteAnimation(name: String): Sprite[Material.Bitmap] =
    Sprite(BindingKey(name + "_sprite"), 0, 0, 1, AnimationKey(name), Material.Bitmap(asset))

  /**
   * Standard Shape to view on screen boundingBox
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
   * @param components
   *   A List of SceneNode
   * @return
   *   Group
   */
  def drawComponents(components: List[SceneNode]): Group =
    Group()
      // .addChild(boudingView)
      .addChild(
        Group()
          .withRef(0, offsetY)
          .addChildren(components)
      )
      .withScale(Vector2(scale, scale))
}

/**
 * ShotAsset trait from which all shots can be extends
 */
trait ShotAsset extends AnythingAsset {
  override val name: Option[String] = None
  override val width: Int           = 40
  override val height: Int          = 40
  override val offsetY: Int         = 0
  override val scale: Double        = 1

  def drawShot: Shape
}

/**
 * All AnythingAsset assets for loading: Characters, Enemies, Elements and Items
 */
object AnythingAsset {
  val assets: Set[AssetType] = Characters.assets ++ Enemies.assets ++ Elements.assets ++ Items.assets
}
