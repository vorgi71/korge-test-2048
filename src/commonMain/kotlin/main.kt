import korlibs.event.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.image.format.*
import korlibs.image.text.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.korge.*
import korlibs.korge.animate.*
import korlibs.korge.input.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.math.geom.*
import korlibs.math.interpolation.*
import my2048.*
import kotlin.properties.*
import kotlin.random.*
import kotlin.time.Duration.Companion.seconds

suspend fun main() = Korge(
  windowSize = Size(480, 640),
  title = "2048",
  backgroundColor = RGBA(254, 247, 240)
) {
  val sceneContainer = sceneContainer()

  sceneContainer.changeTo { MyScene() }
}

object MainData {
  var cellSize = 0.0
  var fieldSize = 0.0
  var leftIndent = 0.0
  var topIndent = 0.0
  var font: BitmapFont by Delegates.notNull()

  var isAnimationRunning = false
  var isGameOver = false

  val tiles = mutableMapOf<Int, Tile>()
  private var freeId = 0
  var positionMap = PositionMap()

  fun xOffset(x: Int) = leftIndent + 10.0 + (10 + cellSize) * x
  fun yOffset(y: Int) = topIndent + 10.0 + (10 + cellSize) * y
  fun getFreeId(): Int {
    return freeId++
  }

  fun tileNumberFor(id: Int): TileNumber? {
    return tiles[id]?.tileNumber
  }

  fun deleteTile(blockId: Int) = tiles.remove(blockId)!!.removeFromParent()
}

class MyScene : Scene() {

  private val md = MainData

  override suspend fun SContainer.sceneMain() {
    md.cellSize = views.virtualWidth / 5.0
    md.fieldSize = 50 + 4 * md.cellSize
    md.leftIndent = (views.virtualWidth - md.fieldSize) / 2
    md.topIndent = 150.0

    val bgField = roundRect(Size(md.fieldSize, md.fieldSize), RectCorners(5.0), fill = Colors["#b9aea0"]) {
      position(md.leftIndent, md.topIndent)
    }

    graphics {
      it.position(md.leftIndent, md.topIndent)
      fill(Colors["#a09e80"]) {
        for (y in 0..<4) {
          for (x in 0..<4) {
            roundRect(10.0 + (10 + md.cellSize) * x, 10.0 + (10 + md.cellSize) * y, md.cellSize, md.cellSize, 5.0, 5.0)
          }
        }
      }
    }

    val bgLogo = roundRect(Size(md.cellSize, md.cellSize), RectCorners(5.0), fill = Colors["#edc403"]) {
      position(md.leftIndent, 30.0)
    }

    val bgBest = roundRect(Size(md.cellSize * 1.5, md.cellSize * 0.8), RectCorners(5.0), fill = Colors["#bbae9e"]) {
      alignRightToRightOf(bgField)
      alignTopToTopOf(bgLogo)
    }
    val bgScore = roundRect(Size(md.cellSize * 1.5, md.cellSize * 0.8), RectCorners(5.0), fill = Colors["#bbae9e"]) {
      alignRightToLeftOf(bgBest, 24)
      alignTopToTopOf(bgBest)
    }

    md.font = resourcesVfs["clear_sans.fnt"].readBitmapFont()

    text("2048", md.cellSize * 0.5, Colors.WHITE, md.font).centerOn(bgLogo)
    text("BEST", md.cellSize * 0.25, RGBA(239, 226, 210), md.font) {
      centerXOn(bgBest)
      alignTopToTopOf(bgBest, 5.0)
    }
    text("0", md.cellSize * 0.5, Colors.WHITE, md.font) {
      alignment = TextAlignment.MIDDLE_CENTER
      alignTopToTopOf(bgBest, 20.0)
      centerXOn(bgBest)
    }
    text("SCORE", md.cellSize * 0.25, RGBA(239, 226, 210), md.font) {
      centerXOn(bgScore)
      alignTopToTopOf(bgScore, 5.0)
    }
    text("0", md.cellSize * 0.5, Colors.WHITE, md.font) {
      alignment = TextAlignment.MIDDLE_CENTER
      alignTopToTopOf(bgScore, 20.0)
      centerXOn(bgScore)
    }

    val btnSize = md.cellSize * 0.3

    val restartImg = resourcesVfs["restart.png"].readBitmap()
    val undoImg = resourcesVfs["undo.png"].readBitmap()

    val restartBlock = container {
      val background = roundRect(Size(btnSize, btnSize), RectCorners(5.0), fill = RGBA(185, 174, 160))
      image(restartImg) {
        size(btnSize * 0.8, btnSize * 0.8)
        centerOn(background)
      }
      alignTopToBottomOf(bgBest, 5)
      alignRightToRightOf(bgField)

      onOver {
        background.color = Colors.DARKKHAKI
      }
      onOut {
        background.color = RGBA(185, 174, 160)
      }
    }

    restartBlock.onClick {
      restart()
    }

    val undoBlock = container {
      val background = roundRect(Size(btnSize, btnSize), RectCorners(5.0), fill = RGBA(185, 174, 160))
      image(undoImg) {
        size(btnSize * 0.6, btnSize * 0.6)
        centerOn(background)
      }
      alignTopToTopOf(restartBlock)
      alignRightToLeftOf(restartBlock, 5.0)
    }

    generateBlock()

    keys {
      down {
        when (it.key) {
          Key.LEFT, Key.A -> stage?.moveTilesTo(Direction.LEFT)
          Key.RIGHT, Key.D -> stage?.moveTilesTo(Direction.RIGHT)
          Key.UP, Key.W -> stage?.moveTilesTo(Direction.TOP)
          Key.DOWN, Key.S -> stage?.moveTilesTo(Direction.BOTTOM)
          else -> Unit
        }
      }
    }

    onSwipe(20.0) {
      when (it.direction) {
        SwipeDirection.LEFT -> stage?.moveTilesTo(Direction.LEFT)
        SwipeDirection.RIGHT -> stage?.moveTilesTo(Direction.RIGHT)
        SwipeDirection.TOP -> stage?.moveTilesTo(Direction.TOP)
        SwipeDirection.BOTTOM -> stage?.moveTilesTo(Direction.BOTTOM)
      }
    }

  }
}

fun Container.createTileWithId(id: Int, tileNumber: TileNumber, pos: Position) {
  MainData.tiles[id] = tile(tileNumber) {
    position(MainData.xOffset(pos.x), MainData.yOffset(pos.y))
  }
}

fun Container.createTile(tileNumber: TileNumber, pos: Position): Int {
  val id = MainData.getFreeId()
  createTileWithId(id, tileNumber, pos)
  return id
}

fun Container.generateBlock() {
  val freePosition = MainData.positionMap.getRandomFreePosition() ?: return
  val number = if (Random.nextDouble() < 0.9) TileNumber.ZERO else TileNumber.ONE
  val newId = createTile(number, freePosition)
  MainData.positionMap[freePosition.x, freePosition.y] = newId
}

fun Stage.moveTilesTo(direction: Direction) {
  if (MainData.isAnimationRunning) {
    return
  }
  if (!MainData.positionMap.hasAvailableMoves()) {
    if (MainData.isGameOver) {
      return
    }
    MainData.isGameOver = true
    showGameOver {
      MainData.isGameOver = false
      restart()
    }
  }

  val moves = mutableListOf<Pair<Int, Position>>()
  val merges = mutableListOf<Triple<Int, Int, Position>>()

  val newMap = MainData.positionMap.calculateNewMap(MainData.positionMap.copy(), direction, moves, merges)

  if (MainData.positionMap != newMap) {
    MainData.isAnimationRunning = true
    showAnimation(moves, merges) {
      // when animation ends
      MainData.positionMap = newMap
      generateBlock()
      MainData.isAnimationRunning = false
    }
    MainData.isAnimationRunning = false
  }
}

fun Stage.showAnimation(
  moves: List<Pair<Int, Position>>,
  merges: List<Triple<Int, Int, Position>>,
  onEnd: () -> Unit
) {
  launchImmediately {
    animate {
      parallel {
        moves.forEach { (id, pos) ->
          val tile:Tile= MainData.tiles[id]!!
          moveTo(tile,MainData.xOffset(pos.x),MainData.yOffset(pos.y), 0.15.seconds, Easing.LINEAR)
        }
        merges.forEach { (id1, id2, pos) ->
          sequence {
            parallel {
              moveTo(MainData.tiles[id1]!!,MainData.xOffset(pos.x),MainData.yOffset(pos.y), 0.15.seconds, Easing.LINEAR)
              moveTo(MainData.tiles[id2]!!,MainData.xOffset(pos.x),MainData.yOffset(pos.y), 0.15.seconds, Easing.LINEAR)
            }
            block {
              val nextNumber = MainData.tileNumberFor(id1)!!.next()
              MainData.deleteTile(id1)
              MainData.deleteTile(id2)
              createTileWithId(id1, nextNumber, pos)
            }
            sequenceLazy {
              scaleTo(MainData.tiles[id1]!!,1.0,1.0)
            }
          }
        }
      }
      block {
        onEnd()
      }
    }
  }
}


fun Container.showGameOver(onRestart: () -> Unit) = container {
  fun restart() {
    this@container.removeFromParent()
    onRestart()
  }

  position(MainData.leftIndent, MainData.topIndent)

  roundRect(Size(MainData.fieldSize, MainData.fieldSize), RectCorners(5.0), fill = Colors["#FFFFFF33"])
  text("Game Over", 60.0, Colors.BLACK, MainData.font) {
    centerBetween(0.0, 0.0, MainData.fieldSize, MainData.fieldSize)
    y -= 60
  }
  text("Try again", 40.0, font = MainData.font) {
    centerBetween(0.0, 0.0, MainData.fieldSize, MainData.fieldSize)
    y += 20
    textSize = 40.0
    color = RGBA(0, 0, 0)
    onOver { color = RGBA(90, 90, 90) }
    onOut { color = RGBA(0, 0, 0) }
    onDown { color = RGBA(120, 120, 120) }
    onUp { color = RGBA(120, 120, 120) }
    onClick { restart() }
  }

  keys.down {
    when (it.key) {
      Key.ENTER, Key.SPACE -> restart()
      else -> Unit
    }
  }
}

fun Container.restart() {
  println("in restart()")
  MainData.positionMap = PositionMap()
  MainData.tiles.values.forEach { it.removeFromParent() }
  MainData.tiles.clear()
  generateBlock()
}
