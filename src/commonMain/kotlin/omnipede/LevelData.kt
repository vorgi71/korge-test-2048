package omnipede

import korlibs.audio.sound.*
import korlibs.korge.view.*
import omnipede.BeetleState.*
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
      SpawnData("Spider", 5, 1),
      SpawnData("Beetle", 15000, 1),
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
        return Beetle(parent,"${enemyType}_$spawnIndex")
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
