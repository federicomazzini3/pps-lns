package lns.scenes.game.collisions

import indigo.shared.{ FrameContext, Outcome }
import lns.StartupData
import lns.scenes.game.anything.{ AliveModel, SolidModel, * }
import lns.scenes.game.characters.CharacterModel
import lns.scenes.game.enemies.EnemyModel
import lns.scenes.game.items.ItemModel
import lns.scenes.game.room.*
import lns.scenes.game.room.door.Location
import lns.scenes.game.shots.*
import lns.scenes.game.stats.*
import lns.scenes.game.stats
import scala.language.implicitConversions

object CollisionUpdater {

  /**
   * Update an Anything, accordingly to a strategy, if there's a collision
   * @param anything
   * @param anythings
   * @param f
   *   function that set the strategy to update an anything
   * @return
   *   an updated AnythingModel
   */
  def apply(anything: AnythingModel)(anythings: Map[AnythingId, AnythingModel])(
      f: (AnythingModel, AnythingModel) => Outcome[AnythingModel]
  ): Outcome[AnythingModel] =
    //prendo solo i solid che non sono crossable e diversi da me stesso
    anything match {
      case a: SolidModel if !a.crossable =>
        anythings.values
          .collect {
            case against: SolidModel if !against.crossable && against.id != anything.id => against
          }
          //uno shot non si scontra con un altro shot, uno shot non si scontra con il proprietario
          .filter(against =>
            (anything, against) match {
              case (shot: ShotModel, other: ShotModel)                => false // no collision between 2 shots
              case (shot: ShotModel, other) if shot.owner == other.id => false // no collision shot and owner
              case (other, shot: ShotModel) if shot.owner == other.id => false // no collision shot and owner
              case _                                                  => true
            }
          )
          .foldLeft(Outcome(anything)) { (anything, against) =>
            for {
              any               <- anything
              anyUpdatedWithAll <- f(any, against)
            } yield anyUpdatedWithAll
          }
      case _ => Outcome(anything)
    }

  /**
   * Update an anything position if there is a collision with another element
   * @param anything
   * @param against
   * @return
   *   an updated anything moved the minimum necessary not to collide with the other element
   */
  def updateMove(anything: AnythingModel, against: AnythingModel): Outcome[AnythingModel] =
    (anything, against) match {
      case (anything: SolidModel with DynamicModel, against: SolidModel) =>
        anything match {
          case s: ShotModel =>
            val newPosition = Boundary.elementBound(against.shotArea, anything.boundingBox)
            Outcome(
              anything
                .withDynamic(
                  newPosition,
                  anything.speed,
                  anything.collisionDetected | newPosition.position != anything.boundingBox.position
                )
                .asInstanceOf[anything.Model]
            )
          case _ =>
            val newPosition = Boundary.elementBound(against.boundingBox, anything.boundingBox)
            Outcome(
              anything
                .withDynamic(
                  newPosition,
                  anything.speed,
                  anything.collisionDetected | newPosition.position != anything.boundingBox.position
                )
                .asInstanceOf[anything.Model]
            )
        }
      case _ => Outcome(anything)
    }

  /**
   * Update anything stats if there is a collision with another element
   * @param context
   * @param anything
   * @param against
   * @return
   *   anything updated with new stats, based on the element's stats it collides with
   */
  def updateStats(
      context: FrameContext[StartupData]
  )(anything: AnythingModel, against: AnythingModel): Outcome[AnythingModel] =
    import Utils.checkCollisionAndUpdate

    (anything, against) match {

      case (e1: EnemyModel, e2: EnemyModel) => Outcome(anything)

      case (character: CharacterModel, item: ItemModel) if !item.pickedup =>
        checkCollisionAndUpdate(character, item)(character =>
          item.stats.foldLeft(Outcome(character))((character, stat) =>
            for {
              c                <- character
              updatedCharacter <- c.sumStat(context, stat)
            } yield updatedCharacter
          )
        )

      case (item: ItemModel, character: CharacterModel) if !item.pickedup =>
        checkCollisionAndUpdate(item, character)(item => Outcome(item.withPick(true)))

      case (anything: SolidModel with AliveModel, against: SolidModel with DamageModel) =>
        checkCollisionAndUpdate(anything, against)(anything =>
          anything.hit(
            context,
            anything match {
              case s: ShotModel => s.life
              case _            => PropertyName.Damage @@ against.stats
            }
          )
        )

      case _ => Outcome(anything)
    }
}

object Utils {
  def checkCollisionAndUpdate[T <: AnythingModel](anything: T, against: AnythingModel)(f: T => Outcome[T]): Outcome[T] =
    (anything, against) match {
      case (solid: SolidModel, shot: ShotModel) =>
        Collision.withElement(shot.boundingBox, solid.shotArea)

      case (shot: ShotModel, solid: SolidModel) =>
        Collision.withElement(solid.shotArea, shot.boundingBox)

      case _ =>
        Collision.withElement(against.boundingBox, anything.boundingBox)

    } match {

      case Some(_, _) =>
        f(anything)
      case _ => Outcome(anything)
    }
}
