package at.searles.fractlang.interpreter

import at.searles.commons.math.Cplx

interface PlotCallback {
        fun plot(z: Cplx)
    }
