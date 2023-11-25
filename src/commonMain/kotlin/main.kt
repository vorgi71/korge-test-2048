import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.image.color.*
import korlibs.image.font.*
import korlibs.image.format.*
import korlibs.image.text.*
import korlibs.io.file.std.*
import korlibs.korge.ui.*
import korlibs.korge.view.align.*
import korlibs.math.geom.*

suspend fun main() = Korge(
  windowSize = Size(480, 640),
  title = "2048",
  backgroundColor = RGBA(254, 247, 240)
) {
  val sceneContainer = sceneContainer()

  sceneContainer.changeTo { MyScene() }
}

class MyScene : Scene() {
  override suspend fun SContainer.sceneMain() {
    val cellSize = views.virtualWidth / 5.0
    val fieldSize = 50 + 4 * cellSize
    val leftIndent = (views.virtualWidth - fieldSize) / 2
    val topIndent = 150.0

    val bgField = roundRect(Size(fieldSize, fieldSize), RectCorners(5.0), fill = Colors["#b9aea0"]) {
      position(leftIndent, topIndent)
    }

    graphics {
      it.position(leftIndent, topIndent)
      fill(Colors["#a09e80"]) {
        for (y in 0..<4) {
          for (x in 0..<4) {
            roundRect(10.0 + (10 + cellSize) * x, 10.0 + (10 + cellSize) * y, cellSize, cellSize, 5.0, 5.0)
          }
        }
      }
    }

    val bgLogo = roundRect(Size(cellSize, cellSize), RectCorners(5.0), fill = Colors["#edc403"]) {
      position(leftIndent, 30.0)
    }

    val bgBest = roundRect(Size(cellSize * 1.5, cellSize * 0.8), RectCorners(5.0), fill = Colors["#bbae9e"]) {
      alignRightToRightOf(bgField)
      alignTopToTopOf(bgLogo)
    }
    val bgScore = roundRect(Size(cellSize * 1.5, cellSize * 0.8), RectCorners(5.0), fill = Colors["#bbae9e"]) {
      alignRightToLeftOf(bgBest, 24)
      alignTopToTopOf(bgBest)
    }

    val sansFont = resourcesVfs["clear_sans.fnt"].readBitmapFont()

    text("2048", cellSize*0.5, Colors.WHITE, sansFont).centerOn(bgLogo)
    text("BEST", cellSize*0.25, RGBA(239,226,210),sansFont) {
      centerXOn(bgBest)
      alignTopToTopOf(bgBest,5.0)
    }
    text("0",cellSize*0.5,Colors.WHITE,sansFont) {
      alignment = TextAlignment.MIDDLE_CENTER
      alignTopToTopOf(bgBest, 20.0)
      centerXOn(bgBest)
    }
    text("SCORE", cellSize*0.25, RGBA(239,226,210),sansFont) {
      centerXOn(bgScore)
      alignTopToTopOf(bgScore,5.0)
    }
    text("0",cellSize*0.5,Colors.WHITE,sansFont) {
      alignment = TextAlignment.MIDDLE_CENTER
      alignTopToTopOf(bgScore, 20.0)
      centerXOn(bgScore)
    }

    val btnSize = cellSize * 0.3

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

  }
}
