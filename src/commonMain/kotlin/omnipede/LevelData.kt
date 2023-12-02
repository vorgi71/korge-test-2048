package omnipede

import korlibs.korge.view.*
import kotlin.random.*

data class SpawnData(var enemyType: String, var spanProbability: Int, var maxSpawn: Int)

object LevelData {
  lateinit var parent: Container

  var levelData = mapOf(1 to listOf(SpawnData("Spider", 50, 1)))

  var random = Random(0)

  var enemyMap = mutableMapOf<String, MutableList<Enemy>>()

  fun init(parent: Container) {
    this.parent = parent
  }

  fun spawn(level: Int): String? {
    var spawnList = levelData[level]
    var roll = random.nextInt(10_000)
    spawnList?.forEach { spawnData ->
      var spawnCount = enemyMap[spawnData.enemyType]?.size ?: 0
      if (roll <= spawnData.spanProbability && spawnCount < spawnData.maxSpawn) {
        var enemyList: MutableList<Enemy>?
        enemyList = enemyMap[spawnData.enemyType]
        if (enemyList == null) {
          enemyList = mutableListOf()
          enemyMap.put(spawnData.enemyType, enemyList)
        }
        spawnCount++
        enemyList += createEnemy(spawnData.enemyType, spawnCount)
        return spawnData.enemyType
      }
    }
    return null
  }

  fun despawn(enemy: Enemy) {
    val split = enemy.name.split("_")
    val enemyType = split.first()
    enemyMap[enemyType]?.remove(enemy)
  }

  fun getEnemy(name: String): Enemy? {
    val split = name.split("_")
    val enemyType = split.first()
    return enemyMap[enemyType]?.find { enemy: Enemy -> enemy.name == name }
  }

  fun createEnemy(enemyType: String, spawnIndex: Int): Enemy {
    when (enemyType) {
      "Spider" -> {
        return Spider(parent, "${enemyType}_$spawnIndex")
      }
    }
    return Spider(parent, "Spider_$spawnIndex")
  }

  fun getEnemyViews(): List<View> {
    var enemies: MutableList<View> = mutableListOf()
    enemyMap.values.forEach { enemyTypeMap ->
      enemyTypeMap.forEach { enemy ->
        enemies.add(enemy.view)
      }
    }
    return enemies
  }
}

open class Enemy(val parent: Container, val name: String) {
  lateinit var view: View
}

class Spider(parent: Container, name: String) : Enemy(parent, name) {
  companion object {
    lateinit var spiderSprite: SpriteAnimation
    var random = Random(0)
  }

  val direction: Int
  var xDir: Int = 0
  var yDir: Int = 0
  var moveCount = 0

  init {

    if (random.nextBoolean()) {
      direction = -1
    } else {
      direction = 1
    }
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
          if (random.nextBoolean()) yDir = -1 else yDir = 1
          if (random.nextBoolean()) xDir = direction else xDir = 0
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
        if (x+Lawn.cellSize > Lawn.fieldWith || x < 0) {
          LevelData.despawn(this@Spider)
          parent.removeChild(view)
        }
      }
    }
  }
}
