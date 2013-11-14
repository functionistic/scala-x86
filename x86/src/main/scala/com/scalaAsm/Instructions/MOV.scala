package com.scalaAsm.x86.Instructions

import com.scalaAsm.x86._
import x86Registers._
import Addressing._
import com.scalaAsm.utils.Endian

trait MOV extends ModRM with Operands
trait MOV_RM[O1, O2, O3] extends MOV {
  def get(x: O3): Array[Byte]
}

trait MOV_R[-O1, -O2] extends MOV {
  def get(op1: O1, op2: O2): Array[Byte]
}

trait MOV_R2[O1] extends MOV {
  def get(x: O1): Array[Byte]
}

object MOV extends Instruction {
  implicit object mov1 extends MOV_R[*[r32 + imm8], r32] { def get(x: *[r32 + imm8], y: r32) = 0x89.toByte +: modRM(x, y) }

  implicit object mov3 extends MOV_R[r32, *[r32 + imm8]] { def get(x: r32, y: *[r32 + imm8]) = 0x8B.toByte +: modRM(x, y) }

  implicit object mov4 extends MOV_R[r32, r32] { def get(x: r32, y: r32) = 0x8B.toByte +: modRM(x, y) }

  implicit object mov9 extends MOV_R[r16, r16] { def get(x: r16, y: r16) = 0x8B.toByte +: modRM(x, y) }

  implicit object mov5 extends MOV_R[r32, *[r32]] { def get(x: r32, y: *[r32]) = 0x8B.toByte +: modRM(x, y) }

  implicit object mov7 extends MOV_R[r16, imm16] { def get(x: r16, y: imm16) = Array[Byte]((0xB8 + x.ID).toByte, (y.value & 0x00FF).toByte, ((y.value & 0xFF00) >> 8).toByte) }
  implicit object mov8 extends MOV_R[r8, imm8] { def get(x: r8, y: imm8) = Array[Byte]((0xB0 + x.ID).toByte, y.value) }

  implicit object mov6 extends MOV_R[r32, imm32] { def get(x: r32, y: imm32) = (0xB8 + x.ID).toByte +: Endian.swap(y.value) }
}