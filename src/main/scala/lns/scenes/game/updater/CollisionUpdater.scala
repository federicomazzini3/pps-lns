package lns.scenes.game.updater

import indigo.shared.{ FrameContext, Outcome }
import lns.StartupData
import lns.scenes.game.anything.{ SolidModel, * }
import lns.scenes.game.characters.CharacterModel
import lns.scenes.game.enemies.EnemyModel
import lns.scenes.game.items.ItemModel
import lns.scenes.game.room.*
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
              an  <- anything
              all <- f(an, against)
            } yield all //f(an, against)
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
  def updateLife(
      context: FrameContext[StartupData]
  )(anything: AnythingModel, against: AnythingModel): Outcome[AnythingModel] =
    (anything, against) match {
      case (anything: SolidModel with AliveModel, against: SolidModel with DamageModel) =>
        (anything, against) match {
          case (_, s: ShotModel) =>
            Collision.withElement(against.boundingBox, anything.shotArea)
          case (s: ShotModel, _) =>
            Collision.withElement(against.shotArea, anything.boundingBox)
          case (e1: EnemyModel, e2: EnemyModel) =>
            None
          case _ =>
            Collision.withElement(against.boundingBox, anything.boundingBox)
        } match {
          case Some(_, _) =>
            anything.hit(
              context,
              anything match {
                case s: ShotModel => s.life
                case _            => PropertyName.Damage @@ against.stats
              }
            )
          case _ => Outcome(anything)
        }
      case (character: CharacterModel, item: ItemModel) if !item.pickedup =>
        Collision.withElement(against.boundingBox, anything.boundingBox) match {
          case Some(_, _) =>
            item.stats.foldLeft(Outcome(character))((character, stat) =>
              for {
                c                <- character
                updatedCharacter <- c.sumStat(context, stat)
              } yield updatedCharacter
            )
          case _ => Outcome(character)
        }
      case (item: ItemModel, character: CharacterModel) if !item.pickedup =>
        Collision.withElement(against.boundingBox, anything.boundingBox) match {
          case Some(_, _) => Outcome(item.withPick(true))
          case _          => Outcome(item)
        }
      case _ => Outcome(anything)
    }
}
