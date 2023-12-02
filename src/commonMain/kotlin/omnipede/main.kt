package omnipede

import korlibs.audio.sound.*
import korlibs.event.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.image.format.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.math.*
import korlibs.math.geom.*
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
    var zap = resourcesVfs["zap.wav"].readSound()

    var mushroomSpriteMap = resourcesVfs["Mushroom.png"].readBitmap()
    var mushroomAnimation = SpriteAnimation(mushroomSpriteMap,8,8, columns = 4)

    var flowerSprite = resourcesVfs["Flower.png"].readBitmap()

    var cellSize = max(views.virtualWidth / 30.0, views.virtualHeight / 32.0).toInt()
    var fieldWidth = cellSize * 30
    var leftIndent = (views.virtualWidth - fieldWidth) / 2
    var fieldHeight = cellSize * 32
    var topIndent = (views.virtualHeight - fieldHeight) / 2

    val bgField = roundRect(Size(fieldWidth, fieldHeight), RectCorners(1.0), fill = Colors["#1E1F22"]) {
      position(leftIndent, topIndent)
    }
    val playerField = roundRect(Size(fieldWidth, 6 * cellSize), RectCorners(1.0), fill = Colors["#1a401d"]) {
      position(leftIndent, topIndent + 24 * cellSize)
    }
    var player: RoundRect = roundRect(Size(cellSize, cellSize), RectCorners(cellSize / 2), fill = Colors.GOLD) {
      position(leftIndent + cellSize * 15, topIndent + cellSize * 29)
    }
    var missile: RoundRect = roundRect(Size(4, cellSize), RectCorners(1), Colors.LIGHTCORAL) {
      position(leftIndent + cellSize * 15, topIndent + cellSize * 29)
      hitTestEnabled=true
    }

    Lawn.init(this,cellSize,mushroomAnimation,flowerSprite,126)

    var infoText: Text = text("2048", cellSize * 1, Colors.WHITE, font).position(leftIndent, topIndent + cellSize * 30)

    var shooting = false

    this.addUpdater {
      val gamepads = input.connectedGamepads
      if (gamepads.size == 0) {
        infoText.text = "no controller connected"
        return@addUpdater
      }
      val rawGamepad0 = input.gamepads[0]
      val pressedStart: Boolean = if (rawGamepad0[GameButton.START] > 0.5) true else false
      val pressedX = if (rawGamepad0.get(GameButton.PS_CROSS) > 0.5) true else false

      val pos: Point = rawGamepad0[GameStick.LEFT]
      infoText.text = "pos: $pos start: $pressedStart pressedX: $pressedX shooting: $shooting"
      player.x += pos.x * (cellSize / 2)
      player.x = player.x.clamp(playerField.x,playerField.x+playerField.width-cellSize)
      player.y -= pos.y * (cellSize / 3)
      player.y = player.y.clamp(playerField.y,playerField.y+playerField.height-cellSize)

      if (!shooting) {
        missile.x = player.x + (cellSize / 2) - 2
        missile.y = player.y
      } else {
        missile.y -= 8

        var missileHit=Lawn.getViewObjects().find {
          val tempRect1 = it.getGlobalBounds()
          val tempRect2 = missile.getGlobalBounds()
          return@find tempRect1.intersects(tempRect2)
        }

        val hitObject=Lawn.objects.find { lawnObject -> lawnObject.viewObject==missileHit }
        hitObject?.hit()

        if (missile.y < 0 || missileHit!=null) {
          shooting = false
          missile.x = player.x + (cellSize / 2) - 2
          missile.y = player.y
        }
      }

      if (pressedStart) {
        player.position(cellSize * 15, cellSize * 29)
      }
      if (pressedX && !shooting) {
        shooting = true
        coroutineContext.launchUnscoped {
          zap.play(PlaybackTimes(1))
        }
      }
    }

  }


}
