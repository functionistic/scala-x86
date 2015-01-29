package com.scalaAsm.x86
package Instructions
package General

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._

// Description: Load String
// Category: general/datamovstring

object LODS extends InstructionDefinition[OneOpcode]("LODS") with LODSImpl

trait LODSImpl {
  implicit object LODS_0 extends LODS._0 {
    def opcode = 0xAC
    override def hasImplicitOperand = true
  }
}
