package com.scalaAsm.x86
package Instructions
package Standard

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

object RETN extends InstructionDefinition[OneOpcode]("RETN") with RETNImpl

// Return from procedure

trait RETNImpl {
  implicit object RETN_194_imm16 extends RETN._1[imm16] {
    def opcode = 0xC2
    override def hasImplicateOperand = true
  }

  implicit object RETN_195 extends RETN._0 {
    def opcode = 0xC3
    override def hasImplicateOperand = true
  }
}
