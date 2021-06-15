import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap

class Well(bitmap: Bitmap, private val num_x: Int, private val num_y: Int) :
    Container() {
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

    fun collision(t: Tetromino): Boolean =
        t.blockMap.blocks()
            .any { (x, y, b) ->
                if (b == null) {
                    return@any false
                }

                val tx = t.x + x
                val ty = t.y + y
                if (tx < 0 || tx >= num_x || ty < 0 || ty >= num_y) {
                    true
                } else {
                    blockMap.get(tx, ty) != null
                }
            }

    fun merge(t: Tetromino) {
        t.blockMap.blocks().forEach { (x, y, b) ->
            if (b != null)
                blockMap.set(t.x + x, t.y + y, b)
        }
    }

    fun clean(): Boolean {
        val (y, row) = blockMap.rows().find { (_, row) ->
            row.all { it != null }
        } ?: return false

        row.forEach { it?.removeFromParent() }

        (y downTo 1).forEach { yy ->
            blockMap.setRow(yy, blockMap.getRow(yy - 1).copyOf())
        }

        return true
    }

    fun clear() {
        blockMap.clear()
    }

    fun draw() {
        blockMap.draw(Block.WIDTH, 0.0)
    }
}
