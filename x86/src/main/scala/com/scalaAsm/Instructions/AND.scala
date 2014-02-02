package com.scalaAsm.x86.Instructions

import com.scalaAsm.x86._
import x86Registers._

trait AND extends ModRMFormat with Operands
trait AND_RM[-O1, -O2] extends AND {
  def get(p1: O1, p2: O2): Array[Byte]
}

object AND {
  implicit object and1 extends AND_RM[r32, r32] { def get(x: r32, y: r32) = 0x23.toByte +: modRM2(x, y) }
}