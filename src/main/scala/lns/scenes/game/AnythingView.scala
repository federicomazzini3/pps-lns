package lns.scenes.game

import indigo.*
import indigo.shared.scenegraph.*
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import lns.StartupData
import lns.core.Assets
import indigo.scenes.SceneEvent.JumpTo
import indigo.shared.*
import indigo.platform.assets.*
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.input.{ Gamepad, Keyboard, Mouse }
import indigo.shared.time.GameTime
import lns.scenes.menu.MenuScene

/*Util*/
extension (s: Group) def moveTo(vector: Vector2)       = s.moveTo(vector.x.toInt, vector.y.toInt)
extension (b: BoundingBox) def moveBy(vector: Vector2) = b.moveBy(vector.x, vector.y)

/*Anything*/
trait AnythingModel {
  type Model >: this.type <: AnythingModel

  val boundingBox: BoundingBox

  def getPosition(): Vector2 = Vector2(boundingBox.horizontalCenter, boundingBox.bottom)

  def update(context: FrameContext[StartupData]): Outcome[Model] = Outcome(this)
}

trait AnythingViewModel {
  type ViewModel >: this.type <: AnythingViewModel
}

trait AnythingView {
  type Model <: AnythingModel
  type ViewModel <: AnythingViewModel | Unit
  type View <: Group

  def view(model: Model, viewModel: ViewModel): View

  def draw(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): SceneUpdateFragment =
    SceneUpdateFragment(
      view(model, viewModel).moveTo(model.getPosition())
    )
}

/*Dynamic*/
enum DynamicState {
  case IDLE, MOVE_LEFT, MOVE_RIGHT, MOVE_DOWN, MOVE_UP
}
import DynamicState._

trait DynamicModel extends AnythingModel {
  type Model >: this.type <: DynamicModel

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
  def edit(boundingBox: BoundingBox, speed: Vector2): Model

  override def update(context: FrameContext[StartupData]): Outcome[Model] =
    for {
      superObj <- super.update(context)
      newSpeed = computeSpeed(context)
      newObj   = superObj.edit(boundingBox.moveBy(newSpeed), newSpeed).asInstanceOf[Model]
    } yield newObj

  /*
    val superObj: Outcome[Model] = super.update(context);
    val superModel: Model        = superObj.unsafeGet
    val newSpeed                 = computeSpeed(context);
    val up: Model                = superModel.edit(boundingBox.moveBy(newSpeed), newSpeed).asInstanceOf[Model]

    Outcome(up)
   */

  /*
    superObj.map[Model] { (newObj: Model) =>
      val newSpeed  = computeSpeed(context);
      val up: Model = newObj.edit(boundingBox.moveBy(newSpeed), newSpeed);
    }
   */

}

trait AliveModel extends AnythingModel {
  type Model >: this.type <: AliveModel

  val life: Int
  val invincibilityTimer: Double
  val invincibility: Double

  def edit(life: Int, invincibilityTimer: Double): Model

  def hit(context: FrameContext[StartupData], danno: Int): Outcome[Model] = invincibilityTimer match {
    case 0 if life - danno > 0 => Outcome(edit(life - danno, invincibility))
    case 0                     => Outcome(edit(0, invincibility))
    case _                     => Outcome(this)
  }

  override def update(context: FrameContext[StartupData]): Outcome[Model] = for {
    superObj <- super.update(context)
    newObj = invincibilityTimer match {
      case 0 => superObj
      case _ if invincibilityTimer - context.gameTime.delta.toDouble > 0 =>
        superObj.edit(life, invincibilityTimer - context.gameTime.delta.toDouble).asInstanceOf[Model]
      case _ => superObj.edit(life, 0).asInstanceOf[Model]
    }
  } yield newObj

}

case class CharacterModel(boundingBox: BoundingBox, life: Int, speed: Vector2, invincibilityTimer: Double = 0)
    extends AnythingModel
    with AliveModel
    with DynamicModel {

  type Model = CharacterModel

  val maxSpeed              = 4
  val invincibility: Double = 2.0

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

  def edit(life: Int, invincibilityTimer: Double): Model =
    copy(life = life, invincibilityTimer = invincibilityTimer)
  def edit(boundingBox: BoundingBox, speed: Vector2): Model =
    copy(boundingBox = boundingBox, speed = speed)

  def computeSpeed(context: FrameContext[StartupData]): Vector2 =
    context.inputState.mapInputs(inputMappings, Vector2.zero) * context.gameTime.delta.toDouble

}

case class CharacterView() extends AnythingView with Isaac {
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
    10,
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

object app extends App {
  import indigo.shared.constants.Key
  import indigo.shared.events.KeyboardEvent.KeyDown

  var character = CharacterView()

  var startupData = StartupData(screenDimensions = Rectangle(0, 0, 0, 0))
  var model       = CharacterModel.initial(startupData)

  val keyboard =
    Keyboard.calculateNext(
      Keyboard.default,
      List(KeyDown(Key.LEFT_ARROW), KeyDown(Key.DOWN_ARROW))
    )

  val inputState = new InputState(Mouse.default, keyboard, Gamepad.default)

  println("OLD " + model.boundingBox)

  var newModel = model
    .update(
      new FrameContext[StartupData](
        GameTime.zero,
        Dice.fromSeed(1000),
        inputState,
        new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
        startupData
      )
    )
    .unsafeGet
  println("OLD NOT EDITED" + model.boundingBox)

  newModel = model
    .update(
      new FrameContext[StartupData](
        GameTime.withDelta(Seconds(1), Seconds(1.5)),
        Dice.fromSeed(1000),
        inputState,
        new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
        startupData
      )
    )
    .unsafeGet

  println("NEW AFTER EDIT" + newModel.boundingBox)

}
