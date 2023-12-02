package omnipede

import korlibs.korge.view.*
import kotlin.random.*

data class SpawnData(var enemyType: String, var spanProbability: Int, var maxSpawn: Int)

object LevelData {
  lateinit var parent: Container

  var levelData = mapOf(1 to listOf(SpawnData("Spider", 1000, 10)))

  var random = Random(0)

  var enemyMap = mutableMapOf<String, MutableMap<String, View>>()

  fun init(parent: Container) {
    this.parent = parent
  }

  fun spawn(level: Int): String? {
    var spawnList = levelData[level]
    var roll = random.nextInt(10_000)
    spawnList?.forEach { spawnData ->
      var spawnCount = enemyMap[spawnData.enemyType]?.size ?: 0
      if (roll <= spawnData.spanProbability && spawnCount < spawnData.maxSpawn) {
        var enemyTypeMap: MutableMap<String, View>?
        enemyTypeMap = enemyMap.get(spawnData.enemyType)
        if (enemyTypeMap == null) {
          enemyTypeMap = mutableMapOf()
          enemyMap.put(spawnData.enemyType, enemyTypeMap)
        }
        spawnCount++
        enemyTypeMap["${spawnData.enemyType}_${spawnCount}"] = createEnemy(spawnData.enemyType, spawnCount)
        return spawnData.enemyType
      }
    }
    return null
  }

  fun despawn(objectName: String) {
    val split = objectName.split("_")
    val enemyType = split[0]
    val enemyIndex = split[1]
    enemyMap[enemyType]?.remove("${enemyType}_${enemyIndex}")
  }

  fun createEnemy(enemyType: String, spawnIndex: Int): View {
    when (enemyType) {
      "Spider" -> {
        return Spider(parent, "${enemyType}_$spawnIndex").view
      }
    }
    return Spider(parent, "Spider_$spawnIndex").view
  }

  fun getEnemies(): List<View> {
    var enemies: MutableList<View> = mutableListOf()
    enemyMap.values.forEach { enemyTypeMap ->
      enemyTypeMap.values.forEach { view ->
        enemies.add(view)
      }
    }
    return enemies
  }
}

class Spider(parent: Container, name: String) {
  companion object {
    lateinit var spiderSprite: SpriteAnimation
    var random = Random(0)
  }

  val direction: Int
  var xDir: Int = 0
  var yDir: Int = 0
  var moveCount = 0

  val view: View

  init {

    if (random.nextBoolean()) {
      direction = -1
    } else {
      direction = 1
    }
    view = parent.image(spiderSprite[0]) {
      smoothing = false
      val xPos = if (direction == -1) {
        29 * LawnObject.cellSize
      } else {
        0
      }
      println("spawning spider at ${xPos} ${36 * LawnObject.cellSize}")
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
        moveCount++
        if (moveCount >= LawnObject.cellSize) {
          moveCount = 0
        }
        if (x > LawnObject.cellSize * 30 || x < 0) {
          LevelData.despawn(name)
          parent.removeChild(this)
        }
      }
    }

  }


}
