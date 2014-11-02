package psp
package tests

import org.scalacheck._, Prop._, Gen._
import psp.std._, api._

trait PspArb1                 { implicit def arbSize: Arb[Size]       = Arb(gen.size)     }
trait PspArb2 extends PspArb1 { implicit def arbAtomic: Arb[Atomic]   = Arb(gen.atomic)   }
trait PspArb3 extends PspArb2 { implicit def arbPrecise: Arb[Precise] = Arb(gen.precise)  }

class SizeSpec extends ScalacheckBundle with PspArb3 {
  def bundle = "Size laws"

  private def tried[T](op: => T) = try scala.Right(op) catch { case t: Throwable => scala.Left(t) }

  // When testing e.g. associativity and the sum overflows, we
  // need to do more than compare values for equality.
  private def sameOutcome[T](p1: => T, p2: => T): Boolean = (tried(p1), tried(p2)) match {
    case (scala.Right(x1), scala.Right(x2)) => x1 == x2
    case (scala.Left(t1), scala.Left(t2))   => t1.getClass == t2.getClass
    case _                                  => false
  }

  def certain[T: Arb, U: Arb](f: (T, U) => Boolean): Prop = forAll((p1: T, p2: U) => f(p1, p2))
  def commutative[T: Arb](op: BinOp[T]): Prop             = forAll((p1: T, p2: T) => sameOutcome(op(p1, p2), op(p2, p1)))
  def associative[T: Arb](op: BinOp[T]): Prop             = forAll((p1: T, p2: T, p3: T) => sameOutcome(op(op(p1, p2), p3), op(p1, op(p2, p3))))
  def associatives[A: Arb](ops: BinOp[A]*): Prop          = ops map (x => associative(x)) reduceLeft (_ && _)
  def commutatives[A: Arb](ops: BinOp[A]*): Prop          = ops map (x => commutative(x)) reduceLeft (_ && _)

  def flip(r: Prop.Result): Prop.Result = r match {
    case Prop.Result(Prop.True, _, _, _)  => r.copy(status = Prop.False)
    case Prop.Result(Prop.False, _, _, _) => r.copy(status = Prop.True)
    case _                                => r
  }

  // ...Aaaaand right on cue, a bunch of these tests broke until I added a type annotation.
  def props = sciList[NamedProp](
    "s1 <= (s1 max s2)"               -> certain[Atomic, Atomic]((s1, s2) => (s1: Size) p_<= (s1 max s2)),
    "s1 >= (s1 min s2)"               -> certain[Atomic, Atomic]((s1, s2) => (s1: Size) p_>= (s1 min s2)),
    "s1 <= (s1 + s2)"                 -> certain[Atomic, Atomic]((s1, s2) => (s1: Size) p_<= (s1 + s2)),
    "s1 >= (s1 - s2)"                 -> certain[Atomic, Precise]((s1, s2) => (s1: Size) p_>= (s1 - s2)),
    "<inf> + n"                       -> forAll((s1: Size) => ((Infinite + s1) partialCompare Infinite) == PCmp.EQ),
    "max, min, and + are associative" -> associatives[Size](_ + _, _ max _, _ min _),
    "max, min, and + are commutative" -> commutatives[Size](_ + _, _ max _, _ min _)
  )
}
