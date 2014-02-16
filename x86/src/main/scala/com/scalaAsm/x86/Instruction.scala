package com.scalaAsm.x86

import com.scalaAsm.x86.ModRM._

object Instruction extends ModRM with Operands {

}

private[x86] trait Instruction extends ModRM with Operands {
  def opcode: Byte
  def opcodeExtension: Option[Byte]
  def modRM: Option[AddressingFormSpecifier]

  def getAddressingFormExtended2[X, Y](inst: Instruction2[X, Y])(implicit ev: MODRM_2Extended[X, Y]): AddressingFormSpecifier = {
    modRM2Extended(inst.operand1, inst.operand2, inst.opcodeExtension.get)
  }

  def getAddressingFormExtended1[X](inst: Instruction1[X])(implicit ev: MODRM_1Extended[X]): AddressingFormSpecifier = {
    modRMExtended(inst.operand1, inst.opcodeExtension.get)
  }
  
  def getAddressingForm2[X, Y](inst: Instruction2[X, Y])(implicit ev: MODRM_2[X, Y]): AddressingFormSpecifier = {
    modRM2(inst.operand1, inst.operand2)
  }

  def getAddressingForm1[X](inst: Instruction1[X])(implicit ev: MODRM_1[X]): AddressingFormSpecifier = {
    modRM(inst.operand1)
  }

  def getBytes: Array[Byte] = {
    Array(opcode) ++ modRM.get.getBytes
  }
}

private[x86] trait Instruction1[X] extends Instruction {
  def operand1: X
}

private[x86] trait Instruction2[X, Y] extends Instruction {
  def operand1: X
  def operand2: Y
}