package lns.scenes.game.dungeon

import indigo.*
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.scenes.game.anything.{ AnythingId, AnythingModel }
import lns.scenes.game.enemy.boney.BoneyModel
import lns.scenes.game.enemy.mask.MaskModel
import lns.scenes.game.enemy.nerve.NerveModel
import lns.scenes.game.enemy.parabite.ParabiteModel
import lns.scenes.game.room.RoomModel
import lns.scenes.game.room.door.{ DoorState, Location }
import lns.scenes.game.stats.*
import lns.scenes.game.element.ElementModel
import lns.scenes.game.items.ItemModel
import lns.subsystems.prolog.{ Substitution, Term }
import lns.subsystems.prolog.Term.*

import scala.collection.immutable.HashMap

object Generator {

  def apply(grid: BasicGrid): DungeonModel =
    DungeonModel(
      grid.content
        .map((position, roomType) => (position, generateRoom(grid, position, roomType))),
      grid.initialRoom
    )

  def generateRoom(grid: BasicGrid, position: Position, roomType: RoomType): RoomModel =
    roomType match {
      case RoomType.Item =>
        RoomModel.itemRoom(position, generateDoors(grid, position), generateItems())
      case RoomType.Arena =>
        val enemyTest  = MaskModel.initial
        val enemyTest2 = MaskModel.initial2
        RoomModel.arenaRoom(
          position,
          generateDoors(grid, position),
          Map(enemyTest.id -> enemyTest) ++ generateBlockingElements() + (enemyTest2.id -> enemyTest2)
        )
      case RoomType.Boss =>
        RoomModel.bossRoom(position, generateDoors(grid, position), generateBlockingElements())
      case RoomType.Start => // TODO da rimuovere
        RoomModel.itemRoom(position, generateDoors(grid, position), generateItems())
      case _ => RoomModel.emptyRoom(position, generateDoors(grid, position))

    }

  def generateDoors(grid: BasicGrid, position: Position): Set[Location] =
    Set(Location.Left, Location.Right, Location.Above, Location.Below)
      .filter(location => Grid.near(grid)(position)(location).isDefined)

  def generateBlockingElements(): Map[AnythingId, AnythingModel] = ElementModel.stones()

  def generateItems(): Map[AnythingId, AnythingModel] = ItemModel.all

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
