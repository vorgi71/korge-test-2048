package my2048

import MainData
import korlibs.datastructure.*
import kotlin.random.*

data class Position(val x: Int, val y: Int)

class PositionMap(private val array: IntArray2 = IntArray2(4, 4, -1)) {
  fun getOrNull(x: Int, y: Int): Position? {
    if (array.get(x, y) != -1) {
      return Position(x, y)
    }
    return null
  }

  fun getNumber(x: Int, y: Int): Int = array.tryGet(x, y)?.let { id: Int ->
    MainData.tiles[id]?.tileNumber?.ordinal ?: -1
  } ?: -1

  fun getId(x: Int, y: Int): Int = array.get(x, y)

  fun setId(x: Int, y: Int, id: Int) {
    array.set(x, y, id)
  }

  override fun equals(other: Any?): Boolean {
    if (other is PositionMap) {
      return this.array.data.contentEquals(other.array.data)
    }
    return false
  }

  override fun hashCode(): Int {
    return this.array.data.hashCode()
  }

  fun freePositions(): List<Position> {
    val freePositions: MutableList<Position> = mutableListOf()
    array.each { x, y, id ->
      if (id == -1) {
        freePositions.add(Position(x, y))
      }
    }
    return freePositions
  }

  fun isFull(): Boolean {
    return array.count { id -> id == -1 } == 0
  }

  fun getRandomFreePosition(): Position? {
    val freePositions = freePositions()
    if (freePositions.isEmpty()) {
      return null
    }
    val chosenPositionIndex = Random.nextInt(freePositions.size)
    return freePositions[chosenPositionIndex]
  }
}
