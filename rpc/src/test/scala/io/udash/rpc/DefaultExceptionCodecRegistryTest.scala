package io.udash.rpc

import com.avsystem.commons.serialization.GenCodec
import io.udash.rpc.serialization.{DefaultExceptionCodecRegistry, ExceptionCodecRegistry}
import io.udash.testing.UdashSharedTest

private sealed trait RootTrait extends Throwable
private sealed trait SubTrait extends RootTrait
private case class SubTraitImpl() extends SubTrait

private sealed trait SealedHierarchy extends Throwable
private case class SealedHierarchyA(a: Int) extends SealedHierarchy
private case class SealedHierarchyB(b: Double) extends SealedHierarchy

class DefaultExceptionCodecRegistryTest extends UdashSharedTest with Utils  {
  val exceptionsRegistry: ExceptionCodecRegistry = new DefaultExceptionCodecRegistry
  exceptionsRegistry.register(GenCodec.materialize[CustomException], "CustomException")
  exceptionsRegistry.register(GenCodec.materialize[SealedHierarchy], "SealedHierarchy")
  exceptionsRegistry.register(GenCodec.materialize[RootTrait], "RootTrait")

  "DefaultExceptionCodecRegistry" should {
    "find name of GenCodec for class" in {
      exceptionsRegistry.name(new RuntimeException("???")) should be(None)
      exceptionsRegistry.name(new NullPointerException("???")) should be(Some("NPE"))
      exceptionsRegistry.name(CustomException("???", 7)) should be(Some("CustomException"))
      exceptionsRegistry.name(SealedHierarchyA(42)) should be(Some("SealedHierarchy"))
      exceptionsRegistry.name(SealedHierarchyB(42)) should be(Some("SealedHierarchy"))
      exceptionsRegistry.name(SubTraitImpl()) should be(Some("RootTrait"))
    }
  }
}