package generators

import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.vm.VmGenerator
import org.junit.Test
import java.io.File

class GenerateVm {
    @Test
    fun test() {
        val vm = VmGenerator.generateVm(FractlangProgram.vmInstructions)

        File("generated/vm.rsh").writeText(vm)
    }
}