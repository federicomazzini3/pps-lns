package lns.scenes.game.map

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import indigo.shared.datatypes.RGBA
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.dungeon.Position
import lns.scenes.game.dungeon.DungeonModel
import lns.scenes.game.room.*

/**
 * Displays a dungeon mini-map at left-bottom corner of the screen
 */
object MapView {

  var horizontalCells = 14
  var verticalCells   = 14
  val gap             = 2
  val roomSize        = 20
  val cellSize        = gap + roomSize

  /**
   * Displays the map by using the screen size, the dungeon data and the current room
   * @param context
   *   indigo frame context data
   * @param dungeon
   *   [[DungeonModel]]
   * @param currentRoom
   *   current room [[Position]]
   * @return
   *   indigo Group with each room as a Shape element
   */
  def draw(context: FrameContext[StartupData], dungeon: DungeonModel, currentRoom: Position): Group =
    Group()
      .addChildren(drawDungeon(dungeon, currentRoom))
      .withRef(0, verticalCells * cellSize)
      .moveTo(context.startUpData.screenDimensions.bottomLeft)

  /**
   * get room map color
   * @param currentRoom
   *   current room [[Position]], where the character actually is playing
   * @param room
   *   the [[RoomModel]] for which get a color
   * @return
   *   RGBA color to display
   */
  def roomColor(currentRoom: Position)(room: RoomModel): RGBA = room match {
    case r if r.positionInDungeon == currentRoom => RGBA.Cyan
    case r: BossRoom                             => RGBA.Magenta
    case r: ItemRoom                             => RGBA.Yellow
    case _                                       => RGBA.White
  }

  /**
   * draws the room as a box filled with a color
   * @param color
   *   RGBA
   * @return
   *   Shape.Box filled with color
   */
  def drawRoom(color: RGBA): Shape.Box = Shape.Box(
    Rectangle(Point(0, 0), Size(roomSize, roomSize)),
    Fill.Color(color)
  )

  /**
   * Draws a grid of rooms centered on current room
   * @param dungeon
   *   [[DungeonModel]] data
   * @param currentRoom
   *   current room [[Position]], where the character actually is playing
   * @return
   *   a List of Box, each one representing a room
   */
  def drawDungeon(dungeon: DungeonModel, currentRoom: Position): List[Shape.Box] =
    for {
      x    <- (0 to horizontalCells).toList
      y    <- (0 to verticalCells).toList
      room <- dungeon.room((currentRoom._1 + x - horizontalCells / 2, currentRoom._2 - (y - verticalCells / 2)))
      color = roomColor(currentRoom)(room)
    } yield drawRoom(color).moveTo(x * cellSize, y * cellSize)

}
