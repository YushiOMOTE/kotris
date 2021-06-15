class Tetromino(type: TetrominoType, var x: Int, var y: Int, var block: () -> Block) {
    var blockMap: BlockMap

    init {
        var xx = 0
        var yy = 0
        val height = type.pattern.filter { it == '/' }.count() + 1
        val width = type.pattern.takeWhile { it != '/' }.count()
        blockMap = BlockMap(width, height)
        type.pattern.forEach {
            when (it) {
                '.' -> blockMap.set(xx++, yy, block())
                ' ' -> xx++
                '/' -> {
                    xx = 0
                    yy++
                }
            }
        }
        draw()
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
        blockMap.rotateLeft()
    }

    fun rotateRight() {
        blockMap.rotateRight()
    }

    fun draw() {
        val baseX = (x + 1) * Block.WIDTH
        val baseY = y * Block.HEIGHT
        blockMap.draw(baseX, baseY)
    }

    fun clear() {
        blockMap.clear()
    }
}
