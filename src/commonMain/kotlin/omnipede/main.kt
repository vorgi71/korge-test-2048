package omnipede

import korlibs.event.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.io.file.std.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.korge.view.Circle
import korlibs.korge.view.align.*
import korlibs.math.geom.*

import korlibs.math.geom.shape.*
import korlibs.math.geom.vector.*
import kotlin.math.*

suspend fun main() = Korge(
  windowSize = Size(480, 640),
  title = "omnipede",
  backgroundColor = RGBA(254, 247, 240)
) {
  val sceneContainer = sceneContainer()
  sceneContainer.changeTo { StartScene() }
}

class StartScene : Scene() {
  override suspend fun SContainer.sceneMain() {
    var font = resourcesVfs["clear_sans.fnt"].readBitmapFont()

    var cellSize = max(views.virtualWidth / 30.0, views.virtualHeight / 32.0).toInt()
    var fieldWidth = cellSize * 30
    var leftIndent = (views.virtualWidth - fieldWidth) / 2
    var fieldHeight = cellSize * 32
    var topIndent = (views.virtualHeight - fieldHeight) / 2

    var player: RoundRect? = null
    var missile: RoundRect? = null
    var infoText: Text? = null


    val bgField = roundRect(Size(fieldWidth, fieldHeight), RectCorners(1.0), fill = Colors["#1E1F22"]) {
      position(leftIndent, topIndent)
      val playerField = roundRect(Size(fieldWidth, 6 * cellSize), RectCorners(1.0), fill = Colors["#1a401d"]) {
        position(0, 24 * cellSize)
      }
      player = roundRect(Size(cellSize, cellSize), RectCorners(cellSize / 2), fill = Colors.GOLD) {
        position(cellSize * 15, cellSize * 29)
      }
      missile = roundRect(Size(4, cellSize), RectCorners(1), Colors.LIGHTCORAL) {
        position(cellSize * 15, cellSize * 29)
      }

      infoText = text("2048", cellSize * 1, Colors.WHITE, font).position(50, cellSize * 30)
    }


    var shooting = false

    this.addUpdater {
      val gamepads = input.connectedGamepads
      val rawGamepad0 = input.gamepads[0]
      val pressedStart: Boolean = if (rawGamepad0[GameButton.START] > 0.5) true else false
      val pressedX = if(rawGamepad0.get(GameButton.PS_CROSS)>0.5) true else false

      val pos: Point = rawGamepad0[GameStick.LEFT]
      infoText!!.text = "pos: $pos start: $pressedStart pressedX: $pressedX shooting: $shooting"
      player!!.x += pos.x * 8
      player!!.y -= pos.y * 4

      if (!shooting) {
        missile!!.x = player!!.x + (cellSize / 2) - 2
        missile!!.y = player!!.y
      } else {
        missile!!.y -= 8
        if(missile!!.y<0) {
          shooting=false
          missile!!.x = player!!.x + (cellSize / 2) - 2
          missile!!.y = player!!.y
        }
      }

      if (pressedStart) {
        player!!.position(cellSize * 15, cellSize * 29)
      }
      if(pressedX && !shooting) {
        shooting=true
      }
    }

  }
}
