import com.soywiz.korev.Key
import com.soywiz.korge.*
import com.soywiz.korge.input.*
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

fun moveLeft(well: Well, t: Tetromino) {
    t.moveLeft()
    if (well.collision(t)) {
        t.moveRight()
    }
    t.draw()
}

fun moveRight(well: Well, t: Tetromino) {
    t.moveRight()
    if (well.collision(t)) {
        t.moveLeft()
    }
    t.draw()
}

fun rotateLeft(well: Well, t: Tetromino) {
    t.rotateLeft()
    if (well.collision(t)) {
        t.rotateRight()
    }
    t.draw()
}

fun rotateRight(well: Well, t: Tetromino) {
    t.rotateRight()
    if (well.collision(t)) {
        t.rotateLeft()
    }
    t.draw()
}

fun softDrop(well: Well, t: Tetromino) {
    t.moveDown()
    if (well.collision(t)) {
        t.moveUp()
    }
    t.draw()
}

fun hardDrop(well: Well, t: Tetromino) {
    while (!well.collision(t)) {
        t.moveDown()
    }
    t.moveUp()
    t.draw()
}

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    val images = Array(9) { resourcesVfs["chokkaku${it + 1}.png"].readBitmap() }

    val well = Well(images[0], 10, 20).addTo(this)
    var t = generateTetromino(images)

    keys.down {
        when (it.key) {
            Key.LEFT -> moveLeft(well, t)
            Key.RIGHT -> moveRight(well, t)
            Key.Z -> rotateLeft(well, t)
            Key.X -> rotateRight(well, t)
            Key.DOWN -> softDrop(well, t)
            Key.UP -> hardDrop(well, t)
            else -> Unit
        }
    }

    onClick {
        if (it.currentPosLocal.x > (well.width / 2) + well.x) {
            rotateRight(well, t)
        } else {
            rotateLeft(well, t)
        }
    }

    onSwipe(20.0) {
        when (it.direction) {
            SwipeDirection.LEFT -> moveLeft(well, t)
            SwipeDirection.RIGHT -> moveRight(well, t)
            SwipeDirection.TOP -> hardDrop(well, t)
            SwipeDirection.BOTTOM -> softDrop(well, t)
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