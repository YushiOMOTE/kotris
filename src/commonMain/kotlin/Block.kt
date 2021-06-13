import com.soywiz.korge.view.Container
import com.soywiz.korge.view.image
import com.soywiz.korge.view.size
import com.soywiz.korim.bitmap.Bitmap


class Block(bitmap: Bitmap) : Container() {
    companion object {
        const val WIDTH = 20.0
        const val HEIGHT = 20.0
    }

    init {
        image(bitmap) {
            size(WIDTH, HEIGHT)
        }
    }
}
