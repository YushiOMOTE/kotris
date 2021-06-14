import com.soywiz.korau.sound.*
import com.soywiz.korev.Key
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.*
import kotlinx.coroutines.delay
import kotlin.random.Random

fun Container.generateTetromino(images: Array<Bitmap>): Tetromino {
    val image = images[Random.nextInt(1, images.size)]

    return Tetromino(TetrominoType.values().random(), 3, 0) {
        Block(image).addTo(this)
    }
}

suspend fun main() = Korge(title = "Kotris", width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    val images = Array(9) { resourcesVfs["chokkaku${it + 1}.png"].readBitmap() }

    launchImmediately {
        val sound = resourcesVfs["bgm.mp3"].readSound()
        sound.play(PlaybackParameters(volume = 0.3, times = infinitePlaybackTimes))
    }

    val well = Well(images[0], 10, 20).addTo(this)
    val soundSet = SoundSet(
        land = resourcesVfs["land.wav"].readSound(),
        rotate = resourcesVfs["rotate.wav"].readSound(),
        blocked = resourcesVfs["blocked.wav"].readSound(),
        cleaned = resourcesVfs["cleaned.wav"].readSound()
    )

    val ctx = Context(well, soundSet) {
        generateTetromino(images)
    }

    keys.down {
        when (it.key) {
            Key.LEFT, Key.A -> ctx.moveLeft()
            Key.RIGHT, Key.D -> ctx.moveRight()
            Key.Z, Key.J -> ctx.rotateLeft()
            Key.X, Key.K -> ctx.rotateRight()
            Key.DOWN, Key.S -> ctx.softDrop()
            Key.UP, Key.W -> ctx.hardDrop()
            else -> Unit
        }
    }

    onClick {
        if (it.currentPosLocal.x > (well.width / 2) + well.x) {
            ctx.rotateRight()
        } else {
            ctx.rotateLeft()
        }
    }

    onSwipe(20.0) {
        when (it.direction) {
            SwipeDirection.LEFT -> ctx.moveLeft()
            SwipeDirection.RIGHT -> ctx.moveRight()
            SwipeDirection.TOP -> ctx.hardDrop()
            SwipeDirection.BOTTOM -> ctx.softDrop()
        }
    }

    while (true) {
        ctx.step()
    }
}