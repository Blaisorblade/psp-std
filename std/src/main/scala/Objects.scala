package psp
package std

import api._

object Pair {
  def apply[R, A, B](x: A, y: B)(implicit z: PairUp[R, A, B]): R          = z.create(x, y)
  def unapply[R, A, B](x: R)(implicit z: PairDown[R, A, B]): Some[(A, B)] = Some((z left x, z right x))
}
object +: {
  def unapply[A](xs: Array[A])       = if (xs.length == 0) None else Some(xs.head -> xs.tail)
  def unapply[A](xs: Each[A])        = xs match { case Each(hd, _*) => Some(hd -> xs.tail) ; case _ => None }
  def unapply[A](xs: sCollection[A]) = if (xs.isEmpty) None else Some(xs.head -> xs.tail)
}
