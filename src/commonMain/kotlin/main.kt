import com.soywiz.korev.Key
import com.soywiz.korge.*
import com.soywiz.korge.input.keys
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import kotlinx.coroutines.delay
import kotlin.random.Random

fun Container.generateTetromino(images: Array<Bitmap>): Tetromino {
    val image = images[Random.nextInt(0, images.size)]

    return Tetromino(TetrominoType.values().random(), 3, 0) {
        Block(image).addTo(this)
    }
}

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    val images = Array(9) { resourcesVfs["chokkaku${it + 1}.png"].readBitmap() }

    val well = Well(images[0], 10, 20).addTo(this)
    var t = generateTetromino(images)

    keys.down {
        when (it.key) {
            Key.LEFT -> {
                t.moveLeft()
                if (well.collision(t)) {
                    t.moveRight()
                }
                t.draw()
            }
            Key.RIGHT -> {
                t.moveRight()
                if (well.collision(t)) {
                    t.moveLeft()
                }
                t.draw()
            }
            Key.Z -> {
                t.rotateLeft()
                if (well.collision(t)) {
                    t.rotateRight()
                }
                t.draw()
            }
            Key.X -> {
                t.rotateRight()
                if (well.collision(t)) {
                    t.rotateLeft()
                }
                t.draw()
            }
            Key.DOWN -> {
                t.moveDown()
                if (well.collision(t)) {
                    t.moveUp()
                }
                t.draw()
            }
            Key.UP -> {
                while (!well.collision(t)) {
                    t.moveDown()
                }
                t.moveUp()
                t.draw()
            }
            else -> Unit
        }
    }

    while (true) {
        t.draw()
        delay(1000)
        t.moveDown()
        if (well.collision(t)) {
            t.moveUp()
            well.merge(t)
            while (well.clean()) {
                well.draw()
            }
            well.draw()
            t = generateTetromino(images)
        }
    }
}