package omnipede

import korlibs.datastructure.iterators.*
import korlibs.image.bitmap.*
import korlibs.korge.view.*
import kotlin.random.*

abstract class LawnObject(var x: Int, var y: Int, var viewObject: View) {
  abstract fun hit()

  companion object {
    lateinit var parent: Container
    var cellSize: Int = 16
  }

  var dumpChar = '.'

}

class Mushroom(x: Int, y: Int, viewObject: View, var damage: Int = 0) : LawnObject(x, y, viewObject) {
  companion object {
    lateinit var mushroomAnimation: SpriteAnimation
    fun mushroom(x: Int, y: Int): Mushroom {
      var damage = 0
      var mushroomView = parent.image(mushroomAnimation[0]) {
        smoothing = false
        scale = (cellSize / bitmap.width).toDouble()
        position(x * cellSize, y * cellSize)
        hitTestEnabled = true
      }
      return Mushroom(x, y, mushroomView, damage)
    }
  }

  init {
    dumpChar = 'P'
  }

  override fun hit() {
    damage++
    if (damage > 3) {
      Lawn.objects.remove(this)
      parent.removeChild(this.viewObject)
    } else {
      (viewObject as Image).bitmap = mushroomAnimation[damage]
    }
  }
}

class Flower(x: Int, y: Int, viewObject: View) : LawnObject(x, y, viewObject) {
  companion object {
    lateinit var flowerSprite: Bitmap
    fun flower(x: Int, y: Int): Flower {
      var flowerView = parent.image(flowerSprite) {
        smoothing = false
        scale = (cellSize / bitmap.width).toDouble()
        position(x * cellSize, y * cellSize)
        hitTestEnabled = true
      }
      return Flower(x, y, flowerView)
    }

  }

  init {
    dumpChar = 'F'
  }

  override fun hit() {

  }
}

object Lawn {
  var objects: MutableList<LawnObject> = mutableListOf()

  fun init(
    parent: Container,
    cellSize: Int,
    mushroomAnimation: SpriteAnimation,
    flowerSprite: Bitmap,
    mushroomCount: Int
  ) {
    LawnObject.parent = parent
    LawnObject.cellSize = cellSize
    Mushroom.mushroomAnimation = mushroomAnimation
    Flower.flowerSprite = flowerSprite


    val random = Random(0)
    for (index in 0..<mushroomCount) {
      val x = random.nextInt(30)
      val y = random.nextInt(30)

      if (get(x, y) == null) {
        objects.add(Mushroom.mushroom(x, y))
      }
    }
    dump()
  }

  fun get(x: Int, y: Int): LawnObject? {
    objects.fastForEach { lawnObject ->
      if (lawnObject.x == x && lawnObject.y == y) {
        return lawnObject
      }
    }
    return null
  }

  fun getViewObjects(): List<View> {
    val viewObjects = objects.map { it.viewObject }
    return viewObjects
  }

  fun dump() {
    for (y in 0..<32) {
      for (x in 0..<30) {
        print(get(x, y)?.dumpChar?:'.')
      }
      println()
    }
  }
}
