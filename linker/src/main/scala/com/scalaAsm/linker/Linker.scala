package com.scalaAsm.linker

import com.scalaAsm.portableExe.CompiledImports
import com.scalaAsm.portableExe.DosHeader
import com.scalaAsm.portableExe.PeHeader
import com.scalaAsm.portableExe.DataDirectories
import com.scalaAsm.portableExe.sections._
import com.scalaAsm.portableExe.{PortableExecutable}
import com.scalaAsm.portableExe.OptionalHeader
import com.scalaAsm.portableExe.AdditionalFields
import com.scalaAsm.portableExe.ImageDataDirectory
import com.scalaAsm.portableExe.FileHeader
import com.scalaAsm.portableExe.NtHeader
import com.scalaAsm.portableExe.sections.ResourceGen
import com.scalaAsm.coff.Assembled
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.scalaAsm.coff.Sections
import com.scalaAsm.coff.Section
import com.scalaAsm.coff.SectionHeader
import com.scalaAsm.coff.Characteristic
import com.scalaAsm.coff.CoffSymbol
import com.scalaAsm.coff.Coff

class Linker {
  
  def compileImports(objFile: Coff, dlls: Seq[String], is64Bit: Boolean, importsLoc: Int): CompiledImports = { 
    
    val dllImports = dlls flatMap { dll =>
	    val file = new File("C:/Windows/System32/" + dll);
	 
	    val bFile: Array[Byte] = Array.fill(file.length().toInt)(0);
	      
	    //convert file into array of bytes
	    val fileInputStream = new FileInputStream(file);
	    fileInputStream.read(bFile);
	    fileInputStream.close();
	    
	    val bbuf = ByteBuffer.wrap(bFile)
	    bbuf.order(ByteOrder.LITTLE_ENDIAN)
	    
	    val dosHeader = DosHeader.getDosHeader(bbuf)
	    val peHeader = PeHeader.getPeHeader(bbuf)
	    val dirs = DataDirectories.getDirectories(bbuf)
	    val sections = Sections.getSections(bbuf, peHeader.fileHeader.numberOfSections)
	
	    val export = ImageExportDirectory.getExports(bbuf, sections, dirs.exportSymbols)
	    val importedSymbols = export.functionNames intersect objFile.relocations.filter(reloc => reloc.symbol.sectionNumber == 0).map(_.symbol.name).toSeq
	    
      println(export.functionNames)
      
	    if (importedSymbols.isEmpty)
	      None
	    else
	      Some(Extern(dll, importedSymbols))
    }
    
    val test = Imports(imports = dllImports, offset = importsLoc)

    test.generateImports(is64Bit) 
  }
  
  def link(objFile: Coff, addressOfData: Int, is64Bit: Boolean, dlls: String*): PortableExecutable = {

    val executableImports = compileImports(objFile, dlls, is64Bit, 0x3000)
    
    var offset = 0x3000
    val importSymbols = executableImports.importSymbols.map { sym =>
      val result = CoffSymbol(sym.name.trim, offset, 0)
      if (!sym.name.contains(".dll")) {
        offset += 6
      }
      result
    }

    val getSymbolAddress: Map[String, Int] = {
      val newRefSymbols = objFile.symbols ++ importSymbols
      newRefSymbols.map{sym => (sym.name, sym.location)}.toMap
    }

    
    val resources = objFile.iconPath map (path => Option(ResourceGen.compileResources(0x4000, path))) getOrElse None

    val codeSection = objFile.sections.find { section => (section.header.characteristics & Characteristic.CODE.id) != 0 }.get
    val dataSection = objFile.sections.find { section => (section.header.characteristics & Characteristic.WRITE.id) != 0 }.get
      
    var code = codeSection.contents
    
    objFile.relocations.toList.foreach { relocation =>
      
        val bb = java.nio.ByteBuffer.allocate(4)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        
        relocation.relocationType match {
          case 1 => // addr
            bb.putInt(getSymbolAddress(relocation.symbol.name) + addressOfData - 0x2000 - relocation.referenceAddress - 5)
            code = code.patch(relocation.referenceAddress + 1, bb.array(), 4)
          case 2 => // ProcRef
            bb.putInt(getSymbolAddress(relocation.symbol.name) - relocation.referenceAddress - 5)
            code = code.patch(relocation.referenceAddress + 1, bb.array(), 4)
          case 4 => // InvokeRef/ImportRef
            bb.putInt(getSymbolAddress(relocation.symbol.name) - (relocation.referenceAddress + 0x1000) - 5)
            code = code.patch(relocation.referenceAddress + 1, bb.array(), 4)
          case 3 => // VarRef
            bb.putInt(getSymbolAddress(relocation.symbol.name) - 0x1000 + addressOfData + 0x400000)
            code = code.patch(relocation.referenceAddress + 1, bb.array(), 4)
          case 6 => // LabelRef
            code(relocation.referenceAddress + 1) = (getSymbolAddress(relocation.symbol.name) - relocation.referenceAddress - 2).toByte
        }
    }
    
    val idataSection = Section(
      SectionHeader(
        name = ".idata",
        virtualSize = executableImports.rawData.length,
        virtualAddress = 0x3000,
        sizeOfRawData = 0x200,
        pointerToRawData = 0x800,
        relocPtr = 0,
        linenumPtr = 0,
        relocations = 0,
        lineNumbers = 0,
        characteristics = Characteristic.CODE.id |
          Characteristic.EXECUTE.id |
          Characteristic.READ.id)
     , executableImports.rawData)
      
    val standardSections = List(codeSection.copy(contents = code), dataSection, idataSection)

    val resourceSection: Option[Section] = resources map {res =>
      Option(Section(
       SectionHeader(
        name = ".rsrc",
        virtualSize = res.length,
        virtualAddress = 0x4000,
        sizeOfRawData = res.length,
        pointerToRawData = 0xA00,
        relocPtr = 0,
        linenumPtr = 0,
        relocations = 0,
        lineNumbers = 0,
        characteristics = Characteristic.INITIALIZED.id |
          Characteristic.READ.id), res))
    } getOrElse None

    val sections: List[Section] = standardSections ++ resourceSection

    val dosHeader = DosHeader(
      e_cblp = 108,
      e_cp = 1,
      e_crlc = 0,
      e_cparhdr = 2,
      e_minalloc = 0,
      e_maxalloc = 65535.toShort,
      e_ss = 0,
      e_sp = 0,
      e_csum = 0,
      e_ip = 17,
      e_cs = 0,
      e_lfarlc = 64,
      e_ovno = 0,
      e_res = (0, 0, 26967, 13934),
      e_oemid = 8244,
      e_oeminfo = 29264,
      e_res2 = (26479, 24946, 8557, 2573, 46116.toShort, 47625.toShort, 256, 8653, 19636, 8653),
      watermark = "Scala x86\0")
      
    val optionalHeader = OptionalHeader(
      magic = if (is64Bit) 0x20b else 0x10b,
      majorLinkerVersion = 0,
      minorLinkerVersion = 40,
      sizeOfCode = 512,
      sizeOfInitializedData = 1024,
      sizeOfUninitData = 0,
      addressOfEntryPoint = 0x1000,
      baseOfCode = 0x1000,
      baseOfData = 0x2000,

      AdditionalFields(
        imageBase = 0x400000,
        sectionAlignment = 0x1000,
        fileAlignment = 0x200,
        majorOperatingSystemVersion = if (is64Bit) 5 else 4,
        minorOperatingSystemVersion = 2,
        majorImageVersion = 0,
        minorImageVersion = 0,
        majorSubsystemVersion = if (is64Bit) 5 else 4,
        minorSubsystemVersion = 2,
        win32Version = 0,
        sizeOfImage = 0x5000,
        sizeOfHeaders = 0x200,
        checksum = 0,
        subsystem = 3,
        dllCharacteristics = 0,
        sizeOfStackReserve = 0x100000,
        sizeOfStackCommit = if (is64Bit) 0x10000 else 0x1000,
        sizeOfHeapReserve = 0x100000,
        sizeOfHeapCommit = 0x1000,
        loaderFlags = 0,
        numberOfRvaAndSizes = 16))
        
    val numImportedFunctions = executableImports.importSymbols.filter(sym => !sym.name.contains(".dll")).size

    val directories = resources map {res => DataDirectories(
      importSymbols = executableImports.getImportsDirectory(idataSection.header.virtualAddress + numImportedFunctions * 6),
      importAddressTable = executableImports.getIATDirectory(idataSection.header.virtualAddress + numImportedFunctions * 6 + executableImports.nameTableSize),
      resource = ImageDataDirectory(0x4000, 11300)) 
    } getOrElse {
        DataDirectories(
          importSymbols = executableImports.getImportsDirectory(idataSection.header.virtualAddress + numImportedFunctions * 6),
          importAddressTable = executableImports.getIATDirectory(idataSection.header.virtualAddress + numImportedFunctions * 6 + executableImports.nameTableSize))
    }

    val fileHeader = FileHeader(
      machine = if (is64Bit) 0x8664.toShort else 0x14C,
      numberOfSections = sections.size.toShort,
      timeDateStamp = 0x535BF29F,
      pointerToSymbolTable = 0, // no importance
      numberOfSymbols = 0, // no importance
      sizeOfOptionalHeader = if (is64Bit) 0xF0 else 0xE0,
      characteristics = if (is64Bit) 47 else 271)

    PortableExecutable(dosHeader, NtHeader(fileHeader, optionalHeader), directories, sections)
  }
}