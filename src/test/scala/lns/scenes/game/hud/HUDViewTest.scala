package lns.scenes.game.hud

import org.scalatest.freespec.AnyFreeSpec
import lns.scenes.game.hud.HUDView.*
import lns.scenes.game.hud.HUDView.HeartStatus.*

class HUDViewTest extends AnyFreeSpec {
  "In the HUD" - {
    "the character life compute should show hearts" - {
      "all full if life is full and equal to max life" in {
        assert(computeLife(3, 3) == List(Full, Full, Full))
      }
      "all empty hearts if life is over" in {
        assert(computeLife(3, 0) == List(Empty, Empty, Empty))
      }
      "1 full and 2 empty if life is 1 over 3" in {
        assert(computeLife(3, 1) == List(Full, Empty, Empty))
      }
      "2 full and 1 empty if life is 2 over 3" in {
        assert(computeLife(3, 2) == List(Full, Full, Empty))
      }
      "1 full, 1 half and 1 empty if life is 1.5 over 3" in {
        assert(computeLife(3, 1.5) == List(Full, Half, Empty))
      }
    }
  }
}
