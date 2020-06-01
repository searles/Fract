package at.searles.fract.plugins

import android.os.AsyncTask
import at.searles.commons.math.Cplx
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.interpreter.DebugCallback
import at.searles.fractlang.interpreter.DebugException
import at.searles.fractlang.interpreter.Interpreter
import at.searles.fractlang.interpreter.PlotCallback
import at.searles.fractlang.nodes.Node

class CalcOrbitTask(
    private val startPoint: Cplx,
    private val program: FractlangProgram,
    private val parent: OrbitPlugin
): AsyncTask<Void, Void, List<Cplx>>() {

    override fun doInBackground(vararg params: Void?): List<Cplx> {
        val orbit = ArrayList<Cplx>()

        try {
            program.runInterpreter(Interpreter(
                startPoint,
                object : DebugCallback {
                    override fun step(
                        interpreter: Interpreter,
                        node: Node
                    ) {
                        if (isCancelled) {
                            throw TaskCancelledException()
                        }
                    }
                },
                object : PlotCallback {
                    override fun plot(z: Cplx) {
                        orbit.add(z)
                    }
                }
            ))
        } catch(e: DebugException) {
            // there is an uninitialized variable.
        } catch(e: TaskCancelledException) { }

        return orbit
    }

    override fun onPostExecute(result: List<Cplx>) {
        parent.orbit = result
    }

    private class TaskCancelledException: RuntimeException()
}