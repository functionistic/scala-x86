package com.scalaAsm.asm

import com.scalaAsm.x86.Operands._
import com.scalaAsm.x86._

trait Registers {

    object rdi extends RDI
    object rax extends RAX
    object rcx extends RCX
    object rbp extends RBP
    object rdx extends RDX
    object rbx extends RBX
    object rsp extends RSP
  
    object edi extends EDI
    object eax extends EAX
    object ecx extends ECX
    object ebp extends EBP
    object edx extends EDX
    object ebx extends EBX
    object esp extends ESP
    
    object esi extends ESI
  
    object ax extends AX
    object cx extends CX
    object dx extends DX
  
    object ah extends AH
  
    object cl extends CL
    object bl extends BL
  
    object spl extends SPL
  
    object es extends ES
    object cs extends CS
    object ss extends SS
    object ds extends DS
  
    object r8 extends R8
    object r9 extends R9
    object r10 extends R10
    object r11 extends R11
    object r12 extends R12
    object r13 extends R13
    object r14 extends R14
    object r15 extends R15
    
    object xmm0 extends XMM0
    object xmm1 extends XMM1
    object xmm2 extends XMM2
    object xmm3 extends XMM3
    object xmm4 extends XMM4
    object xmm5 extends XMM5
    object xmm6 extends XMM6
    object xmm7 extends XMM7
}