package com.scalaAsm.x86
package Instructions

abstract class JZ extends x86Instruction("JZ")

trait JZ_1[-O1] extends JZ with OneOperand[O1] with OperandEncoding

object JZ {
  
  implicit object jz1 extends JZ_1[imm8] {
      def operands = new OneOperand[imm8](x) {
        def getAddressingForm = Some(new AddressingFormSpecifier {
	        val modRM = None
		    val scaleIndexBase = None
		    val displacment = None
		    val immediate = Some(x)
	     })}
      val opcode = OneOpcode(0x74)
  }
}