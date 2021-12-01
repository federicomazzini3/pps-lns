package lns.scenes.game.dungeon

import indigo.*
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.anything.{ AnythingId, AnythingModel }
import lns.scenes.game.bosses.BossModel
import lns.scenes.game.enemies.boney.BoneyModel
import lns.scenes.game.enemies.mask.MaskModel
import lns.scenes.game.enemies.nerve.NerveModel
import lns.scenes.game.enemies.parabite.ParabiteModel
import lns.scenes.game.room.{ ArenaRoom, BossRoom, Cell, Floor, RoomModel }
import lns.scenes.game.room.door.{ DoorState, Location }
import lns.scenes.game.stats.*
import lns.scenes.game.elements.ElementModel
import lns.scenes.game.enemies.EnemyModel
import lns.scenes.game.items.ItemModel
import lns.subsystems.prolog.{ Substitution, Term }
import lns.subsystems.prolog.Term.*

import scala.collection.immutable.HashMap
import scala.util.Random

object Generator {

  def apply(grid: BasicGrid): DungeonModel =
    DungeonModel(
      grid.content
        .map((position, roomType) => (position, generateRoom(grid, position, roomType))),
      grid.initialRoom
    )

  def generateRoom(grid: BasicGrid, positionInGrid: Position, roomType: RoomType): RoomModel =
    roomType match {
      case RoomType.Item =>
        RoomModel.itemRoom(positionInGrid, generateDoors(grid, positionInGrid), generateItem(Floor.center))
      case RoomType.Arena =>
        val doorsLocations = generateDoors(grid, positionInGrid)
        RoomModel.arenaRoom(
          positionInGrid,
          doorsLocations,
          Map.empty
        )
      case RoomType.Boss =>
        val boss = BossModel.initial
        RoomModel.bossRoom(
          positionInGrid,
          generateDoors(grid, positionInGrid),
          Map(boss.id -> boss)
        )
      case _ =>
        RoomModel.emptyRoom(positionInGrid, generateDoors(grid, positionInGrid))
    }

  def generateDoors(grid: BasicGrid, position: Position): Set[Location] =
    Set(Location.Left, Location.Right, Location.Above, Location.Below)
      .filter(location => Grid.near(grid)(position)(location).isDefined)

  def generateElementsFromProlog(substitution: Substitution, room: RoomModel): Map[AnythingId, AnythingModel] =
    room match {
      case a: ArenaRoom =>
        generateEnemies(GeneratorHelper.fromPrologList(substitution.links("A").toString)) ++
          generateBlockingElements(GeneratorHelper.fromPrologList(substitution.links("S").toString))
      case b: BossRoom =>
        generateBlockingElements(GeneratorHelper.fromPrologList(substitution.links("S").toString))
      case _ => Map.empty
    }

  def generateBlockingElements(cells: Seq[Cell]): Map[AnythingId, AnythingModel] =
    ElementModel.random(cells)

  def generateEnemies(cells: Seq[Cell]): Map[AnythingId, AnythingModel] =
    EnemyModel.random(cells)

  def generateItem(position: Cell): Map[AnythingId, AnythingModel] =
    ItemModel.random(position)

  /**
   * Generates the dungeon from Prolog Substitution which contains a Term "L" that represents a list of room(x,y,type)
   * @param sub
   *   the prolog service [[Substitution]] produced
   * @return
   *   a Map of [[Position]] -> [[RoomType]]
   */
  def getDungeon(sub: Substitution): Map[Position, RoomType] = getRooms(sub.links("L"))

  private def getRoomType(roomType: String): RoomType = roomType match {
    case "s" => RoomType.Start
    case "a" => RoomType.Arena
    case "i" => RoomType.Item
    case "b" => RoomType.Boss
    case _   => RoomType.Empty
  }

  private def getRooms(terms: List[Term]): Map[Position, RoomType] = terms.collect {
    case Struct(Atom("room"), Num(x, false), Num(y, false), Atom(roomType)) =>
      (x.toInt, y.toInt) -> getRoomType(roomType)
  }.toMap

}
