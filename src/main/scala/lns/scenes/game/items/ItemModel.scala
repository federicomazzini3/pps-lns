package lns.scenes.game.items

import indigo.*
import indigo.shared.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets.Rooms
import lns.core.Macros.copyMacro
import lns.core.anythingAssets.Items
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ AnythingId, AnythingModel, SolidModel }
import lns.scenes.game.stats.*

import scala.language.implicitConversions

/**
 * Item model that is solid and defines the stat variations to attribute to the character when he collects it
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param id
 *   [[AnythingId]] The unique identifier of the Anything instance.
 * @param view
 *   [[AnythingView]] The Anything's View factory function.
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param Name
 *   string identifying the item
 * @param stats
 *   Item [[Stats]] to be summed to the character's stats
 * @param shotAreaOffset
 *   shotAreaOffset
 * @param crossable
 *   crossable, default false
 */
case class ItemModel(
    id: AnythingId,
    view: () => ItemView[_],
    boundingBox: BoundingBox,
    name: String,
    stats: Stats,
    shotAreaOffset: Int = 0,
    crossable: Boolean = false
) extends SolidModel {

  type Model = ItemModel

  def withSolid(crossable: Boolean): Model = copyMacro
}

/**
 * Factory of [[ItemModel]]
 */
object ItemModel {

  /**
   * Create [[ItemModel]] by item name
   * @param name
   *   String of item name
   * @return
   *   Map[AnythingId, AnythingModel]
   */
  def create(name: String): Map[AnythingId, AnythingModel] =
    val item = ItemModel(
      id = AnythingId.generate,
      view = () => ItemView,
      boundingBox = ItemView.boundingBox(Vertex(Rooms.floorSize / 6, Rooms.floorSize / 6)),
      name = name,
      stats = Stats.item(name)
    )
    Map(item.id -> item)

  /**
   * Create all [[ItemModel]] available
   * @return
   *   Map[AnythingId, AnythingModel]
   */
  def all: Map[AnythingId, AnythingModel] =
    val itemsXrow = 5
    val items = for {
      (name, count) <- Items.all.zipWithIndex
      x = count match {
        case x if x >= itemsXrow => count * 2 - itemsXrow * 2
        case _                   => count * 2
      }
      y = count match {
        case x if x >= itemsXrow => 6
        case _                   => 2
      }
      id = AnythingId.generate
    } yield id -> ItemModel(
      id = id,
      view = () => ItemView,
      boundingBox = ItemView.boundingBox(Vertex(Rooms.cellSize * x, Rooms.cellSize * y)),
      name = name,
      stats = Stats.item(name)
    )

    items.toMap
}
