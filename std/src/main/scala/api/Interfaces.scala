package psp
package api

import Api._

/** Foreach is the common parent of View and Each.
 *
 *  A View always wraps an indeterminate number of Views
 *  and a single Each which provides the original basis.
 *  An Each may be composed from smaller Eaches but is
 *  otherwise atomic. The size of an Each is known, the
 *  size of a View may not be.
 */
trait Foreach[+A] extends Any {
  def size: Size
  def foreach(f: A => Unit): Unit
}
object Foreach {
  def apply[A](mf: Suspended[A], n: Size): Foreach[A] = new Foreach[A] {
    def size                        = n
    def foreach(f: A => Unit): Unit = mf(f)
  }
}
final class Builds[-Elem, +To](val f: Foreach[Elem] => To) {
  def build(xs: Foreach[Elem]): To   = f(xs)
  def apply(mf: Suspended[Elem]): To = build(Foreach(mf, Size.Unknown))
}

trait Each[+A]    extends Any with Foreach[A]
trait View[+A]    extends Any with Foreach[A]
trait Indexed[+A] extends Any with Each[A]    { def elemAt(i: Vdex): A }
trait Direct[+A]  extends Any with Indexed[A] { def size: Precise      }

trait ExSet[A]     extends Any with Each[A] { def apply(x: A): Boolean    }
trait ExMap[K, +V] extends Any              { def lookup: FiniteDom[K, V] }

/** When a View is split into two disjoint views.
 *  Notably, that's span, partition, and splitAt.
 */
trait SplitView[+A] extends Any {
  def left: View[A]   // the elements in the left-hand M.
  def right: View[A]  // the elements in the right-hand M.
  def rejoin: View[A] // Moral equivalent of left ++ right.
}

/** When a View presents as a sequence of pairs.
 *  There may be two underlying views being zipped, or one view holding pairs.
 */
trait ZipView[+A1, +A2] extends Any {
  // def relativeSize: Option[Long]
  def lefts: View[A1]        // the left element of each pair. Moral equivalent of pairs map fst.
  def rights: View[A2]       // the right element of each pair. Moral equivalent of pairs map snd.
  def pairs: View[A1 -> A2]  // the pairs. Moral equivalent of lefts zip rights.
}

/** When a Show type class is more trouble than it's worth.
 *  Not overriding toString here to leave open the possibility of
 *  using a synthetic toString, e.g. of case classes.
 */
trait ShowDirect extends Any { def to_s: String }
trait ShowSelf extends Any with ShowDirect { override def toString = to_s }