package com.scalaAsm.x86
package Instructions
package General

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Jump short if not zero/not equal (ZF=1)
// Category: general/branch/cond

object JNE extends InstructionDefinition[OneOpcode]("JNE") with JNEImpl

trait JNEImpl {
  implicit object JNE_0 extends JNE._1[rel8] {
    def opcode = 0x75
  }
}