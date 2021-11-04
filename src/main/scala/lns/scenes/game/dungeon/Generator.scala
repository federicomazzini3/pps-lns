package lns.scenes.game.dungeon

import indigo.shared.datatypes.Rectangle
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.enemy.boney.BoneyModel
import lns.scenes.game.enemy.mask.MaskModel
import lns.scenes.game.enemy.nerve.NerveModel
import lns.scenes.game.room.RoomModel
import lns.scenes.game.room.door.{ DoorState, Location }
import lns.subsystems.prolog.{ Atom, Compound, Num, Substitution, Term }
import lns.scenes.game.stats.*

import scala.collection.immutable.HashMap
import lns.scenes.game.room.door.{ DoorState, Location }

object Generator {

  def apply(grid: BasicGrid): DungeonModel =
    DungeonModel(
      grid.content
        .map((position, roomType) => (position, generateRoom(grid, position, roomType)))
    )

  def generateRoom(grid: BasicGrid, position: Position, roomType: RoomType): RoomModel =
    roomType match {
      case RoomType.Empty =>
        RoomModel.emptyRoom(position, generateDoors(grid, position))
      case RoomType.Item =>
        RoomModel.itemRoom(position, generateDoors(grid, position), Set.empty[AnythingModel])
      case RoomType.Arena =>
        val enemyTest = NerveModel.initial
        RoomModel.arenaRoom(position, generateDoors(grid, position), Set(enemyTest)) // Set.empty[AnythingModel]
      case RoomType.Boss =>
        RoomModel.bossRoom(position, generateDoors(grid, position), Set.empty[AnythingModel])
    }

  def generateDoors(grid: BasicGrid, position: Position): Set[Location] =
    Set(Location.Left, Location.Right, Location.Above, Location.Below)
      .filter(location => Grid.near(grid)(position)(location).isDefined)

  /**
   * Generates the dungeon from Prolog Substitution which contains a Term "L" that represents a list of room(x,y,type)
   * @param sub
   * @return
   */
  def getDungeon(sub: Substitution) = getRooms(sub.links("L"))

  private def getRoomType(roomType: String): RoomType = roomType match {
    case "s" => RoomType.Empty
    case "a" => RoomType.Arena
    case "i" => RoomType.Item
    case "e" => RoomType.Empty
    case "b" => RoomType.Boss
    case _   => RoomType.Empty
  }

  private def getRooms(term: Term): Map[Position, RoomType] =
    term match {
      case Atom("[]") => HashMap[Position, RoomType]()
      case Compound(a, Compound(Atom("room"), Num(x, false), Num(y, false), Atom(roomType)), next) =>
        getRooms(next) + ((x.toInt, y.toInt) -> getRoomType(roomType))
      case _ => HashMap[Position, RoomType]()
    }
}
