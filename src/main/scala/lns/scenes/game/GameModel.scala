package lns.scenes.game

import indigo.*
import indigo.shared.FrameContext
import indigoextras.geometry.Vertex
import lns.StartupData
import lns.core.PrologClient
import lns.scenes.game.*
import anything.*
import characters.*
import dungeon.*
import dungeon.RoomType.*
import room.*
import shots.*
import collisions.*
import collisions.CollisionUpdater.*
import collisions.PassageUpdater.*

sealed trait GameModel

object GameModel {

  /**
   * The game not started yet
   * @param prologClient
   */
  case class NotStarted(val prologClient: PrologClient) extends GameModel

  /**
   * The game started
   * @param dungeon
   *   the map that contains all room and their disposition
   * @param currentRoomPosition
   *   the current room where the character plays
   * @param character
   *   the character controlled by the player
   */
  case class Started(
      val prologClient: PrologClient,
      val dungeon: DungeonModel,
      val currentRoomPosition: Position,
      val character: CharacterModel
  ) extends GameModel {

    /**
     * the extended anything collection with the current room's anything and the character
     */
    val allAnythings: Map[AnythingId, AnythingModel] =
      currentRoom.anythings + (character.id -> character)

    /**
     * change the current room displayed by the game
     * @param newCurrentRoom
     *   the new room position
     * @return
     *   a new Game Model with current room updated
     */
    def changeCurrentRoom(newCurrentRoom: Position): Started =
      this.copy(currentRoomPosition = newCurrentRoom)

    /**
     * @return
     *   the RoomModel of the current room
     */
    def currentRoom: RoomModel = dungeon.room(currentRoomPosition).get

    /**
     * Update the GameModel with an updated current RoomModel
     * @param f
     *   function that set the strategy to update the room
     * @return
     *   a new GameModel with an updated current Room
     */
    def updateCurrentRoom(f: RoomModel => Outcome[RoomModel]): Outcome[Started] =
      updateRoom(currentRoomPosition)(f)

    /**
     * Update the GameModel with an updated current RoomModel
     * @param position
     *   dungeon position of the room
     * @param f
     *   function that set the strategy to update the room
     * @return
     *   a new GameModel with an updated Room
     */
    def updateRoom(position: Position)(f: RoomModel => Outcome[RoomModel]): Outcome[Started] =
      dungeon.room(position) match {
        case Some(room) =>
          for (updatedRoom <- f(room))
            yield this.copy(dungeon = dungeon.updateRoom(position)(updatedRoom), prologClient = PrologClient())
        case _ => Outcome(this)
      }

    /**
     * Update the GameModel with an updated Character
     * @param f
     *   function that set the strategy to update the room
     * @return
     *   a new GameModel with an updated Character
     */
    def updateCharacter(f: CharacterModel => Outcome[CharacterModel]): Outcome[Started] =
      for (updatedCharacter <- f(character))
        yield this.copy(character = updatedCharacter)

    /**
     * Update the GameModel with an updated collection of anything
     * @param f
     *   function that set the strategy to update anythings
     * @return
     *   a new GameModel with an updated collection of anythings
     */
    def updateEachAnythings(f: AnythingModel => Outcome[AnythingModel]): Outcome[GameModel.Started] =
      updateCurrentRoom(room => room.updateEachAnything(f))

    /**
     * Update the GameModel with an updated collection of anything
     * @param f
     *   function that set the strategy to update anythings
     * @return
     *   a new GameModel with an updated collection of anythings
     */
    def updateAnythings(
        f: Map[AnythingId, AnythingModel] => Outcome[Map[AnythingId, AnythingModel]]
    ): Outcome[GameModel.Started] =
      updateCurrentRoom(room => room.updateAnythings(f))

    /**
     * Update the GameModel with a new current Room when the character pass through a door
     * @return
     *   a new GameModel with an updated current Room and an updated Character
     */
    def updateWithPassage: Outcome[Started] =
      val (newRoom, movedCharacter) = PassageUpdater(dungeon, currentRoom, character)
      this
        .changeCurrentRoom(newRoom)
        .updateCharacter(character => Outcome(movedCharacter))

    /**
     * Update stats of anythings when they collide each other
     * @param context
     * @return
     *   a new GameModel with an updated collection of anythings
     */
    def updateStatsAfterCollision(context: FrameContext[StartupData]): Outcome[GameModel.Started] =
      for {
        modelWithUpdatedCharacter <- this
          .updateCharacter(character =>
            CollisionUpdater(character)(this.currentRoom.anythings)(updateStats(context))
              .map(c => c.asInstanceOf[CharacterModel])
          )
        modelWithUpdatedAnything <- modelWithUpdatedCharacter.updateEachAnythings(anything =>
          for (a <- CollisionUpdater(anything)(this.allAnythings)(updateStats(context)))
            yield a
        )
        modelWithoutDeadAnythings <- modelWithUpdatedAnything.updateAnythings(anythings =>
          Outcome(anythings.filter {
            case (id, elem: AliveModel) if elem.life <= 0 => false
            case _                                        => true
          })
        )
      } yield modelWithoutDeadAnythings

    /**
     * Update position of anythings when they collide each other
     * @return
     *   a new GameModel with an updated collection of anythings
     */
    def updateMovementAfterCollision: Outcome[GameModel.Started] =
      for {
        modelWithUpdatedCharacter <- this
          .updateCharacter(character =>
            CollisionUpdater(character)(this.currentRoom.anythings)(updateMove)
              .map(c => c.asInstanceOf[CharacterModel])
          )
        modelWithUpdatedAnythings <- modelWithUpdatedCharacter.updateEachAnythings(anything =>
          CollisionUpdater(anything)(this.allAnythings)(updateMove)
        )
      } yield modelWithUpdatedAnythings
  }

  def initial: GameModel = NotStarted(PrologClient())

  def start(startupData: StartupData, rooms: Map[Position, RoomType]): GameModel =
    /** Generation here */
    val dungeonModel: DungeonModel =
      Generator(
        BasicGrid(
          rooms
        )
      )

    Started(
      PrologClient(),
      dungeonModel,
      dungeonModel.initialRoom,
      CharacterModel.initial
    )
}
