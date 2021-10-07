package lns.core

import indigo.*
import indigoextras.ui.ButtonAssets

/**
 * Factory for Assets
 */
object Assets {

  val baseUrl: String = "assets/"

  def initialAssets(): Set[AssetType] = Buttons.assets ++ Fonts.assets

  def initialFont(): FontInfo = Fonts.fontInfo

  def secondary(): Set[AssetType] = Character.assets ++ Doors.assets ++ Rooms.assets ++ Objects.assets

  object Buttons {
    val start: AssetName = AssetName("btn_start")

    object Graphics {
      val start: ButtonAssets =
        ButtonAssets(
          up = Graphic(0, 0, 221, 70, 2, Material.Bitmap(Buttons.start)).withCrop(0, 0, 221, 70),
          over = Graphic(0, 0, 221, 70, 2, Material.Bitmap(Buttons.start)).withCrop(0, 0, 221, 70),
          down = Graphic(0, 0, 221, 70, 2, Material.Bitmap(Buttons.start)).withCrop(0, 0, 221, 70)
        )
    }

    val assets: Set[AssetType] = Set(
      AssetType.Image(start, AssetPath(baseUrl + "btn_start.png"))
    )
  }

  object Fonts {
    val smallFontName: AssetName            = AssetName("smallFontName")
    val fontKey: FontKey                    = FontKey("boxy font")
    val fontMaterial: Material.ImageEffects = Material.ImageEffects(smallFontName)

    val fontInfo: FontInfo =
      FontInfo(fontKey, 320, 230, FontChar("?", 47, 26, 11, 12))
        .addChar(FontChar("A", 2, 39, 10, 12))
        .addChar(FontChar("B", 14, 39, 9, 12))
        .addChar(FontChar("C", 25, 39, 10, 12))
        .addChar(FontChar("D", 37, 39, 9, 12))
        .addChar(FontChar("E", 49, 39, 9, 12))
        .addChar(FontChar("F", 60, 39, 9, 12))
        .addChar(FontChar("G", 72, 39, 9, 12))
        .addChar(FontChar("H", 83, 39, 9, 12))
        .addChar(FontChar("I", 95, 39, 5, 12))
        .addChar(FontChar("J", 102, 39, 9, 12))
        .addChar(FontChar("K", 113, 39, 10, 12))
        .addChar(FontChar("L", 125, 39, 9, 12))
        .addChar(FontChar("M", 136, 39, 13, 12))
        .addChar(FontChar("N", 2, 52, 11, 12))
        .addChar(FontChar("O", 15, 52, 10, 12))
        .addChar(FontChar("P", 27, 52, 9, 12))
        .addChar(FontChar("Q", 38, 52, 11, 12))
        .addChar(FontChar("R", 51, 52, 10, 12))
        .addChar(FontChar("S", 63, 52, 9, 12))
        .addChar(FontChar("T", 74, 52, 11, 12))
        .addChar(FontChar("U", 87, 52, 10, 12))
        .addChar(FontChar("V", 99, 52, 9, 12))
        .addChar(FontChar("W", 110, 52, 13, 12))
        .addChar(FontChar("X", 125, 52, 9, 12))
        .addChar(FontChar("Y", 136, 52, 11, 12))
        .addChar(FontChar("Z", 149, 52, 10, 12))
        .addChar(FontChar("0", 2, 13, 10, 12))
        .addChar(FontChar("1", 13, 13, 7, 12))
        .addChar(FontChar("2", 21, 13, 9, 12))
        .addChar(FontChar("3", 33, 13, 9, 12))
        .addChar(FontChar("4", 44, 13, 9, 12))
        .addChar(FontChar("5", 56, 13, 9, 12))
        .addChar(FontChar("6", 67, 13, 9, 12))
        .addChar(FontChar("7", 79, 13, 9, 12))
        .addChar(FontChar("8", 90, 13, 10, 12))
        .addChar(FontChar("9", 102, 13, 9, 12))
        .addChar(FontChar("?", 47, 26, 11, 12))
        .addChar(FontChar("!", 2, 0, 6, 12))
        .addChar(FontChar(".", 143, 0, 6, 12))
        .addChar(FontChar(",", 124, 0, 8, 12))
        .addChar(FontChar("-", 133, 0, 9, 12))
        .addChar(FontChar(" ", 112, 13, 8, 12))
        .addChar(FontChar("[", 2, 65, 7, 12))
        .addChar(FontChar("]", 21, 65, 7, 12))
        .addChar(FontChar("(", 84, 0, 7, 12))
        .addChar(FontChar(")", 93, 0, 7, 12))
        .addChar(FontChar("\\", 11, 65, 8, 12))
        .addChar(FontChar("/", 150, 0, 9, 12))
        .addChar(FontChar(":", 2, 26, 5, 12))
        .addChar(FontChar("@", 60, 26, 11, 12))
        .addChar(FontChar("_", 42, 65, 9, 12))
        .addChar(FontChar("%", 47, 0, 14, 12))

    val assets: Set[AssetType] =
      Set(
        AssetType.Image(Fonts.smallFontName, AssetPath(baseUrl + "boxy_font_small.png"))
      )
  }

  object Character {
    val specificUrl: String = "characters/"

    val character: AssetName = AssetName("character")

    val assets: Set[AssetType] = Set(
      AssetType.Image(character, AssetPath(baseUrl + specificUrl + "isaac.png"))
    )
  }

  object Doors {
    val specificUrl: String = "doors/"

    val doorCloseEast: AssetName  = AssetName("door-close-east")
    val doorCloseNorth: AssetName = AssetName("door-close-north")
    val doorCloseWest: AssetName  = AssetName("door-close-south")
    val doorCloseSouth: AssetName = AssetName("door-close-west")
    val doorLockEast: AssetName   = AssetName("door-close-east")
    val doorLockNorth: AssetName  = AssetName("door-close-north")
    val doorLockWest: AssetName   = AssetName("door-close-south")
    val doorLockSouth: AssetName  = AssetName("door-close-west")
    val doorOpenEast: AssetName   = AssetName("door-close-east")
    val doorOpenNorth: AssetName  = AssetName("door-close-north")
    val doorOpenWest: AssetName   = AssetName("door-close-south")
    val doorOpenSouth: AssetName  = AssetName("door-close-west")

    val assets: Set[AssetType] = Set(
      AssetType.Image(doorCloseEast, AssetPath(baseUrl + specificUrl + "door_close_east.png")),
      AssetType.Image(doorCloseNorth, AssetPath(baseUrl + specificUrl + "door_close_north.png")),
      AssetType.Image(doorCloseWest, AssetPath(baseUrl + specificUrl + "door_close_west.png")),
      AssetType.Image(doorCloseSouth, AssetPath(baseUrl + specificUrl + "door_close_south.png")),
      AssetType.Image(doorLockEast, AssetPath(baseUrl + specificUrl + "door_close_east.png")),
      AssetType.Image(doorLockNorth, AssetPath(baseUrl + specificUrl + "door_close_north.png")),
      AssetType.Image(doorLockWest, AssetPath(baseUrl + specificUrl + "door_close_west.png")),
      AssetType.Image(doorLockSouth, AssetPath(baseUrl + specificUrl + "door_close_south.png")),
      AssetType.Image(doorOpenEast, AssetPath(baseUrl + specificUrl + "door_close_east.png")),
      AssetType.Image(doorOpenNorth, AssetPath(baseUrl + specificUrl + "door_close_north.png")),
      AssetType.Image(doorOpenWest, AssetPath(baseUrl + specificUrl + "door_close_west.png")),
      AssetType.Image(doorOpenSouth, AssetPath(baseUrl + specificUrl + "door_close_south.png"))
    )
  }

  object Rooms {

    val specificUrl: String = "rooms/"

    val emptyRoom: AssetName = AssetName("empty-room")

    val assets: Set[AssetType] = Set(
      AssetType.Image(emptyRoom, AssetPath(baseUrl + specificUrl + "room.png"))
    )
  }

  object Objects {

    val specificUrl: String = "objects/"

    val barrelCovered: AssetName   = AssetName("barrel-covered")
    val barrelUncovered: AssetName = AssetName("barrel-uncovered")
    val barrelBroken: AssetName    = AssetName("barrel-broken")
    val crateCovered: AssetName    = AssetName("crate-covered")
    val crateUncovered: AssetName  = AssetName("crate-uncovered")
    val crateBroken: AssetName     = AssetName("crate-broken")
    val chestOpen: AssetName       = AssetName("chest-open")
    val chestClosed: AssetName     = AssetName("chest-closed")
    val coinGold: AssetName        = AssetName("coin-gold")
    val coinSilver: AssetName      = AssetName("coin-silver")
    val key: AssetName             = AssetName("key")
    val stone: AssetName           = AssetName("stone")

    val assets: Set[AssetType] = Set(
      AssetType.Image(barrelUncovered, AssetPath(baseUrl + specificUrl + "barrel-uncovered.png")),
      AssetType.Image(barrelCovered, AssetPath(baseUrl + specificUrl + "barrel-covered.png")),
      AssetType.Image(barrelBroken, AssetPath(baseUrl + specificUrl + "barrel-broken.png")),
      AssetType.Image(chestClosed, AssetPath(baseUrl + specificUrl + "chest-closed.png")),
      AssetType.Image(chestOpen, AssetPath(baseUrl + specificUrl + "chest-open.png")),
      AssetType.Image(coinGold, AssetPath(baseUrl + specificUrl + "coin-gold.png")),
      AssetType.Image(coinSilver, AssetPath(baseUrl + specificUrl + "coin-silver.png")),
      AssetType.Image(crateBroken, AssetPath(baseUrl + specificUrl + "crate-broken.png")),
      AssetType.Image(crateCovered, AssetPath(baseUrl + specificUrl + "crate-covered.png")),
      AssetType.Image(crateUncovered, AssetPath(baseUrl + specificUrl + "crate-uncovered.png")),
      AssetType.Image(key, AssetPath(baseUrl + specificUrl + "key.png")),
      AssetType.Image(stone, AssetPath(baseUrl + specificUrl + "stone.png"))
    )

  }

}
