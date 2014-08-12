package com.scalaAsm.x86
package Instructions
package Standard

trait LEA extends x86Instruction {
  val mnemonic = "LEA"
}

trait LEA_2[OpEn, -O1, -O2] extends TwoOperandInstruction[OpEn, O1,O2] with LEA

object LEA {

  implicit object lea1 extends LEA_2[RM, r, rm] {
      val opcode: OpcodeFormat = 0x8D
  }
}