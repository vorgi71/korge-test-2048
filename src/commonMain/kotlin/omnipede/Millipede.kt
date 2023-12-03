package omnipede

import korlibs.audio.sound.*
import korlibs.korge.view.*
import omnipede.MillipedeState.*
import kotlin.math.*

enum class MillipedeState {
  MOVING,
  TURNING
}

class Millipede(parent: Container, name: String) : Enemy(parent, name) {
  companion object {
    lateinit var krabbler: Sound
    lateinit var millipedeSprite: SpriteAnimation
    var partCount = 0
    var spawnX = 0
  }

  private var isHead = false
  private var state = MOVING
  private var xDir = 1
  private var saveX = 0
  private var saveY = 0
  private var animationCount = 0

  init {
    if (partCount == 0) {
      spawnX = 10 + random.nextInt(20)
      isHead = true
      spawnPart()
      partCount++
      spawnX--
    } else {
      isHead = false
      spawnPart()
      partCount++
      spawnX--
    }
  }

  override fun getPoints(player: View): Int {
    return if (isHead) {
      20
    } else {
      10
    }
  }

  override fun deSpawn() {
    var lawnPosition = Lawn.screenToLawn(view.x, view.y)
    partCount--
    Lawn.objects.add(Mushroom.mushroom(lawnPosition.x, lawnPosition.y))
  }

  private fun spawnPart() {
    var spriteIndex = if (isHead) {
      0
    } else {
      4
    }
    view = parent.image(millipedeSprite[spriteIndex]) {
      name(this@Millipede.name)
      position(spawnX * Lawn.cellSize, 0)
      smoothing = false
      scale = (LawnObject.cellSize / bitmap.height).toDouble()
      hitTestEnabled = true

      addUpdater {
        when (state) {
          MOVING -> {
            x += xDir * 2
            if (animationCount++ > 8) {
              // alternate animation
              animationCount = 0
              if (spriteIndex % 2 == 0) {
                spriteIndex++
              } else {
                spriteIndex--
              }
              this.bitmap = millipedeSprite[spriteIndex]
            }

            if (abs(x - saveX) > Lawn.cellSize) {
              var lawnPosition = Lawn.screenToLawn(x, y)
              var nextObject = Lawn.get(lawnPosition.x + xDir, lawnPosition.y)
              var hitBound = false
              if (xDir > 0) {
                if (x > Lawn.fieldWith) {
                  hitBound = true
                }
              }
              if (xDir < 0) {
                if (x < 0) {
                  hitBound = true
                }
              }
              if (nextObject != null || hitBound) {
                x -= ((x + Lawn.cellSize / 2) % Lawn.cellSize)
                state = TURNING
                spriteIndex += xDir * 2
                this.bitmap = millipedeSprite[spriteIndex]
                xDir = -xDir
                saveY = y.toInt()
              }
            }
          }

          TURNING -> {
            y += 2
            if (abs(y - saveY) > Lawn.cellSize) {
              y -= y % Lawn.cellSize
              saveX = x.toInt()
              state = MOVING
            }
          }
        }
      }
    }
  }
}
