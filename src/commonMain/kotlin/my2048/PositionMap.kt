package my2048

import MainData
import korlibs.datastructure.*
import kotlin.random.*

data class Position(val x: Int, val y: Int)

class PositionMap(private val array: IntArray2 = IntArray2(4, 4, -1)) {
  constructor(positionMap: PositionMap) : this() {
    positionMap.array.each { x, y, v -> this.array[x, y] = v }
  }

  fun getOrNull(x: Int, y: Int): Position? {
    if (array.get(x, y) != -1) {
      return Position(x, y)
    }
    return null
  }

  fun getNumber(x: Int, y: Int): Int = array.tryGet(x, y)?.let { id: Int ->
    MainData.tiles[id]?.tileNumber?.ordinal ?: -1
  } ?: -1

  operator fun get(x: Int, y: Int): Int = array.get(x, y)

  operator fun set(x: Int, y: Int, id: Int) {
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

  fun getRandomFreePosition(): Position? {
    val quantity = array.count { it == -1 }
    if (quantity == 0) return null
    val chosen = Random.nextInt(quantity)
    var current = -1
    array.each { x, y, value ->
      if (value == -1) {
        current++
        if (current == chosen) {
          return Position(x, y)
        }
      }
    }
    return null
  }

  fun hasAvailableMoves(): Boolean {
    array.each { x, y, _ ->
      if (hasAdjacentEqualPosition(x, y)) return true
    }
    return false
  }

  private fun hasAdjacentEqualPosition(x: Int, y: Int) = getNumber(x, y).let {
    it == getNumber(x - 1, y) || it == getNumber(x + 1, y) || it == getNumber(x, y - 1) || it == getNumber(x, y + 1)
  }

  fun copy(): PositionMap {
    return PositionMap(this)
  }

  fun calculateNewMap(
    map: PositionMap,
    direction: Direction,
    moves: MutableList<Pair<Int, Position>>,
    merges: MutableList<Triple<Int, Int, Position>>
  ): PositionMap {
    val newMap = PositionMap()
    val startIndex = when (direction) {
      Direction.LEFT, Direction.TOP -> 0
      Direction.RIGHT, Direction.BOTTOM -> 3
    }
    var columnRow = startIndex

    fun newPosition(line: Int) = when (direction) {
      Direction.LEFT -> Position(columnRow++, line)
      Direction.RIGHT -> Position(columnRow--, line)
      Direction.TOP -> Position(line, columnRow++)
      Direction.BOTTOM -> Position(line, columnRow--)
    }

    for (line in 0..3) {
      var curPos = map.getNotEmptyPositionFrom(direction, line)
      columnRow = startIndex
      while (curPos != null) {
        val newPos = newPosition(line)
        val curId = map[curPos.x, curPos.y]
        map[curPos.x, curPos.y] = -1

        val nextPos = map.getNotEmptyPositionFrom(direction, line)
        val nextId = nextPos?.let { map[it.x, it.y] }
        //two blocks are equal
        if (nextId != null && MainData.tileNumberFor(curId) == MainData.tileNumberFor(nextId)) {
          //merge these blocks
          map[nextPos.x, nextPos.y] = -1
          newMap[newPos.x, newPos.y] = curId
          merges += Triple(curId, nextId, newPos)
        } else {
          //add old block
          newMap[newPos.x, newPos.y] = curId
          moves += Pair(curId, newPos)
        }
        curPos = map.getNotEmptyPositionFrom(direction, line)
      }
    }
    return newMap
  }

  fun getNotEmptyPositionFrom(direction: Direction, line: Int): Position? {
    when (direction) {
      Direction.LEFT -> for (i in 0..3) getOrNull(i, line)?.let { return it }
      Direction.RIGHT -> for (i in 3 downTo 0) getOrNull(i, line)?.let { return it }
      Direction.TOP -> for (i in 0..3) getOrNull(line, i)?.let { return it }
      Direction.BOTTOM -> for (i in 3 downTo 0) getOrNull(line, i)?.let { return it }
    }
    return null
  }
}

