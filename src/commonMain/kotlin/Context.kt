import com.soywiz.korau.sound.Sound
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.position
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korio.async.ObservableProperty
import kotlinx.coroutines.delay
import kotlin.random.Random

data class SoundSet(val land: Sound, val rotate: Sound, val blocked: Sound, val cleaned: Sound)

class Context(
    val well: Well, private val sounds: SoundSet, private val images: Array<Bitmap>
) {
    private var tet: Tetromino = spawn()
    val score = ObservableProperty(0)
    var bonus = 1
    var running = true

    private fun spawn(): Tetromino {
        val image = images[Random.nextInt(1, images.size)]

        return Tetromino(TetrominoType.values().random(), 3, 0) {
            Block(image).addTo(well)
        }
    }

    suspend fun moveLeft() {
        if (!running) {
            return
        }
        sounds.rotate.play()
        tet.moveLeft()
        if (well.collision(tet)) {
            sounds.blocked.play()
            tet.moveRight()
        }
        tet.draw()
    }

    suspend fun moveRight() {
        if (!running) {
            return
        }
        sounds.rotate.play()
        tet.moveRight()
        if (well.collision(tet)) {
            sounds.blocked.play()
            tet.moveLeft()
        }
        tet.draw()
    }

    suspend fun rotateLeft() {
        if (!running) {
            return
        }
        sounds.rotate.play()
        tet.rotateLeft()
        if (well.collision(tet)) {
            sounds.blocked.play()
            tet.rotateRight()
        }
        tet.draw()
    }

    suspend fun rotateRight() {
        if (!running) {
            return
        }
        sounds.rotate.play()
        tet.rotateRight()
        if (well.collision(tet)) {
            sounds.blocked.play()
            tet.rotateLeft()
        }
        tet.draw()
    }

    fun softDrop() {
        if (!running) {
            return
        }
        tet.moveDown()
        if (well.collision(tet)) {
            tet.moveUp()
        }
        tet.draw()
    }

    suspend fun hardDrop() {
        if (!running) {
            return
        }
        sounds.rotate.play()
        while (!well.collision(tet)) {
            tet.moveDown()
        }
        tet.moveUp()
        tet.draw()
    }

    suspend fun step(): Boolean {
        tet.moveDown()
        if (well.collision(tet)) {
            sounds.land.play()
            tet.moveUp()
            well.merge(tet)
            while (well.clean()) {
                score.value += bonus * 10
                bonus *= 2
                sounds.cleaned.play()
                delay(400)
                well.draw()
            }
            bonus = 1
            well.draw()
            tet = spawn()
            if (well.collision(tet)) {
                running = false
                return false
            }
        } else {
            tet.draw()
            delay(1000)
        }
        return true
    }

    fun clear() {
        tet.clear()
        well.clear()
        tet = spawn()
        running = true
    }
}
