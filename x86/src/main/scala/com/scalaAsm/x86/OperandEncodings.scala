package com.scalaAsm.x86

import com.scalaAsm.x86.Operands._

trait OperandFormat

object NoOperand extends OperandFormat

object NoAddressingForm extends AddressingFormSpecifier {
   val (modRM, sib, displacment, immediate) = (None, None, None, None)
}

abstract class OneOperandFormat[-X <: Operand] extends OperandFormat {
  def getAddressingForm(x:X, opcode: OpcodeFormat): AddressingFormSpecifier
}

abstract class TwoOperandsFormat[-X <: Operand, -Y <: Operand] extends OperandFormat {
  def getPrefixes(x:X,y:Y): Option[Array[Byte]]
  def getAddressingForm(x:X,y:Y,opcode: OpcodeFormat): AddressingFormSpecifier
}

trait TwoOperandInstructionFormat extends OneOperandInstructionFormat {
   trait DualOperandEncoding[-O1 <: ModRM.rm, -O2] {
    def encode(x: O1, y: O2, opcodeExtend: Option[Byte]): (Option[ModRM], Option[SIB])
  }

//  def getEncoding[O1 <: ModRM.rm, O2](op1: O1, op2: O2, opcodeExtend: Option[Byte])(implicit operands: DualOperandEncoding[O1, O2]) = {
//    operands.encode(op1, op2, opcodeExtend)
//  }
  
    def encode(op1: GPR, op2: Immediate, opcodeExtend: Option[Byte]): (Option[ModRM], Option[SIB]) = {
      val modRM: Option[ModRM] = Some(ModRMOpcode(TwoRegisters, opcodeExtend.get, op1))
      (modRM, None)
    }

    def encode(op1: GPR, op2: Memory, opcodeExtend: Option[Byte]): (Option[ModRM], Option[SIB]) = {
      (op2.base, op2.offset, op2.immediate) match {
        case (Some(base), offset @ Some(off: Displacement8), None) if base.ID == 4 =>
          val modRM = Some(ModRMReg(DisplacementByte, op1, base))
          val sib = Some(SIB(SIB.One, new ESP, base))
          (modRM, sib)
        case (Some(base), offset @ Some(off: Displacement32), None) =>
          val modRM = Some(ModRMReg(DisplacementDword, reg = op1, rm = base))
          (modRM, None)
        case (Some(base), offset @ Some(_: Displacement), None) =>
          val modRM = Some(ModRMReg(DisplacementByte, reg = op1, rm = base))
          (modRM, None)
        case (Some(base), None, None) =>
          val modRM = Some(ModRMReg(NoDisplacement, reg = op1, rm = base))
          (modRM, None)
      }
    }


    def encode(op1: GPR, op2: GPR, opcodeExtend: Option[Byte]): (Option[ModRM], Option[SIB]) = {
      val modRM: Option[ModRM] = Some(ModRMReg(TwoRegisters, op1, op2))
      (modRM, None)
    }
  
  case class MI[M <: ModRM.rm, I <: Immediate]() extends TwoOperandsFormat[M, I] {

    def getAddressingForm(op1: M, op2: I, opcode: OpcodeFormat): AddressingFormSpecifier = {

      op1 match {
        case reg: GPR =>
          new AddressingFormSpecifier {
            val (modRM, sib) = encode(reg, op2, opcode.opcodeExtension)
            val (displacment, immediate) = (None, Some(op2))
          }
      }
    }
    
    def getPrefixes(op1: M, op2: I): Option[Array[Byte]] = {
      op1 match {
        case reg: UniformByteRegister =>
          Some(REX.W(false).get)
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }

  case class RM[R <: ModRM.reg, M <: ModRM.rm]() extends TwoOperandsFormat[R, M] {

    def getAddressingForm(op1: R, op2: M, opcode: OpcodeFormat): AddressingFormSpecifier = {

      op2 match {
        case mem: Memory =>
          new AddressingFormSpecifier {
            val (modRM, sib) = encode(op1, mem, opcode.opcodeExtension)
            val (displacment, immediate) = (mem.offset, mem.immediate)
          }
        case reg: GPR =>
          new AddressingFormSpecifier {
            val (modRM, sib) = encode(op1, reg, opcode.opcodeExtension)
            val (displacment, immediate) = (None, None)
          }
      }
    }

    def getPrefixes(op1: R, op2: M): Option[Array[Byte]] = {
      op1 match {
        case reg: UniformByteRegister =>
          Some(REX.W(false).get)
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }

  case class MR[M <: ModRM.rm, R <: ModRM.reg]() extends TwoOperandsFormat[M, R] {

    def getAddressingForm(op1: M, op2: R, opcode: OpcodeFormat): AddressingFormSpecifier = {
      RM().getAddressingForm(op2, op1, opcode)
    }

    def getPrefixes(op1: M, op2: R): Option[Array[Byte]] = RM().getPrefixes(op2, op1)
  }
  
   case class OI[O <: ModRM.plusRd, I <: Immediate]() extends TwoOperandsFormat[O, I] {

    def getAddressingForm(op1: O, op2: I, opcode: OpcodeFormat): AddressingFormSpecifier = {
      new AddressingFormSpecifier {
        val modRM = None
        val (sib, displacment, immediate) = (None, None, Some(op2))
      }
    }
    
    def getPrefixes(op1: O, op2: I): Option[Array[Byte]] = {
      op1 match {
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }
   
  case class M1[M <: ModRM.rm]() extends TwoOperandsFormat[M, One] {
    def getAddressingForm(op1: M, op2: One, opcode: OpcodeFormat): AddressingFormSpecifier = M().getAddressingForm(op1, opcode)
    def getPrefixes(op1: M, op2: One): Option[Array[Byte]] = None
  }
}

trait OneOperandInstructionFormat {

  trait SingleOperandEncoding[-O1 <: Operand] {
    def encode(op1: O1, opcodeExtend: Option[Byte]): (Option[ModRM], Option[SIB])
  }

  def getEncoding[O1 <: ModRM.rm](op1: O1, opcodeExtend: Option[Byte])(implicit operand: SingleOperandEncoding[O1]) = {
    operand.encode(op1, opcodeExtend)
  }

    def encode(op1: Memory, opcodeExtend: Option[Byte]): (Option[ModRM], Option[SIB]) = {
      (op1.base, op1.offset, op1.immediate) match {
        case (Some(base: Register64), _, _) =>
          val modRM = Some(ModRMOpcode(NoDisplacement, opcodeExtend.get, base))
          val sib = Some(SIB(SIB.One, new ESP, base))
          (modRM, sib)
        case (Some(base), offset @ Some(_: Displacement8), None) =>
          val modRM = Some(ModRMOpcode(DisplacementByte, opcodeExtend.get, base))
          (modRM, None)
        case (Some(base), None, None) =>
          val modRM = Some(ModRMOpcode(NoDisplacement, opcodeExtend.get, base))
          (modRM, None)
        case (None, None, imm @ Some(_: Immediate)) =>
          val modRM = Some(ModRMOpcode(NoDisplacement, opcodeExtend.get, new EBP))
          (modRM, None)
        case _ => (None, None)
      }
    }
  
    def encode(op1: GPR, opcodeExtend: Option[Byte]): (Option[ModRM], Option[SIB]) = {
          val modRM = Some(ModRMOpcode(TwoRegisters, opcodeExtend.get, op1))
          (modRM, None)
    }

  case class O[O <: ModRM.plusRd]() extends OneOperandFormat[O] {
    def getAddressingForm(op1: O, opcode: OpcodeFormat) = {
      new AddressingFormSpecifier {
        val modRM = None
        val (sib, displacment, immediate) = (None, None, None)
      }
    }
  }

 

  case class I[I <: Immediate]() extends OneOperandFormat[I] {

    def getAddressingForm(op1: I, opcode: OpcodeFormat) = {
      new AddressingFormSpecifier {
        val modRM = None
        val (sib, displacment, immediate) = (None, None, Some(op1))
      }
    }

  }

  case class Offset[O <: Memory]() extends OneOperandFormat[O] {

    def getAddressingForm(op1: O, opcode: OpcodeFormat) = {
      new AddressingFormSpecifier {
        val modRM = Some(ModRMOpcode(NoDisplacement, opcode.opcodeExtension.get, new EBP))
        val (sib, displacment, immediate) = (None, op1.offset, None)
      }
    }
  }

  case class M[M <: ModRM.rm]() extends OneOperandFormat[M] {

    def getAddressingForm(op1: M, opcode: OpcodeFormat) = {

      op1 match {
        case rel: Relative =>
          new AddressingFormSpecifier {
            val (modRM, sib) = (None, None)
            val (displacment, immediate) = (rel.offset, None)
          }
        case mem: Memory =>
          new AddressingFormSpecifier {
            val (modRM, sib) = encode(mem, opcode.opcodeExtension)
            val (displacment, immediate) = (mem.offset, mem.immediate)
          }
        case reg: GPR =>
          new AddressingFormSpecifier {
              val (modRM, sib) = encode(reg, opcode.opcodeExtension)
              val (displacment, immediate) = (None, None)
          }
      }
    }
  }
}