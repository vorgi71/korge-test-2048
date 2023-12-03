package omnipede

import korlibs.korge.view.*
import kotlin.math.*

class Spider(parent: Container, name: String) : Enemy(parent, name) {
  companion object {
    lateinit var spiderSprite: SpriteAnimation
  }

  private val direction: Int = if (random.nextBoolean()) {
    -1
  } else {
    1
  }
  private var xDir: Int = 0
  private var yDir: Int = 0
  private var moveCount = 0

  init {

    view = parent.image(spiderSprite[0]) {
      name(name)
      smoothing = false
      val xPos = if (direction == -1) {
        29 * LawnObject.cellSize
      } else {
        0
      }
      position(xPos, 27 * LawnObject.cellSize)
      scale = (LawnObject.cellSize / bitmap.height).toDouble()
      hitTestEnabled = true

      addUpdater {
        if (moveCount == 0) {
          yDir = if (random.nextBoolean()) -1 else 1
          xDir = if (random.nextBoolean()) direction else 0
        }
        x += xDir * LawnObject.cellSize / 6
        y += yDir * LawnObject.cellSize / 6

        if (y > Lawn.fieldHeight) {
          y = Lawn.fieldHeight.toDouble()
          yDir = -1
        }
        if (y < Lawn.fieldHeight * 2 / 3) {
          y = Lawn.fieldHeight * 2.0 / 3
          yDir = 1
        }
        moveCount++
        if (moveCount >= Lawn.cellSize * 2) {
          moveCount = 0
        }
        if (x + Lawn.cellSize > Lawn.fieldWith || x < 0) {
          LevelData.deSpawn(this@Spider)
          parent.removeChild(view)
        }
      }
    }
  }

  override fun getPoints(player: View): Int {
    val distance = abs(view.y - player.y) / Lawn.cellSize
    if (distance < 1) {
      return 1200
    }
    if (distance < 2) {
      return 900
    }
    if (distance < 4) {
      return 600
    }
    return 300
  }

  override fun deSpawn() {

  }
}
