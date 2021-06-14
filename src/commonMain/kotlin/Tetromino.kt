class Tetromino(type: TetrominoType, var x: Int, var y: Int, var block: () -> Block) {
    var blockMap: BlockMap

    init {
        var x = 0
        var y = 0
        val height = type.pattern.filter { it == '/' }.count() + 1
        val width = type.pattern.takeWhile { it != '/' }.count()
        blockMap = BlockMap(width, height)
        type.pattern.forEach {
            when (it) {
                '.' -> blockMap.set(x++, y, block())
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
        blockMap.rotateLeft()
    }

    fun rotateRight() {
        blockMap.rotateRight()
    }

    suspend fun draw() {
        val baseX = (x + 1) * Block.WIDTH
        val baseY = y * Block.HEIGHT
        blockMap.draw(baseX, baseY)
    }
}
