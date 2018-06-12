package io.udash.properties.seq

import io.udash.properties._
import io.udash.properties.single.ReadableProperty

private[properties]
abstract class ZippedSeqPropertyUtils[O] extends AbstractReadableSeqProperty[O, ReadableProperty[O]] {
  override val id: PropertyId = PropertyCreator.newID()
  override protected[properties] val parent: ReadableProperty[_] = null

  protected final val children = CrossCollections.createArray[ReadableProperty[O]]

  protected def update(fromIdx: Int): Unit

  protected final val originListener: Patch[ReadableProperty[_]] => Unit = (patch: Patch[ReadableProperty[_]]) => {
    val idx = patch.idx
    val removed = CrossCollections.slice(children, patch.idx, children.length)
    CrossCollections.replace(children, idx, children.length - idx)
    update(idx)
    val added = CrossCollections.slice(children, patch.idx, children.length)
    if (added.nonEmpty || removed.nonEmpty) {
      val mappedPatch = Patch(patch.idx, removed, added, patch.clearsProperty)
      CallbackSequencer().queue(
        s"${this.id.toString}:fireElementsListeners:${patch.hashCode()}",
        () => structureListeners.foreach(_.apply(mappedPatch))
      )
      valueChanged()
    }
  }

  override def get: Seq[O] =
    children.map(_.get)

  override def elemProperties: Seq[ReadableProperty[O]] =
    children
}

private[properties]
class ZippedReadableSeqProperty[A, B, O: PropertyCreator](
  s: ReadableSeqProperty[A, ReadableProperty[A]],
  p: ReadableSeqProperty[B, ReadableProperty[B]],
  combiner: (A, B) => O
) extends ZippedSeqPropertyUtils[O] {

  protected final def appendChildren(toCombine: Seq[(ReadableProperty[A], ReadableProperty[B])]): Unit =
    toCombine.foreach { case (x, y) => children.+=(x.combine(y, this)(combiner)) }

  protected def update(fromIdx: Int): Unit =
    appendChildren(s.elemProperties.zip(p.elemProperties).drop(fromIdx))

  update(0)
  s.listenStructure(originListener)
  p.listenStructure(originListener)
}

private[properties]
class ZippedAllReadableSeqProperty[A, B, O: PropertyCreator](
  s: ReadableSeqProperty[A, ReadableProperty[A]],
  p: ReadableSeqProperty[B, ReadableProperty[B]],
  combiner: (A, B) => O, defaultA: ReadableProperty[A], defaultB: ReadableProperty[B]
) extends ZippedReadableSeqProperty(s, p, combiner) {

  override protected def update(fromIdx: Int): Unit =
    appendChildren(s.elemProperties.zipAll(p.elemProperties, defaultA, defaultB).drop(fromIdx))
}

private[properties]
class ZippedWithIndexReadableSeqProperty[A](s: ReadableSeqProperty[A, ReadableProperty[A]])
  extends ZippedSeqPropertyUtils[(A, Int)] {

  protected final def appendChildren(toCombine: Seq[(ReadableProperty[A], Int)]): Unit =
    toCombine.foreach { case (x, y) => children.+=(x.transform(v => (v, y))) }

  protected def update(fromIdx: Int): Unit =
    appendChildren(s.elemProperties.zipWithIndex.drop(fromIdx))

  update(0)
  s.listenStructure(originListener)
}