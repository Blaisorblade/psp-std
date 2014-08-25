package psp
package std
package core

// import psp.std._,
import Index.zero
import impl._

trait Direct[+A] extends Any with Indexed[A] with psp.std.Direct[A]
trait DirectLeaf[A] extends Any with Direct[A] with psp.std.DirectLeaf[A]
trait Indexed[+A] extends Any with psp.std.Indexed[A]

final class PureIndexed[+A](size: Size, indexFn: Index => A) extends IndexedImpl[A](size) {
  def elemAt(index: Index): A = indexFn(index)
}

// object Indexed {
//   implicit final class IndexedExtensionOps[A](val xs: Indexed[A]) extends AnyVal {
//     def apply(index: Index): A = xs elemAt index

//     def zip[B](that: Indexed[B]): Indexed[(A, B)]                                                   = zipWith(that)(_ -> _)
//     def zipWith[A1, B](that: Indexed[A1])(f: (A, A1) => B): Indexed[B]                              = new ZippedIndexed2(xs, that, f)
//     def zipWith[A1, A2, B](that1: Indexed[A1], that2: Indexed[A2])(f: (A, A1, A2) => B): Indexed[B] = new ZippedIndexed3(xs, that1, that2, f)
//   }
// }

object Direct {
  // implicit def newBuilder[A] : Builds[A, Direct[A]] = Builds((xs: Foreach[A]) =>
  //   xs match {
  //     case xs: Direct[A] => xs
  //     case _             => Direct.elems(xs.toSeq: _*)
  //   }
  // )
  object Empty extends IndexedImpl[Nothing](SizeInfo.Zero) with HasStaticSize[Nat._0] {
    def elemAt(index: Index): Nothing = failEmpty(pp"$this($index)")
    override def toString = "<empty>"
  }

  def join[A](xs: Direct[A], ys: Direct[A]): Direct[A] = pure(
    xs.size + ys.size,
    index => if (xs.size containsIndex index) xs elemAt index else ys elemAt index - xs.size.value
  )

  /** Immutability (particularly of Arrays) is on the honor system. */
  def pureArray[A](xs: Array[A]): Direct[A]                               = pure(Size(xs.length), xs apply _.value)
  def pure[Repr](xs: Repr)(implicit tc: DirectAccess[Repr]): Direct[tc.A] = pure(tc length xs, index => (tc elemAt xs)(index))
  def pure[A](size: Size, indexFn: Index => A): Direct[A]                 = new PureIndexed(size, indexFn)

  def fill[A](times: Int)(body: => A): Direct[A] = {
    val buf = Vector.newBuilder[A]
    indexRange(0, times) foreach (_ => buf += body)
    pure(buf.result)
  }
  def empty[A] : Direct[A] = Foreach.Empty
  def elems[A](xs: A*): Direct[A] = xs match {
    case xs: WrappedArray[A] => pureArray(xs.array)
    case _                   => pure(xs.toVector)
  }
}
object IntRange {
  def until(start: Int, end: Int): IntRange = if (end < start) until(start, start) else new IntRange(start, end - 1, isInclusive = false)
  def to(start: Int, last: Int): IntRange   = if (last < start) until(start, start) else new IntRange(start, last, isInclusive = true)
}

final class IntRange private (val start: Int, val last: Int, isInclusive: Boolean) extends IndexedImpl[Int](Size(last - start + 1)) with DirectLeaf[Int] {
  def contains(x: Int): Boolean = start <= x && x <= last
  def isEmpty               = last < start
  def end                   = last + 1
  def elemAt(i: Index): Int = start + i.value
  override def toString     = if (isInclusive) s"$start to $last" else s"$start until $end"
}

final class ZippedIndexed2[A, B, +C](left: Indexed[A], right: Indexed[B], f: (A, B) => C) extends Indexed[C] {
  def foreach(f: C => Unit): Unit = {
    var i = zero
    while (isDefinedAt(i)) { f(elemAt(i)); i = i.next }
  }
  def isDefinedAt(index: Index): Boolean = (left isDefinedAt index) && (right isDefinedAt index)
  def apply(index: Index): C             = elemAt(index)
  def elemAt(index: Index): C            = f(left elemAt index, right elemAt index)
  def sizeInfo: SizeInfo               = left.sizeInfo min right.sizeInfo
}

final class ZippedIndexed3[A, A1, A2, +B](xs1: Indexed[A], xs2: Indexed[A1], xs3: Indexed[A2], f: (A, A1, A2) => B) extends Indexed[B] {
  def foreach(f: B => Unit): Unit = {
    var i = zero
    while (isDefinedAt(i)) { f(elemAt(i)); i = i.next }
  }
  def isDefinedAt(index: Index): Boolean = (xs1 isDefinedAt index) && (xs2 isDefinedAt index) && (xs3 isDefinedAt index)
  def apply(index: Index): B             = elemAt(index)
  def elemAt(index: Index): B            = f(xs1(index), xs2(index), xs3(index))
  def sizeInfo: SizeInfo                 = xs1.sizeInfo min xs2.sizeInfo min xs3.sizeInfo
}