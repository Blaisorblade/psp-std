package psp
package std
package ops

import api._

/** A ZipView has similar operations to a View, but with the benefit of
 *  being aware each element has a left and a right.
 */
final case class ZipViewOps[A1, A2](x: ZipView[A1, A2]) extends AnyVal {
  import x._
  def zfoldl[B](f: (B, A1, A2) => B)(implicit z: Empty[B]): B = foldl(z.empty)(f)
  def foldl[B](zero: B)(f: (B, A1, A2) => B): B = {
    var res = zero
    foreach ((x, y) => res = f(res, x, y))
    res
  }
  def find(p: (A1, A2) => Boolean): Option[A1 -> A2] = {
    foreach((x, y) => if (p(x, y)) return Some(x -> y))
    None
  }
  def foreach(f: (A1, A2) => Unit): Unit = (lefts, rights) match {
    case (xs: Direct[A1], ys: Direct[A2]) => (xs.size min ys.size).indices foreach (i => f(xs(i), ys(i)))
    case (xs: Direct[A1], ys)             => (ys take xs.size).foreachWithIndex((y, i) => f(xs(i), y))
    case (xs, ys: Direct[A2])             => (xs take ys.size).foreachWithIndex((x, i) => f(x, ys(i)))
    case _                                => lefts.iterator |> (it => rights foreach (y => if (it.hasNext) f(it.next, y) else return))
  }

  def corresponds(f: (A1, A2) => Boolean)         = this map f forallTrue
  def drop(n: Precise): ZipView[A1, A2]           = new Zipped2(lefts drop n, rights drop n)
  def filter(q: Predicate2[A1, A2])               = withFilter(q)
  def flatMap[B](f: (A1, A2) => View[B]): View[B] = inView(mf => foreach((x, y) => f(x, y) foreach mf))
  def map[B](f: (A1, A2) => B): View[B]           = inView(mf => foreach((x, y) => mf(f(x, y))))
  def take(n: Precise): ZipView[A1, A2]           = new Zipped2(lefts take n, rights take n)
  def toMap[A0 >: A1]: sciMap[A0, A2]             = (pairs: View[A0 -> A2]).toScalaMap
  def withFilter(q: Predicate2[A1, A2])           = inView[A1 -> A2](mf => foreach((x, y) => if (q(x, y)) mf(x -> y)))

  def filterLeft(q: ToBool[A1]): ZipView[A1, A2]  = withFilter((x, y) => q(x)).zipView
  def filterRight(q: ToBool[A2]): ZipView[A1, A2] = withFilter((x, y) => q(y)).zipView
  def findLeft(p: ToBool[A1]): Option[A1 -> A2]   = find((x, y) => p(x))
  def findRight(p: ToBool[A2]): Option[A1 -> A2]  = find((x, y) => p(y))
  def mapLeft[B1](g: A1 => B1): ZipView[B1, A2]   = new Zipped2(lefts map g, rights)
  def mapRight[B2](g: A2 => B2): ZipView[A1, B2]  = new Zipped2(lefts, rights map g)
  def takeWhileLeft(q: ToBool[A1])                = pairs takeWhile (xy => q(fst(xy)))
  def takeWhileRight(q: ToBool[A2])               = pairs takeWhile (xy => q(snd(xy)))

  final def force[That](implicit z: Builds[A1 -> A2, That]): That = z build pairs
}
