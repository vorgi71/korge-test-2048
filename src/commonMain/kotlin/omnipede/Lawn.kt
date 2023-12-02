package omnipede

import korlibs.image.bitmap.*
import korlibs.korge.view.*
import kotlin.random.*

open class LawnObject(var x: Int, var y: Int, viewObject: View) {
  companion object {
    lateinit var parent: Container
    var cellSize: Int = 16
  }
}

class Mushroom(x: Int, y: Int, viewObject: View, var damage: Int = 0) : LawnObject(x, y, viewObject) {
  companion object {
    lateinit var mushroomAnimation: SpriteAnimation
    fun mushroom(x:Int, y:Int): Mushroom {
      var damage = 0
      var mushroomView=parent.image(mushroomAnimation.get(damage)) {
        smoothing = false
        scale = (cellSize / bitmap.width).toDouble()
        position(x * cellSize, y * cellSize)
      }
      return Mushroom(x,y,mushroomView,damage)
    }
  }
}

class Flower(x: Int, y: Int, viewObject: View) : LawnObject(x, y, viewObject) {
  companion object {
    lateinit var flowerSprite: Bitmap
  }
}

object Lawn {
  var objects: MutableList<LawnObject> = mutableListOf()

  fun init(parent: Container, cellSize:Int, mushroomAnimation: SpriteAnimation, flowerSprite: Bitmap, mushroomCount: Int) {
    LawnObject.parent = parent
    LawnObject.cellSize = cellSize
    Mushroom.mushroomAnimation = mushroomAnimation
    Flower.flowerSprite = flowerSprite


    val random = Random(0)
    for (index in 0..<mushroomCount) {
      val x = random.nextInt(30)
      val y = random.nextInt(30) + 1

      objects.add(Mushroom.mushroom(x,y))
    }
  }
}
