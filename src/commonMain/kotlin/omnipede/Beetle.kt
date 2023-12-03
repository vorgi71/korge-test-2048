package omnipede

import korlibs.korge.view.*
import omnipede.BeetleState.*
import kotlin.math.*


enum class BeetleState {
  DOWN,
  SIDE,
  UP,
  OUT
}

class Beetle(parent: Container, name: String) : Enemy(parent, name) {
  companion object {
    lateinit var beetleSprite: SpriteAnimation
  }

  private val direction: Int = if (random.nextBoolean()) {
    -1
  } else {
    1
  }
  private var beetleState = DOWN
  private var xDir: Int = 0
  private var yDir: Int = 0

  init {

    view = parent.image(beetleSprite[0]) {
      name(name)
      smoothing = false
      val xPos = if (direction == -1) {
        29 * LawnObject.cellSize
      } else {
        0
      }
      position(xPos, 25 * LawnObject.cellSize)
      scale = (LawnObject.cellSize / bitmap.height).toDouble()
      hitTestEnabled = true

      addUpdater {
        when (beetleState) {
          DOWN -> {
            xDir = 0
            yDir = 1
          }

          SIDE -> {
            xDir = direction
            yDir = 0
            if (random.nextInt(2000) < 8) {
              beetleState = UP
            }
          }

          UP -> {
            xDir = 0
            yDir = -1
            if (random.nextInt(2000) < 8) {
              beetleState = OUT
            }
          }

          OUT -> {
            xDir = direction
            yDir = 0
          }
        }

        println("beetleState: $beetleState")

        x += xDir * LawnObject.cellSize / 10
        y += yDir * LawnObject.cellSize / 10

        if (beetleState == DOWN && (y + Lawn.cellSize > Lawn.fieldHeight)) {
          y = Lawn.fieldHeight.toDouble()
          beetleState = SIDE
        }
        if (y < Lawn.fieldHeight * 2 / 3) {
          y = Lawn.fieldHeight * 2.0 / 3
          beetleState = OUT
        }

        var lawnPosition = Lawn.screenToLawn(x, y)
        val lawnObject = Lawn.get(lawnPosition)
        if(lawnObject is Mushroom) {
          Lawn.objects.remove(lawnObject)
          lawnObject.viewObject.removeFromParent()
          Lawn.objects.add(Flower.flower(lawnPosition.x,lawnPosition.y))
        }

        if (x + Lawn.cellSize > Lawn.fieldWith || x < 0) {
          LevelData.deSpawn(this@Beetle)
          parent.removeChild(view)
        }
      }
    }
  }

  override fun getPoints(player: View): Int {
    return 500
  }

  override fun deSpawn() {

  }
}
