package com.scalaAsm.x86
package Instructions

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86.Operands.Memory._
import com.scalaAsm.x86.MachineCode
import com.scalaAsm.x86.OneOpcode
import com.scalaAsm.x86.OpcodeFormat
import com.scalaAsm.x86.REX
import com.scalaAsm.x86.TwoOpcodes
import scala.language.implicitConversions
import com.scalaAsm.x86.Operands._

trait LowPriorityFormats extends OperandEncoding {

  implicit object MFormat extends OneOperandFormat[M, Relative] {

    def getAddressingForm(operand: Relative, opcode: OpcodeFormat) = {

          InstructionFormat (
            addressingForm = OnlyDisplacement(operand.displacement),
            immediate = None
          )
        
    }
    
     def getPrefixes(operand: Relative): Option[Array[Byte]] = None
  }
  
  implicit object MFormat2 extends OneOperandFormat[M, RegisterIndirect[r32]] {

    def getAddressingForm(operand: RegisterIndirect[r32], opcode: OpcodeFormat) = {
          InstructionFormat (
            addressingForm = OnlyModRM(ModRMOpcode(NoDisplacement, opcode.opcodeExtension.get, operand.base)), //mem.encode(opcode.opcodeExtension),
            immediate = None
          )
    }
    
     def getPrefixes(operand: RegisterIndirect[r32]): Option[Array[Byte]] = None
  }
  
  implicit object MFormatB1 extends OneOperandFormat[M, BaseIndex[r64, _]] {

    def getAddressingForm(operand: BaseIndex[r64, _], opcode: OpcodeFormat) = {
          InstructionFormat (
            WithSIBNoDisplacement(ModRMOpcode(NoDisplacement, opcode.opcodeExtension.get, operand.base), SIB(SIB.One, new ESP, operand.base)),
            immediate = None
          )
    }
    
     def getPrefixes(operand: BaseIndex[r64, _]): Option[Array[Byte]] = None
  }
  
  implicit object MFormatB2 extends OneOperandFormat[M, BaseIndex[_,Constant8]] {

    def getAddressingForm(operand: BaseIndex[_,Constant8], opcode: OpcodeFormat) = {
          InstructionFormat (
            NoSIBWithDisplacement(ModRMOpcode(DisplacementByte, opcode.opcodeExtension.get, operand.base), operand.displacement),
            immediate = None
          )
    }
    
     def getPrefixes(operand: BaseIndex[_,Constant8]): Option[Array[Byte]] = None
  }
  
  implicit object MFormat4 extends OneOperandFormat[M, GPR] {

    def getAddressingForm(operand: GPR, opcode: OpcodeFormat) = {
          InstructionFormat (
            addressingForm = OnlyModRM(ModRMOpcode(TwoRegisters, opcode.opcodeExtension.get, operand)),///reg.encode(opcode.opcodeExtension),
            immediate = None
          )
    }
    
     def getPrefixes(operand: GPR): Option[Array[Byte]] = None
  }

  implicit object DSFormat extends OneOperandFormat[DS, DS] {
    def getAddressingForm(op1: DS, opcode: OpcodeFormat) = NoAddressingForm
    def getPrefixes(op1: DS) = None
  }

  implicit object CSFormat extends OneOperandFormat[CS, CS] {
    def getAddressingForm(op1: CS, opcode: OpcodeFormat) = NoAddressingForm
    def getPrefixes(op1: CS) = None
  }

  implicit object OFormat extends OneOperandFormat[O, ModRM.plusRd] {
    def getAddressingForm(operand: ModRM.plusRd, opcode: OpcodeFormat) = {
      InstructionFormat (
        addressingForm = NoModRM(),
        immediate = None
      )
    }
    
    def getPrefixes(operand: ModRM.plusRd): Option[Array[Byte]] = None
  }

  implicit object IFormat extends OneOperandFormat[I, Immediate] {

    def getAddressingForm(operand: Immediate, opcode: OpcodeFormat) = {
      InstructionFormat (
        addressingForm = NoModRM(),
        immediate = Some(operand)
      )
    }
    
    def getPrefixes(operand: Immediate): Option[Array[Byte]] = None
  }

  implicit object OffsetFormat extends OneOperandFormat[Offset, BaseIndex[r64,_]] {

    def getAddressingForm(operand: BaseIndex[r64,_], opcode: OpcodeFormat) = {
      InstructionFormat (
        WithSIBNoDisplacement(ModRMOpcode(NoDisplacement, opcode.opcodeExtension.get, operand.base), SIB(SIB.One, new ESP, operand.base)),
        immediate = None
      )
    }
    
    def getPrefixes(operand: BaseIndex[r64,_]): Option[Array[Byte]] = None
  }
  
  implicit object OffsetFormat2 extends OneOperandFormat[Offset, BaseIndex[_,Constant8]] {

    def getAddressingForm(operand: BaseIndex[_,Constant8], opcode: OpcodeFormat) = {
      InstructionFormat (
        NoSIBWithDisplacement(ModRMOpcode(DisplacementByte, opcode.opcodeExtension.get, operand.base), operand.displacement),
        immediate = None
      )
    }
    
    def getPrefixes(operand: BaseIndex[_,Constant8]): Option[Array[Byte]] = None
  }
  
  implicit object MRFormat extends TwoOperandFormat[MR, BaseIndex[_,Constant8], ModRM.reg] {

    def getAddressingForm(op1: BaseIndex[_,Constant8], op2: ModRM.reg, opcode: OpcodeFormat) = {
      RMFormat.getAddressingForm(op2, op1, opcode)
    }

    def getPrefixes(op1: BaseIndex[_,Constant8], op2: ModRM.reg): Option[Array[Byte]] = RMFormat.getPrefixes(op2, op1)
  }
  
  implicit object RMFormat extends TwoOperandFormat[RM, ModRM.reg, BaseIndex[_,Constant8]] {

    def getAddressingForm(op1: ModRM.reg, op2: BaseIndex[_,Constant8], opcode: OpcodeFormat) = {
      InstructionFormat (
          NoSIBWithDisplacement(ModRMReg(DisplacementByte, reg = op1, rm = op2.base), op2.displacement),
          immediate = None
      )
    }

    def getPrefixes(op1: ModRM.reg, op2: BaseIndex[_,Constant8]): Option[Array[Byte]] = {
      op1 match {
        case reg: UniformByteRegister =>
          Some(REX.W(false).get)
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }
}



trait Formats extends LowPriorityFormats {

  implicit object RMFormatB1 extends TwoOperandFormat[RM, ModRM.reg, BaseIndex[ESP,Constant8]] {

    def getAddressingForm(op1: ModRM.reg, op2: BaseIndex[ESP,Constant8], opcode: OpcodeFormat) = {
      InstructionFormat (
    	    WithSIBWithDisplacement(ModRMReg(DisplacementByte, op1, op2.base), SIB(SIB.One, new ESP, op2.base), op2.displacement),
          immediate = None
      )
    }

    def getPrefixes(op1: ModRM.reg, op2: BaseIndex[ESP,Constant8]): Option[Array[Byte]] = {
      op1 match {
        case reg: UniformByteRegister =>
          Some(REX.W(false).get)
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }
  
  implicit object RMFormatB2 extends TwoOperandFormat[RM, ModRM.reg, BaseIndex[_,Constant32]] {

    def getAddressingForm(op1: ModRM.reg, op2: BaseIndex[_,Constant32], opcode: OpcodeFormat) = {
      InstructionFormat (
    	    NoSIBWithDisplacement(ModRMReg(DisplacementDword, reg = op1, rm = op2.base), op2.displacement),  
          immediate = None
      )
    }

    def getPrefixes(op1: ModRM.reg, op2: BaseIndex[_,Constant32]): Option[Array[Byte]] = {
      op1 match {
        case reg: UniformByteRegister =>
          Some(REX.W(false).get)
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }
  
  implicit object RMFormat6 extends TwoOperandFormat[RM, ModRM.reg, ModRM.reg] {

    def getAddressingForm(op1: ModRM.reg, op2: ModRM.reg, opcode: OpcodeFormat) = {

      InstructionFormat (
        OnlyModRM(ModRMReg(TwoRegisters, op1, op2)),
        immediate = None
      )
    }

    def getPrefixes(op1: ModRM.reg, op2: ModRM.reg): Option[Array[Byte]] = {
      op1 match {
        case reg: UniformByteRegister =>
          Some(REX.W(false).get)
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }
  
  implicit object MFormat5 extends OneOperandFormat[M, AbsoluteAddress[Constant32]] {

    def getAddressingForm(operand: AbsoluteAddress[Constant32], opcode: OpcodeFormat) = {
          InstructionFormat (
            addressingForm = NoSIBWithDisplacement(ModRMOpcode(NoDisplacement, opcode.opcodeExtension.get, new EBP), operand.displacement), //mem.encode(opcode.opcodeExtension),
            immediate = None
          )
    }
    
     def getPrefixes(operand: AbsoluteAddress[Constant32]): Option[Array[Byte]] = None
  }
  
    implicit object AbsoluteAddress32 extends AbsoluteAddress[Constant32] {
      selff =>
        var offset = 0
      def displacement = Constant32(offset)
        
      def getRelative = new Relative32 {
        def displacement = Constant32(offset)
        def size = 4
      }
    }
  
  implicit object AbsoluteAddress64 extends AbsoluteAddress[Constant64] {
    selff =>
      var offset:Long = 0
      def displacement = Constant64(offset)
        
      def getRelative = new Relative64 {
        def displacement = Constant64(offset)
        def size = 4
      }
  }
  
  implicit object MIFormat extends TwoOperandFormat[MI, ModRM.rm, Immediate] {

    def getAddressingForm(op1: ModRM.rm, op2: Immediate, opcode: OpcodeFormat) = {

      op1 match {
        case reg: GPR =>
          InstructionFormat (
            addressingForm = OnlyModRM(ModRMOpcode(TwoRegisters, opcode.opcodeExtension.get, reg)),//reg.encode(opcode.opcodeExtension),
            immediate = Some(op2)
          )
      }
    }

    def getPrefixes(op1: ModRM.rm, op2: Immediate): Option[Array[Byte]] = {
      op1 match {
        case reg: UniformByteRegister =>
          Some(REX.W(false).get)
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }

  implicit object RMFormat2 extends TwoOperandFormat[RM, ModRM.reg, AbsoluteAddress[Constant32]] {

    def getAddressingForm(op1: ModRM.reg, op2: AbsoluteAddress[Constant32], opcode: OpcodeFormat) = {
      InstructionFormat (
        addressingForm = NoSIBWithDisplacement(ModRMOpcode(NoDisplacement, opcode.opcodeExtension.get, new EBP), op2.displacement),
        immediate = None
      )
    }

    def getPrefixes(op1: ModRM.reg, op2: AbsoluteAddress[Constant32]): Option[Array[Byte]] = {
      op1 match {
        case reg: UniformByteRegister =>
          Some(REX.W(false).get)
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }
  
  implicit object RMFormat3 extends TwoOperandFormat[RM, ModRM.reg, RegisterIndirect[r32]] {

    def getAddressingForm(op1: ModRM.reg, op2: RegisterIndirect[r32], opcode: OpcodeFormat) = {
      InstructionFormat (
        addressingForm = OnlyModRM(ModRMReg(NoDisplacement, op1, rm = op2.base)),
        immediate = None
      )
    }

    def getPrefixes(op1: ModRM.reg, op2: RegisterIndirect[r32]): Option[Array[Byte]] = {
      op1 match {
        case reg: UniformByteRegister =>
          Some(REX.W(false).get)
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }

 
  
  implicit object MRFormat2 extends TwoOperandFormat[MR, ModRM.reg, ModRM.reg] {

    def getAddressingForm(op1: ModRM.reg, op2: ModRM.reg, opcode: OpcodeFormat) = {
      RMFormat6.getAddressingForm(op2, op1, opcode)
    }

    def getPrefixes(op1: ModRM.reg, op2: ModRM.reg): Option[Array[Byte]] = RMFormat6.getPrefixes(op2, op1)
  }

  implicit object OIFormat extends TwoOperandFormat[OI, ModRM.plusRd, Immediate] {

    def getAddressingForm(op1: ModRM.plusRd, op2: Immediate, opcode: OpcodeFormat) = {
      InstructionFormat (
        addressingForm = NoModRM(),
        immediate = Some(op2)
      )
    }

    def getPrefixes(op1: ModRM.plusRd, op2: Immediate): Option[Array[Byte]] = {
      op1 match {
        case reg: Register64 =>
          Some(REX.W(true).get)
        case _ => None
      }
    }
  }

  implicit object M1Format extends TwoOperandFormat[M1, Relative{type Size = DwordOperand}, One] with Formats {
    def getAddressingForm(op1: Relative{type Size = DwordOperand},  op2: One, opcode: OpcodeFormat) = MFormat.getAddressingForm(op1, opcode)
    def getPrefixes(op1: Relative{type Size = DwordOperand}, op2: One): Option[Array[Byte]] = MFormat.getPrefixes(op1)
  }
  
  implicit object M1Format2 extends TwoOperandFormat[M1, RegisterIndirect[r32], One] with Formats {
    def getAddressingForm(op1: RegisterIndirect[r32],  op2: One, opcode: OpcodeFormat) = MFormat2.getAddressingForm(op1, opcode)
    def getPrefixes(op1: RegisterIndirect[r32], op2: One): Option[Array[Byte]] = MFormat2.getPrefixes(op1)
  }
  
  implicit object M1Format3 extends TwoOperandFormat[M1, BaseIndex[r64,_], One] with Formats {
    def getAddressingForm(op1: BaseIndex[r64,_],  op2: One, opcode: OpcodeFormat) = MFormatB1.getAddressingForm(op1, opcode)
    def getPrefixes(op1: BaseIndex[r64,_], op2: One): Option[Array[Byte]] = MFormatB1.getPrefixes(op1)
  }
  
  implicit object M1Format4 extends TwoOperandFormat[M1, GPR, One] with Formats {
    def getAddressingForm(op1: GPR,  op2: One, opcode: OpcodeFormat) = MFormat4.getAddressingForm(op1, opcode)
    def getPrefixes(op1: GPR, op2: One): Option[Array[Byte]] = MFormat4.getPrefixes(op1)
  }
}