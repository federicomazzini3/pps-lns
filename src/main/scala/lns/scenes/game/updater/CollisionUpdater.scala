package lns.scenes.game.updater

import indigo.shared.{ FrameContext, Outcome }
import lns.StartupData
import lns.scenes.game.anything.*
import lns.scenes.game.room.*
import lns.scenes.game.shot.*
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
            Outcome(
              anything
                .withDynamic(Boundary.elementBound(against.shotArea, anything.boundingBox), anything.speed)
                .asInstanceOf[anything.Model]
            )
          case _ =>
            Outcome(
              anything
                .withDynamic(Boundary.elementBound(against.boundingBox, anything.boundingBox), anything.speed)
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
          case _ =>
            Collision.withElement(against.boundingBox, anything.boundingBox)
        } match {
          case Some(a, b) =>
            anything.hit(context, PropertyName.Damage @@ against.stats)
          case _ => Outcome(anything)
        }
      case _ => Outcome(anything)
    }
}
