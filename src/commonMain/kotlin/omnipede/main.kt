package omnipede

import korlibs.audio.sound.*
import korlibs.datastructure.iterators.*
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
  windowSize = Size(640, 800),
  title = "omnipede",
  backgroundColor = RGBA(25, 48, 20)
) {
  val sceneContainer = sceneContainer()
  sceneContainer.changeTo { StartScene() }
}

class StartScene : Scene() {
  override suspend fun SContainer.sceneMain() {
    var debugFont = resourcesVfs["clear_sans.fnt"].readBitmapFont()
    var font = resourcesVfs["PublicPixel.ttf"].readTtfFont()
    var zap = resourcesVfs["zap.wav"].readSound()

    var mushroomSpriteMap = resourcesVfs["Mushroom.png"].readBitmap()
    var mushroomAnimation = SpriteAnimation(mushroomSpriteMap, 8, 8, columns = 4)

    var playerSprite = resourcesVfs["Player.png"].readBitmap()
    var flowerSprite = resourcesVfs["Flower.png"].readBitmap()

    var spiderSpriteMap = resourcesVfs["Spider.png"].readBitmap()
    var spiderAnimation = SpriteAnimation(spiderSpriteMap, 16, 8)
    Spider.spiderSprite = spiderAnimation

    var cellSize = min(views.virtualWidth / 30.0, views.virtualHeight / 32.0).toInt()
    var fieldWidth = cellSize * 30
    var leftIndent = (views.virtualWidth - fieldWidth) / 2
    var fieldHeight = cellSize * 32
    var topIndent = (views.virtualHeight - fieldHeight) / 2

    val bgField = roundRect(Size(fieldWidth, fieldHeight), RectCorners(1.0), fill = Colors["#22151a"]) {
      position(leftIndent, topIndent)
      hitTestEnabled = false
    }

    val playerField = roundRect(Size(fieldWidth, 6 * cellSize), RectCorners(1.0), fill = Colors["#1a401d"]) {
      position(leftIndent, topIndent + 24 * cellSize)
      hitTestEnabled = false
    }

    var player = image(playerSprite) {
      smoothing = false
      scale = (cellSize.toDouble() / bitmap.width)
      position(15 * cellSize, 32 * cellSize)
      hitTestEnabled = true
    }

    var missile: RoundRect = roundRect(Size(4, cellSize), RectCorners(1), Colors.LIGHTCORAL) {
      position(leftIndent + cellSize * 15, topIndent + cellSize * 29)
      hitTestEnabled = true
    }

    Lawn.init(this, cellSize, fieldWidth, fieldHeight, mushroomAnimation, flowerSprite, 126)

    var infoText: Text =
      text("2048", cellSize * 1, Colors.WHITE, debugFont).position(leftIndent, topIndent + cellSize * 30)
    var scoreText: Text = text("0", cellSize * 1, Colors.WHITE, font).position(leftIndent, topIndent)

    LevelData.init(this)

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
      infoText.text = "leftIndent $leftIndent topIndent $topIndent cellSize $cellSize"
      player.x += pos.x * (cellSize / 2)
      player.y -= pos.y * (cellSize / 3)

      var playerHit = collisionWithViews(player, Lawn.getViewObjects(), 0.2 * cellSize)
      if (playerHit != null) {
        var tryCount = 8
        var xMove = -pos.x * (cellSize / 8)
        var yMove = pos.y * (cellSize / 16)
        while (tryCount-- > 0) {
          player.x += xMove
          player.y += yMove
          if (collisionWithViews(player, Lawn.getViewObjects(), 0.2 * cellSize) == null) {
            break
          }
        }
      }

      player.x = player.x.clamp(playerField.x, playerField.x + playerField.width - cellSize)

      player.y = player.y.clamp(playerField.y, playerField.y + playerField.height - cellSize)

      if (!shooting) {
        missile.x = player.x + (cellSize / 2) - 2
        missile.y = player.y
      } else {
        missile.y -= cellSize

        var missileDestroyed = false
        var missileHit = collisionWithViews(missile, Lawn.getViewObjects())

        if (missileHit != null) {
          val hitObject = Lawn.objects.find { lawnObject -> lawnObject.viewObject == missileHit }
          hitObject?.hit()
          missileDestroyed = true
        }

        missileHit = collisionWithViews(missile, LevelData.getEnemyViews())
        if (missileHit != null) {
          missileDestroyed = true
          missileHit.removeFromParent()

          println ("missile hit(${missileHit.name}")
          var enemy: Enemy? = missileHit.name?.let { name -> LevelData.getEnemy(name) }
          if (enemy != null) {
            val points = enemy.getPoints(player)
            if (points >= 300) {
              spawnPointAnimation(this, missile.x, missile.y, points)
            }
            LevelData.score += points
            scoreText.text = "${LevelData.score}"
            LevelData.despawn(enemy)
          }
        }

        if (missile.y < 0 || missileDestroyed) {
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

      LevelData.spawn(1)
    }
  }

  private fun spawnPointAnimation(parent: SContainer, xPos: Double, yPos: Double, points: Int) {
    var text = parent.text("$points", Lawn.cellSize * 0.8, color = Colors.WHITE, DefaultTtfFont)
      .position(xPos - Lawn.cellSize, yPos)
    var durationCount = 0
    text.addUpdater {
      color = color.withA(255 - durationCount * 3)
      y += 1
      if (durationCount++ > 50) {
        this.removeFromParent()
      }
    }
  }

  private fun collisionWithViews(view: View, otherViews: List<View>, inset: Double = 0.0): View? {
    var tempRect2 = view.getGlobalBounds()
    if (inset > 0.0) {
      tempRect2 = tempRect2.copy(
        tempRect2.x + inset,
        tempRect2.y + inset,
        tempRect2.width - 2 * inset,
        tempRect2.height - 2 * inset
      )
    }
    otherViews.fastForEach {
      val tempRect1 = it.getGlobalBounds()
      if (tempRect1.intersects(tempRect2)) {
        return it
      }
    }
    return null
  }
}
