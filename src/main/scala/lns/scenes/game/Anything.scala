package lns.scenes.game

import indigo.*
import indigo.shared.scenegraph.*
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import lns.StartupData
import lns.core.Assets
import indigo.scenes.SceneEvent.JumpTo
import lns.scenes.menu.MenuScene

/*Util*/
extension (s: Group) def moveTo(vector: Vector2)       = s.moveTo(vector.x.toInt, vector.y.toInt)
extension (b: BoundingBox) def moveBy(vector: Vector2) = b.moveBy(vector.x, vector.y)
extension (v: Vector2) def map[B](f: Vector2 => B): B  = f(v)

/*Anything*/
trait AnythingModel[M] {
  val boundingBox: BoundingBox
  val life: Option[Int]
  val invincibilityTimer: Double

  val invincibility: Double = 2.0
  val damage: Double        = 1

  def edit(life: Option[Int], invincibilityTimer: Double): M
  def edit(life: Option[Int]): M
  def edit(invincibilityTimer: Double): M

  def downLife(context: FrameContext[StartupData], danno: Int): Outcome[M] =
    if (invincibilityTimer == 0) {
      Outcome(edit(life.flatMap(l => Some(l - danno)), invincibility))
    } else {
      Outcome(edit(life))
    }

  def getPosition(): Vector2 = Vector2(boundingBox.horizontalCenter, boundingBox.bottom)
  def update(context: FrameContext[StartupData]): Outcome[M] =
    println("[AnythingModel] UPDATE")
    if (invincibilityTimer > 0)
      Outcome(edit(invincibilityTimer - context.gameTime.delta.toDouble))
    else
      Outcome(edit(0.0d))
}

trait AnythingViewModel[VM]

trait Anything[M, VM] {
  type Model <: AnythingModel[M]
  type ViewModel <: AnythingViewModel[VM] | Unit
  type View <: Group

  //val model: Model
  //val viewModel: ViewModel

  def view(model: Model, viewModel: ViewModel): View

  def draw(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): SceneUpdateFragment =
    SceneUpdateFragment(
      view(model, viewModel).moveTo(model.getPosition())
    )
}

/**/
enum DynamicState {
  case IDLE, MOVE_LEFT, MOVE_RIGHT, MOVE_DOWN, MOVE_UP
}
import DynamicState._

trait DynamicModel[M <: AnythingModel[M] with DynamicModel[M]] extends AnythingModel[M] {
  val speed: Vector2

  def isMoving(): Boolean = getState() match {
    case IDLE => false
    case _    => true
  }

  def getState(): DynamicState = speed match {
    case Vector2(x, _) if x < 0 => MOVE_LEFT
    case Vector2(x, _) if x > 0 => MOVE_RIGHT
    case Vector2(_, y) if y < 0 => MOVE_UP
    case Vector2(_, y) if y > 0 => MOVE_DOWN
    case _                      => IDLE
  }

  def computeSpeed(context: FrameContext[StartupData]): Vector2
  def edit(boundingBox: BoundingBox, speed: Vector2): M

  override def update(context: FrameContext[StartupData]): Outcome[M] =
    for {
      superObj <- super.update(context)
      newSpeed = computeSpeed(context)
      newObj   = superObj.edit(boundingBox.moveBy(newSpeed), newSpeed)
    } yield newObj
  // superObj.map(newObj => computeSpeed(context).map(newSpeed => newObj.edit(boundingBox.moveBy(newSpeed), newSpeed)))
}

case class CharacterModel(boundingBox: BoundingBox, life: Option[Int], speed: Vector2, invincibilityTimer: Double = 0)
    extends DynamicModel[CharacterModel] {
  val maxSpeed = 3

  val inputMappings: InputMapping[Vector2] =
    InputMapping(
      Combo.withKeyInputs(Key.LEFT_ARROW, Key.UP_ARROW)    -> Vector2(-maxSpeed, -maxSpeed),
      Combo.withKeyInputs(Key.LEFT_ARROW, Key.DOWN_ARROW)  -> Vector2(-maxSpeed, maxSpeed),
      Combo.withKeyInputs(Key.LEFT_ARROW)                  -> Vector2(-maxSpeed, 0.0d),
      Combo.withKeyInputs(Key.RIGHT_ARROW, Key.UP_ARROW)   -> Vector2(maxSpeed, -maxSpeed),
      Combo.withKeyInputs(Key.RIGHT_ARROW, Key.DOWN_ARROW) -> Vector2(maxSpeed, maxSpeed),
      Combo.withKeyInputs(Key.RIGHT_ARROW)                 -> Vector2(maxSpeed, 0.0d),
      Combo.withKeyInputs(Key.UP_ARROW)                    -> Vector2(0.0d, -maxSpeed),
      Combo.withKeyInputs(Key.DOWN_ARROW)                  -> Vector2(0.0d, maxSpeed)
    )

  def edit(life: Option[Int], invincibilityTimer: Double): CharacterModel =
    copy(life = life, invincibilityTimer = invincibilityTimer)
  def edit(life: Option[Int]): CharacterModel          = copy(life = life)
  def edit(invincibilityTimer: Double): CharacterModel = copy(invincibilityTimer = invincibilityTimer)
  def edit(boundingBox: BoundingBox, speed: Vector2): CharacterModel =
    copy(boundingBox = boundingBox, speed = speed)

  def computeSpeed(context: FrameContext[StartupData]): Vector2 =
    context.inputState.mapInputs(inputMappings, Vector2.zero)

}

case class Character() extends Anything[CharacterModel, Unit] with Isaac {
  type Model     = CharacterModel
  type ViewModel = Unit
  type View      = Group

  def view(model: Model, viewModel: ViewModel): View =
    Group()
      .addChild(boundingModel)
      .addChild(shadowModel)
      .addChild(bodyModel(model))
      .addChild(headModel(model))
      .withRef(width / 2, height / 2)
      .withScale(Vector2(scale, scale))
}

object CharacterModel {
  def initial(startupData: StartupData): CharacterModel = CharacterModel(
    BoundingBox(
      Vertex(startupData.screenDimensions.horizontalCenter, startupData.screenDimensions.verticalCenter),
      Vertex(28, 33)
    ),
    Some(10),
    Vector2(0, 0)
  )
}

trait Isaac {
  val width: Int  = 28
  val height: Int = 33
  val scale: Int  = 3

  /* Head */
  val headWidth: Int  = 28
  val headHeight: Int = 25
  def headCrop(model: CharacterModel): Rectangle = model.getState() match {
    case MOVE_LEFT  => Rectangle(250, 25, headWidth, headHeight)
    case MOVE_RIGHT => Rectangle(90, 25, headWidth, headHeight)
    case MOVE_UP    => Rectangle(170, 25, headWidth, headHeight)
    case _          => Rectangle(10, 25, headWidth, headHeight)
  }
  def headModel(model: CharacterModel): Graphic[Material.Bitmap] =
    Graphic(headCrop(model), 1, Material.Bitmap(Assets.Character.character))
      .withRef(0, 0)
      .moveTo(0, 0)

  /* Body */
  val bodyWidth: Int  = 18
  val bodyHeight: Int = 13
  def bodyCrop(model: CharacterModel): Rectangle = model.getState() match {
    case MOVE_LEFT  => Rectangle(15, 123, bodyWidth, bodyHeight)
    case MOVE_RIGHT => Rectangle(175, 123, bodyWidth, bodyHeight)
    case MOVE_UP    => Rectangle(15, 80, bodyWidth, bodyHeight)
    case _          => Rectangle(175, 80, bodyWidth, bodyHeight)
  }
  def bodyFlip(model: CharacterModel): Boolean = model.getState() match {
    case MOVE_LEFT => true
    case _         => false
  }
  def bodyModel(model: CharacterModel): Graphic[Material.Bitmap] =
    Graphic(bodyCrop(model), 1, Material.Bitmap(Assets.Character.character))
      .withRef(bodyWidth / 2, 0)
      .flipHorizontal(bodyFlip(model))
      .moveTo(width / 2, 20)

  /*Shadow*/
  val shadowModel: Shape =
    Shape
      .Circle(
        center = Point(width / 2, height + width / 4),
        radius = width / 3,
        Fill.Color(RGBA(0, 0, 0, 0.5))
      )
      .scaleBy(1, 0.25)

  /*Bounding*/
  val boundingModel: Shape =
    Shape.Box(
      Rectangle(Point(0, 0), Size(width, height)),
      Fill.Color(RGBA(1, 1, 1, 0.5))
    )
}
