import com.soywiz.korau.sound.*
import com.soywiz.korev.Key
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.ui.textAlignment
import com.soywiz.korge.ui.uiText
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korim.text.HorizontalAlign
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korim.text.VerticalAlign
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.*

suspend fun Container.run(ctx: Context) {
    while (ctx.step()) {
    }
    gameover(ctx) {
        ctx.clear()
        run(ctx)
    }
}

fun Container.gameover(ctx: Context, onRestart: suspend () -> Unit) =
    container {
        suspend fun restart() {
            this@container.removeFromParent()
            onRestart()
        }

        var title = text("Game Over") {
            centerXOn(this@container)
            centerYOn(this@container)
        }

        text("Try again") {
            alignTopToBottomOf(title)
            onClick {
                restart()
            }
        }

        centerOn(ctx.well)
    }

suspend fun main() = Korge(title = "Kotris", width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    launchImmediately {
        val sound = resourcesVfs["bgm.mp3"].readSound()
        sound.play(PlaybackParameters(volume = 0.3, times = infinitePlaybackTimes))
    }

    val images = Array(9) { resourcesVfs["chokkaku${it + 1}.png"].readBitmap() }
    val soundSet = SoundSet(
        land = resourcesVfs["land.wav"].readSound(),
        rotate = resourcesVfs["rotate.wav"].readSound(),
        blocked = resourcesVfs["blocked.wav"].readSound(),
        cleaned = resourcesVfs["cleaned.wav"].readSound()
    )

    val well = Well(images[0], 10, 20).also {
        it.centerXOn(this)
        it.positionY(30.0)
        it.addTo(this)
    }

    val ctx = Context(
        well,
        soundSet,
        images
    )

    text(ctx.score.value.toString()) {
        centerXOn(well)
        alignBottomToTopOf(well)
        ctx.score.observe { text = it.toString() }
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

    run(ctx)
}