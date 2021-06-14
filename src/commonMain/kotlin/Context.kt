import com.soywiz.korau.sound.Sound
import kotlinx.coroutines.delay

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

    fun softDrop() {
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
                delay(400)
                well.draw()
            }
            well.draw()
            tet = spawn()
        }
    }
}
