import com.soywiz.korge.view.Container
import com.soywiz.korge.view.image
import com.soywiz.korge.view.position
import com.soywiz.korge.view.size
import com.soywiz.korim.bitmap.Bitmap

class Well(bitmap: Bitmap, private val num_x: Int, private val num_y: Int) : Container() {
    private val blockMap: BlockMap = BlockMap(num_x, num_y)

    init {
        // Draw side and bottom lines
        (0..num_x + 1).forEach { x ->
            (0..num_y).forEach { y ->
                if (x == 0 || x == num_x + 1 || y == num_y) {
                    image(bitmap) {
                        position(x * Block.WIDTH, y * Block.HEIGHT)
                        size(Block.WIDTH, Block.HEIGHT)
                    }
                }
            }
        }
    }

    fun collision(p: Tetromino): Boolean =
        p.blockMap.blocks().any { (x, y, pb) ->
            val wx = p.x + x
            val wy = p.y + y
            val wb = blockMap.get(wx, wy)
            if (wb != null && pb != null) {
                true
            } else if (pb != null) {
                wx < 0 || wx >= num_x || y < 0 || wy >= num_y
            } else {
                false
            }
        }

    fun merge(p: Tetromino) {
        p.blockMap.blocks().forEach { (x, y, pb) ->
            if (pb != null)
                blockMap.set(p.x + x, p.y + y, pb)
        }
    }

    fun clean(): Boolean {
        val (y, row) = blockMap.rows().find { (_, row) ->
            row.all { it != null }
        } ?: return false

        row.forEach { it?.removeFromParent() }

        (y downTo 1).forEach { yy ->
            blockMap.setRow(yy, blockMap.getRow(yy - 1))
        }

        return true
    }

    fun draw() {
        val baseX = (x + 1) * Block.WIDTH
        val baseY = y * Block.HEIGHT
        blockMap.draw(baseX, baseY)
    }
}
