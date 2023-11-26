package my2048

import MainData
import korlibs.image.color.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.math.geom.*
import my2048.TileNumber.*

class Tile(var tileNumber:TileNumber) : Container() {

  init {
    roundRect(Size(MainData.cellSize,MainData.cellSize), RectCorners(5.0), fill = tileNumber.color)
    val textColor = when(tileNumber) {
      ZERO, ONE -> Colors.BLACK
      else -> Colors.WHITE
    }
    text(tileNumber.value.toString(),textSizeFor(tileNumber),textColor) {
      centerBetween(0.0,0.0,MainData.cellSize,MainData.cellSize)
    }
  }

  private fun textSizeFor(tileNumber: TileNumber):Double = when (tileNumber) {
    ZERO, ONE, TWO, THREE, FOUR, FIVE -> MainData.cellSize / 2
    SIX, SEVEN, EIGHT -> MainData.cellSize * 4 / 9
    NINE, TEN, ELEVEN, TWELVE -> MainData.cellSize * 2 / 5
    THIRTEEN, FOURTEEN, FIFTEEN -> MainData.cellSize * 7 / 20
    SIXTEEN -> MainData.cellSize * 3 / 10
  }


}

fun Container.tile(
  number: TileNumber,
  callback: @ViewDslMarker Tile.() -> Unit = {}
):Tile = Tile(number).addTo(this,callback)
