package lns.core.anythingAssets

import indigo.*
import lns.core.Assets

import scala.language.implicitConversions

/**
 * Basic trait for the Item asset
 */
trait ItemAsset extends AnythingAsset {
  override val name: Option[String] = None
  override val width: Int           = Assets.Rooms.cellSize
  override val height: Int          = Assets.Rooms.cellSize * 1.6
  override val offsetY: Int         = Assets.Rooms.cellSize * 0.6
  override val scale: Double        = 1

  val altarWidth: Int    = 32
  val altarHeight: Int   = 32
  val altarScale: Double = Assets.Rooms.cellSize.toDouble / 32

  val itemWidth: Int    = 32
  val itemHeight: Int   = 32
  val itemScale: Double = Assets.Rooms.cellSize.toDouble / 32

  /**
   * Draw item asset over altar
   * @param name
   *   item name
   * @return
   *   Group
   */
  def drawItemOnAltar(name: String): Group =
    drawComponents(List(altarView, itemView(name)))

  /**
   * Draw only altar when once item is picked
   * @param name
   *   item name
   * @return
   *   Group
   */
  def drawOnlyAltar: Group =
    drawComponents(List(altarView))

  def altarView: Graphic[Material.Bitmap] =
    Graphic(
      Rectangle(0, 0, altarWidth, altarHeight),
      1,
      Material.Bitmap(AssetName("altar"))
    ).moveTo(0, Assets.Rooms.cellSize * 0.6)
      .withScale(Vector2(altarScale, altarScale))

  def itemView(name: String): Graphic[Material.Bitmap] =
    Graphic(
      Rectangle(0, 0, itemWidth, itemHeight),
      1,
      Material.Bitmap(AssetName(name))
    ).withScale(Vector2(itemScale, itemScale))
}

/**
 * Items assets for loading
 */
object Items {
  val all: Set[String] =
    Set("arrow", "drop", "eye", "fireball", "glasses", "heart", "juice", "mushroom", "syringe", "tail")

  val assets: Set[AssetType] =
    all.map(v => AssetType.Image(AssetName(v), AssetPath(Assets.baseUrl + "items/" + v + ".png"))) +
      AssetType.Image(AssetName("altar"), AssetPath(Assets.baseUrl + "items/altar.png"))
}
