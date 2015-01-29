package com.scalaAsm.x86
package Instructions
package General

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Jump short if zero/equal (ZF=0)
// Category: general/branch/cond

object JZ extends InstructionDefinition[OneOpcode]("JZ") with JZImpl

trait JZImpl {
  implicit object JZ_0 extends JZ._1[rel8] {
    def opcode = 0x74
  }
}
