package psp
package std
package core

// import psp.std._,
import SizeInfo._

object Foreach {
  def Empty = Direct.Empty

  final class Constant[A](elem: A) extends Foreach[A] {
    def sizeInfo = Infinite
    @inline def foreach(f: A => Unit): Unit = while (true) f(elem)
    override def toString = pp"constant($elem)"
  }

  final case class Unfold[A](zero: A)(next: A => A) extends Foreach[A] {
    def sizeInfo = Infinite
    @inline def foreach(f: A => Unit): Unit = {
      var current = zero
      while (true) {
        f(current)
        current = next(current)
      }
    }
    override def toString = pp"unfold from $zero"
  }

  final case class Times[A](size: Size, elem: A) extends Foreach[A] with psp.std.HasPreciseSizeImpl {
    @inline def foreach(f: A => Unit): Unit = IntRange.until(0, size.value) foreach (_ => f(elem))
    override def toString = pp"$elem x$size"
  }

  final class PureForeach[+A](mf: Suspended[A], val sizeInfo: SizeInfo) extends Foreach[A] {
    @inline def foreach(f: A => Unit): Unit = mf(f)
    override def toString = pp"$mf"
  }

  def from(n: Int): Foreach[Int]       = unfold(n)(_ + 1)
  def from(n: Long): Foreach[Long]     = unfold(n)(_ + 1)
  def from(n: BigInt): Foreach[BigInt] = unfold(n)(_ + 1)

  def const[A](elem: A): Constant[A]            = new Constant(elem)
  def times[A](times: Int, elem: A): Foreach[A] = Times(Size(times), elem)

  def unfold[A](start: A)(next: A => A): Unfold[A]          = Unfold[A](start)(next)
  def traversable[A](xs: GenTraversableOnce[A]): Foreach[A] = new impl.TraversableAsForeach[A](xs.toTraversable.seq)

  def join[A](xs: Foreach[A], ys: Foreach[A]): Foreach[A] = {
    val sizeInfo = xs.sizeInfo + ys.sizeInfo
    val mf: Suspended[A] = f => { xs foreach f ; ys foreach f }
    new PureForeach(mf, sizeInfo)
  }
  def empty[A] : Foreach[A] = Empty
  def apply[A](mf: Suspended[A]): Foreach[A] = new PureForeach[A](mf, unknownSize)
  def elems[A](xs: A*): Foreach[A] = Direct.elems(xs: _*)

  def stringify[A: Show](xs: Foreach[A], max: Int = 3): String = {
    def prefix = xs.shortClass
    def lp = "("
    def rp = ")"
    def base = pp"""$prefix$lp${xs take max join ", "}"""

    xs.sizeInfo match {
      case Precise(Size(n)) if n <= max => pp"$base$rp"
      case Precise(n)                   => pp"$base, ... $n elements$rp"
      case Infinite                     => pp"$base, ... <inf>$rp"
      case info                         => pp"$base, ... $info$rp"
    }
  }
}