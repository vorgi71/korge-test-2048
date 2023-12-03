package omnipede

import korlibs.audio.sound.*
import korlibs.korge.view.*
import omnipede.MillipedeState.MOVING
import omnipede.MillipedeState.TURNING
import kotlin.coroutines.*
import kotlin.math.*
import kotlin.random.*

data class SpawnData(var enemyType: String, var spanProbability: Int, var maxSpawn: Int)

object LevelData {
  private lateinit var parent: Container

  private var levelData = mapOf(
    1 to listOf(
      SpawnData("Spider", 50, 1),
      SpawnData("Millipede", 10_000, 1)
    )
  )

  private var random = Random(0)

  private var enemyMap = mutableMapOf<String, MutableList<Enemy>>()

  var score: Int = 0

  fun init(parent: Container) {
    this.parent = parent
  }

  fun spawn(level: Int): String? {
    val spawnList = levelData[level]
    val roll = random.nextInt(10_000)
    spawnList?.forEach { spawnData ->
      var spawnCount = enemyMap[spawnData.enemyType]?.size ?: 0
      if (roll <= spawnData.spanProbability && spawnCount < spawnData.maxSpawn) {
        var enemyList: MutableList<Enemy>?
        enemyList = enemyMap[spawnData.enemyType]
        if (enemyList == null) {
          enemyList = mutableListOf()
          enemyMap[spawnData.enemyType] = enemyList
        }
        spawnCount++
        enemyList += createEnemy(spawnData.enemyType, spawnCount)
        return spawnData.enemyType
      }
    }
    return null
  }

  fun deSpawn(enemy: Enemy) {
    val split = enemy.name.split("_")
    val enemyType = split.first()
    enemy.deSpawn()
    enemyMap[enemyType]?.remove(enemy)
  }

  fun getEnemy(name: String): Enemy? {
    val split = name.split("_")
    val enemyType = split.first()
    return enemyMap[enemyType]?.find { enemy: Enemy -> enemy.name == name }
  }

  private fun createEnemy(enemyType: String, spawnIndex: Int): Enemy {
    when (enemyType) {
      "Spider" -> {
        return Spider(parent, "${enemyType}_$spawnIndex")
      }

      "Millipede" -> {
        var internalSpawnIndex = spawnIndex
        for (d in 1..<10) {
          enemyMap[enemyType]?.add(
            Millipede(parent, "${enemyType}_${internalSpawnIndex++}")
          )
        }
        return Millipede(parent, "${enemyType}_${internalSpawnIndex}")
      }
    }
    return Spider(parent, "Spider_$spawnIndex")
  }

  fun getEnemyViews(): List<View> {
    val enemies: MutableList<View> = mutableListOf()
    enemyMap.values.forEach { enemyTypeMap ->
      enemyTypeMap.forEach { enemy ->
        enemies.add(enemy.view)
      }
    }
    return enemies
  }
}

abstract class Enemy(val parent: Container, val name: String) {
  companion object {
    var random = Random(0)
  }

  lateinit var view: View

  abstract fun getPoints(player: View): Int
  abstract fun deSpawn()
}

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
