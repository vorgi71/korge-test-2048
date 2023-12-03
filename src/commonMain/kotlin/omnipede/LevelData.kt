package omnipede

import korlibs.audio.sound.*
import korlibs.image.color.*
import korlibs.korge.view.*
import korlibs.korge.view.filter.*
import korlibs.time.*
import omnipede.BeetleState.*
import kotlin.coroutines.*
import kotlin.math.*
import kotlin.random.*
import kotlin.time.*

data class SpawnData(
  var enemyType: String,
  var spanProbability: Int,
  var maxSpawn: Int,
  var duration: Long = 0,
)

class LevelDataContainer(val colorFilter: List<Filter>, val spawnData: List<List<SpawnData>>) {
  fun getSpawnData(level: Int): List<SpawnData> {
    if (level < spawnData.size ) {
      return spawnData[level]
    }
    return spawnData[spawnData.size - 1]
  }

  fun getFilter(level: Int): Filter {
    return colorFilter[level % colorFilter.size]
  }
}

object LevelData {
  private lateinit var parent: Container

  private var levelData = listOf(
    listOf(
      SpawnData("Spider", 5, 1),
      SpawnData("Beetle", 3, 1),
      SpawnData("Millipede", 20_000, 1)
    ),
    listOf(
      SpawnData("Spider", 5, 1),
      SpawnData("Beetle", 5, 1),
      SpawnData("Millipede", 20_000, 1)
    ),
    listOf(
      SpawnData("Bee", 1_000, 8, 5000)
    )
  )

  private var filter= listOf(
    ColorMatrixFilter(ColorMatrixFilter.IDENTITY_MATRIX),
    ColorMatrixFilter(ColorMatrixFilter.SEPIA_MATRIX,0.5),
    ColorMatrixFilter(ColorMatrixFilter.GRAYSCALE_MATRIX,0.3),
    BlurFilter()
  )

  private var levelDataContainer = LevelDataContainer(filter, levelData)

  private var random = Random(0)

  private var enemyMap = mutableMapOf<String, MutableList<Enemy>>()

  var score: Int = 0

  fun init(parent: Container) {
    this.parent = parent
  }

  var startTime: DateTime = DateTime(0)
  var currentLevel = 1
  var spawnList = levelDataContainer.getSpawnData(currentLevel)

  fun startLevel(level: Int) {
    currentLevel = level
    spawnList = levelDataContainer.getSpawnData(level)
    startTime = DateTime.now()
  }

  fun spawn(): String? {
    val roll = random.nextInt(20_000)
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

      "Beetle" -> {
        return Beetle(parent, "${enemyType}_$spawnIndex")
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

  fun getLevelFilter(level: Int): Filter {
    return levelDataContainer.getFilter(level)
  }

  fun isLevelOver(level: Int): Boolean {
    var duration = 0L
    if (spawnList.any { spawnData ->
        duration = spawnData.duration
        duration > 0
      }) {
      if (startTime + duration.milliseconds < DateTime.now()) {
        return true
      }
    } else {
      if (!enemyMap.values.any { enemies -> enemies.any { enemy -> enemy is Millipede } }) {
        return true
      }
    }
    return false
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
