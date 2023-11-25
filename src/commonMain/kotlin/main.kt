import korlibs.korge.*
import korlibs.korge.scene.*
import korlibs.korge.view.*
import korlibs.image.color.*
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

    val bgField = RoundRect(Size(fieldSize, fieldSize), RectCorners(5.0), fill = Colors["#b9aea0"])
    addChild(bgField)
  }
}
