import com.soywiz.korau.sound.*
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
    val image = images[Random.nextInt(1, images.size)]

    return Tetromino(TetrominoType.values().random(), 3, 0) {
        Block(image).addTo(this)
    }
}

data class SoundSet(val land: Sound, val rotate: Sound, val blocked: Sound, val cleaned: Sound)

class Context(private val well: Well, private val sounds: SoundSet, private val spawn: () -> Tetromino) {
    private var tet: Tetromino = spawn()

    suspend fun moveLeft() {
        sounds.rotate.play()
        tet.moveLeft()
        if (well.collision(tet)) {
            sounds.blocked.play()
            tet.moveRight()
        }
        tet.draw()
    }

    suspend fun moveRight() {
        sounds.rotate.play()
        tet.moveRight()
        if (well.collision(tet)) {
            sounds.blocked.play()
            tet.moveLeft()
        }
        tet.draw()
    }

    suspend fun rotateLeft() {
        sounds.rotate.play()
        tet.rotateLeft()
        if (well.collision(tet)) {
            sounds.blocked.play()
            tet.rotateRight()
        }
        tet.draw()
    }

    suspend fun rotateRight() {
        sounds.rotate.play()
        tet.rotateRight()
        if (well.collision(tet)) {
            sounds.blocked.play()
            tet.rotateLeft()
        }
        tet.draw()
    }

    suspend fun softDrop() {
        tet.moveDown()
        if (well.collision(tet)) {
            tet.moveUp()
        }
        tet.draw()
    }

    suspend fun hardDrop() {
        sounds.rotate.play()
        while (!well.collision(tet)) {
            tet.moveDown()
        }
        tet.moveUp()
        tet.draw()
    }

    suspend fun step() {
        tet.draw()
        delay(1000)
        tet.moveDown()
        if (well.collision(tet)) {
            sounds.land.play()
            tet.moveUp()
            well.merge(tet)
            while (well.clean()) {
                sounds.cleaned.play()
                well.draw()
            }
            well.draw()
            tet = spawn()
        }
    }
}

suspend fun main() = Korge(title = "Kotris", width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    val images = Array(9) { resourcesVfs["chokkaku${it + 1}.png"].readBitmap() }
    val bgm = resourcesVfs["bgm.mp3"].readSound()
    bgm.play(PlaybackParameters(volume = 0.3, times = infinitePlaybackTimes))

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