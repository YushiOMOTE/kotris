import com.soywiz.korge.view.position
import com.soywiz.korma.geom.triangle.Triangle

class BlockMap(var width: Int, var height: Int) {
    private var blocks = Array<Array<Block?>>(height) { Array(width) { null } }

    fun get(x: Int, y: Int): Block? = blocks.getOrNull(y)?.getOrNull(x)

    fun set(x: Int, y: Int, b: Block?): Block? {
        val old = blocks[y][x]
        blocks[y][x] = b
        return old
    }

    fun getRow(y: Int): Array<Block?> = blocks[y]

    fun setRow(y: Int, row: Array<Block?>) {
        blocks[y] = row
    }

    private fun transpose() {
        val nb = BlockMap(height, width)
        blocks().forEach { (x, y, b) ->
            nb.set(y, x, b)
        }
        width = nb.width
        height = nb.height
        blocks = nb.blocks
    }

    private fun reverseRow() {
        blocks().forEach { (x, y, _) ->
            if (x < width / 2) {
                val rx = width - x - 1
                set(x, y, get(rx, y).also { set(rx, y, get(x, y)) })
            }
        }
    }

    private fun reverseColumn() {
        blocks().forEach { (x, y, _) ->
            if (y < height / 2) {
                val ry = height - y - 1
                set(x, y, get(x, ry).also { set(x, ry, get(x, y)) })
            }
        }
    }

    fun rotateLeft() {
        transpose()
        reverseColumn()
    }

    fun rotateRight() {
        transpose()
        reverseRow()
    }

    fun blocks(): Sequence<Triple<Int, Int, Block?>> =
        rows().flatMap { (y, row) -> row.asSequence().withIndex().map { (x, b) -> Triple(x, y, b) } }

    fun rows(): Sequence<IndexedValue<Array<Block?>>> = blocks.asSequence().withIndex()

    fun draw(baseX: Double, baseY: Double) {
        blocks().forEach { (x, y, b) -> b?.position(x * Block.WIDTH + baseX, y * Block.HEIGHT + baseY) }
    }
}
