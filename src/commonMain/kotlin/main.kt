import com.soywiz.korev.Key
import com.soywiz.korge.*
import com.soywiz.korge.input.keys
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class Type(val size: Int, val pattern: String, val pivots: Array<Pair<Int, Int>>) {
    I(4, "....", arrayOf(Pair(1, 0), Pair(0, 1))),
    O(2, "../..", arrayOf(Pair(0, 0))),
    T(3, ".../ . ", arrayOf(Pair(1, 0), Pair(0, 1), Pair(1, 1), Pair(1, 1))),
    J(3, " ./ ./..", arrayOf(Pair(1, 1), Pair(1, 0), Pair(0, 1), Pair(1, 1))),
    L(3, ". /. /..", arrayOf(Pair(1, 0), Pair(1, 1), Pair(0, 1), Pair(1, 1))),
    S(3, " ../.. ", arrayOf(Pair(1, 0), Pair(1, 1))),
    Z(3, ".. / ..", arrayOf(Pair(0, 1), Pair(1, 0)))
}

const val BLOCK_WIDTH = 20.0
const val BLOCK_HEIGHT = 20.0

class Piece(var type: Type, var x: Int, var y: Int, var init: () -> Block) {
    var blocks: Blocks = Blocks(type.size, type.size)

    init {
        var x = 0
        var y = 0
        type.pattern.forEach {
            when (it) {
                '.' -> blocks.set(x++, y, init())
                ' ' -> x++
                '/' -> {
                    x = 0
                    y++
                }
            }
        }
    }

    fun moveRight() {
        x += 1
    }

    fun moveLeft() {
        x -= 1
    }

    fun moveDown() {
        y += 1
    }

    fun moveUp() {
        y -= 1
    }

    fun rotateLeft() {
        blocks.rotateLeft()
    }

    fun rotateRight() {
        blocks.rotateRight()
    }

    suspend fun draw() {
        val baseX = (x + 1) * BLOCK_WIDTH
        val baseY = (y + 1) * BLOCK_HEIGHT
        (0 until blocks.height).forEach { y ->
            (0 until blocks.width).forEach { x ->
                val b = blocks.get(x, y)
                b?.position(x * BLOCK_WIDTH + baseX, y * BLOCK_HEIGHT + baseY)
            }
        }
    }
}

class Blocks(var width: Int, var height: Int) {
    var blocks = Array<Block?>(width * height) { null }

    fun get(x: Int, y: Int): Block? = blocks.getOrNull(y * width + x)

    fun set(x: Int, y: Int, b: Block?): Block? {
        val i = y * width + x
        val old = blocks[i]
        blocks[i] = b
        return old
    }

    private fun transpose() {
        val nb = Blocks(height, width)
        (0 until width).forEach { x ->
            (0 until height).forEach { y ->
                nb.set(y, x, get(x, y))
            }
        }
        width = nb.width
        height = nb.height
        blocks = nb.blocks
    }

    private fun reverseRow() {
        (0 until height).forEach { y ->
            (0 until width / 2).forEach { x ->
                val rx = width - x - 1
                set(x, y, get(rx, y).also { set(rx, y, get(x, y)) })
            }
        }
    }

    private fun reverseColumn() {
        (0 until width).forEach { x ->
            (0 until height / 2).forEach { y ->
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

    fun debug(label: String) {
        println("--- $label")
        blocks.withIndex().forEach { (index, b) ->
            if (b != null) {
                print("*")
            } else {
                print(".")
            }
            if (index % width == width - 1) {
                println()
            }
        }
    }
}

class Block(img: Bitmap) : Container() {
    init {
        image(img) {
            size(BLOCK_WIDTH, BLOCK_HEIGHT)
        }
    }
}

class Well(img: Bitmap, private val num_x: Int, private val num_y: Int) : Container() {
    val blocks: Blocks = Blocks(num_x, num_y)

    init {
        (0..num_x + 1).forEach { x ->
            (0..num_y + 1).forEach { y ->
                if (x == 0 || x == num_x + 1 || y == num_y + 1) {
                    image(img) {
                        position(x * BLOCK_WIDTH, y * BLOCK_HEIGHT)
                        size(BLOCK_WIDTH, BLOCK_HEIGHT)
                    }
                }
            }
        }
    }

    fun collision(p: Piece): Boolean =
        (0 until p.blocks.width).any { x ->
            (0 until p.blocks.height).any { y ->
                val wx = p.x + x
                val wy = p.y + y
                val wb = blocks.get(wx, wy)
                val pb = p.blocks.get(x, y)
                if (wb != null && pb != null) {
                    true
                } else if (pb != null) {
                    wx < 0 || wx >= num_x || y < 0 || wy >= num_y
                } else {
                    false
                }
            }
        }

    fun merge(p: Piece) {
        (0 until p.blocks.width).forEach { x ->
            (0 until p.blocks.height).forEach { y ->
                val b = p.blocks.get(x, y)
                if (b != null)
                    blocks.set(p.x + x, p.y + y, b)
            }
        }
    }

    fun clean(): Boolean {
        (blocks.height - 1 downTo 0).forEach { y ->
            var canClean = (0 until blocks.width).all { x ->
                blocks.get(x, y) != null
            }

            if (canClean) {
                (y downTo 1).forEach { yy ->
                    (0 until blocks.width).forEach { x ->
                        val old = blocks.set(x, yy, blocks.get(x, yy - 1))
                        if (yy == y)
                            old?.removeFromParent()
                    }
                }
                return true
            }
        }

        return false
    }

    suspend fun draw() {
        val baseX = (x + 1) * BLOCK_WIDTH
        val baseY = (y + 1) * BLOCK_HEIGHT
        (0 until blocks.width).forEach { x ->
            (0 until blocks.height).forEach { y ->
                val b = blocks.get(x, y)
                b?.position(x * BLOCK_WIDTH + baseX, y * BLOCK_HEIGHT + baseY)
            }
        }
    }
}

fun Container.generatePiece(imgs: Array<Bitmap>): Piece {
    val img = imgs[Random.nextInt(0, imgs.size)]

    return Piece(Type.values().random(), 3, 3) {
        Block(img).addTo(this)
    }
}

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    val imgs = Array(9) { resourcesVfs["chokkaku${it + 1}.png"].readBitmap() }

    val well = Well(imgs[0], 10, 20).addTo(this)
    var p = generatePiece(imgs)

    keys.down {
        when (it.key) {
            Key.LEFT -> {
                p.moveLeft()
                if (well.collision(p)) {
                    p.moveRight()
                }
                p.draw()
            }
            Key.RIGHT -> {
                p.moveRight()
                if (well.collision(p)) {
                    p.moveLeft()
                }
                p.draw()
            }
            Key.Z -> {
                p.rotateLeft()
                if (well.collision(p)) {
                    p.rotateRight()
                }
                p.draw()
            }
            Key.X -> {
                p.rotateRight()
                if (well.collision(p)) {
                    p.rotateLeft()
                }
                p.draw()
            }
            Key.DOWN -> {
                p.moveDown()
                if (well.collision(p)) {
                    p.moveUp()
                }
                p.draw()
            }
            Key.UP -> {
                while (!well.collision(p)) {
                    p.moveDown()
                }
                p.moveUp()
                p.draw()
            }
            else -> Unit
        }
    }

    while (true) {
        p.draw()
        delay(1000)
        p.moveDown()
        if (well.collision(p)) {
            p.moveUp()
            well.merge(p)
            while (well.clean()) {
                well.draw()
            }
            well.draw()
            p = generatePiece(imgs)
        }
    }
}