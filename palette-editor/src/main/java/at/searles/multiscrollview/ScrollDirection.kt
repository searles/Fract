package at.searles.multiscrollview

enum class ScrollDirection {
    NoScroll {
        override val isHorizontal = false
        override val isVertical = false
    },
    Horizontal {
        override val isHorizontal = true
        override val isVertical = false
    },
    Vertical {
        override val isHorizontal = false
        override val isVertical = true
    },
    Both {
        override val isHorizontal = true
        override val isVertical = true
    };

    abstract val isHorizontal: Boolean
    abstract val isVertical: Boolean
}