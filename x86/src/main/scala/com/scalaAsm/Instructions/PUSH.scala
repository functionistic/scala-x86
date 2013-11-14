package com.scalaAsm.x86.Instructions

import com.scalaAsm.x86._
import x86Registers._
import Addressing._
import scala.annotation.implicitNotFound
import com.scalaAsm.utils.Endian

trait PUSH extends ModRM with Operands

@implicitNotFound(msg = "Cannot find PUSH implementation for ${O1}")
trait PUSH_M[-O1] extends PUSH {
  def get(op1: O1): Array[Byte]
}

object PUSH extends Instruction {
  implicit object push1 extends PUSH_M[r32] { def get(x: r32) = Array((0x50 + x.ID).toByte) }
  implicit object push8 extends PUSH_M[r16] { def get(x: r16) = Array((0x50 + x.ID).toByte) }
  implicit object push4 extends PUSH_M[imm8] { def get(x: imm8) = Array(0x6A.toByte, x.value) }
  implicit object push5 extends PUSH_M[imm16] { def get(x: imm16) = Array(0x68.toByte) ++ Endian.swap(x.value) }
  implicit object push6 extends PUSH_M[*[r32 + imm8]] { def get(x: *[r32 + imm8]) = 0xFF.toByte +: modRM(x, reg = 6.toByte) }
  implicit object push7 extends PUSH_M[CS] { def get(x: CS) = Array(0x0E.toByte) }
}