package com.scalaAsm.x86
package Operands

import com.scalaAsm.utils.Endian

protected[x86] trait AddressingFormSpecifier {
    val modRM: Option[ModRMFormat]
    val scaleIndexBase: Option[Immediate]
    val displacment: Option[Immediate]
    val immediate: Option[Immediate]
    
    lazy val getBytes: Array[Byte] = {
      val components = List(scaleIndexBase, displacment, immediate)
      (modRM match {
        case (Some(modRM)) => Array(modRM.get)
        case _ => Array.emptyByteArray
      }) ++ components.flatten.flatMap(_.getBytes)
    }
    
    lazy val size: Int = {
      val components = List(scaleIndexBase, displacment, immediate)
      (modRM match {
        case (Some(modRM)) => 1
        case _ => 0
      }) + components.flatten.map(imm => imm.size).sum
    }
  }

object ModRM {
  type rm = RegisterOrMemory
  type reg = GPR
}

 sealed class RegisterMode(val value: Byte)
  case object NoDisplacment extends RegisterMode(0) // If r/m is 110, Displacement (16 bits) is address; otherwise, no displacemen
  case object Displacment8 extends RegisterMode(1)  // Eight-bit displacement, sign-extended to 16 bits
  case object Displacment16or32 extends RegisterMode(2) // 32-bit displacement (example: MOV [BX + SI]+ displacement,al)
  case object OtherRegister extends RegisterMode(3)     // r/m is treated as a second "reg" field

trait ModRMFormat {
    def get: Byte
  }
  
  case class ModRMByte(mod: RegisterMode, rm: GPR, reg: Option[GPR] = None, opEx: Option[Byte] = None) extends ModRMFormat {
    def get: Byte = {
      (reg, opEx) match {
        case (_, Some(opEx)) => ((mod.value << 6) + (opEx << 3) + rm.ID).toByte
        case (_, _) => ((mod.value << 6) + (reg.get.ID << 3) + rm.ID).toByte
      }
      
    }
  }