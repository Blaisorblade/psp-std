package psp
package std

import java.{ lang => jl }
import scala.{ collection => sc }
import api._
import psp.dmz.PolicyDmz

/** Yes I know all about implicit classes.
 *  There's no way to write an implicit value class which doesn't hardcode
 *  its location into an object. Separating the implicit conversion from
 *  the class allows clients to build their own package object.
 *
 *  This is all a consequence of scala offering no means for managing namespaces,
 *  so namespace management has become hopelessly entangled with unrelated concerns
 *  like inheritance, specificity, method dispatch, and so forth.
 */
abstract class StdPackage
      extends impl.OrderInstances
         with impl.EmptyInstances
         with StdTypeclasses
         with StdProperties
         with impl.AlgebraInstances
         with GlobalShow
         with StdGateways
         with lowlevel.StdArrowAssoc
         with ApiAliases
         with ApiMethods
         with SpireIntegration
         with PolicyDmz {

  // Higher than Direct.
  implicit def arraySpecificOps[A](xs: Array[A]): ops.ArraySpecificOps[A] = new ops.ArraySpecificOps[A](xs)


  implicit class ApiOrderOps[A](val ord: Order[A]) {
    def |[B: Order](f: A => B): Order[A] = Order((x, y) => ord.compare(x, y) || ?[Order[B]].compare(f(x), f(y)))
    def toEq: Eq[A]                      = Eq[A]((x, y) => ord.compare(x, y) == Cmp.EQ)
    def toHashEq: HashEq[A]              = HashEq natural toEq
    def reverse: Order[A]                = Order[A]((x, y) => ord.compare(x, y).flip)
    def on[B](f: B => A): Order[B]       = Order[B]((x, y) => ord.compare(f(x), f(y)))
  }
  implicit class CmpEnumOps(val cmp: Cmp) {
    def || (that: => Cmp): Cmp = if (cmp == Cmp.EQ) that else cmp
  }
  implicit class BuildsOps[Elem, To](z: Builds[Elem, To]) {
    def comap[Prev](f: Prev => Elem): Builds[Prev, To] = Builds(xs => z build (xs map f))
    def map[Next](f: To => Next): Builds[Elem, Next]   = Builds(xs => f(z build xs))
    def direct: Suspended[Elem] => To                  = mf => z build Each(mf)
    def scalaBuilder: scmBuilder[Elem, To]             = sciVector.newBuilder[Elem] mapResult (xs => z build xs)
  }
  implicit class JavaEnumerationOps[A](it: jEnumeration[A]) {
    def toIterator = BiIterator enumeration it
  }
  implicit class TupleViewOps[A, B](val xs: View[(A, B)]) {
    def filterLeft(p: Predicate[A])  = xs withFilter (x => p(x._1))
    def filterRight(p: Predicate[B]) = xs withFilter (x => p(x._2))
    def lefts: View[A]               = xs map (_._1)
    def rights: View[B]              = xs map (_._2)
  }
  implicit class Tuple2Ops[A, B](val lhs: (A, B)) {
    def fold[C, D](rhs: (A, B))(f: (A, A) => C, g: (B, B) => C)(h: (C, C) => D): D =
      h(f(lhs._1, rhs._1), g(lhs._2, rhs._2))
  }
  implicit class SameTuple2Ops[A](val x: (A, A)) {
    def seq: Direct[A] = Direct(x._1, x._2)
  }
  implicit class IsEmptyOps(val lhs: IsEmpty) {
    def emptyOrNonEmpty: Size = if (lhs.isEmpty) impl.Size.Empty else impl.Size.NonEmpty
  }
  implicit class AnyTargetSeqOps[A: HashEq](root: A) {
    def transitiveDepth(maxDepth: Int, expand: A => Each[A]): Each[A] = {
      var seen = exSet[A]()
      def loop(depth: Int, root: A, f: A => Unit): Unit = if (depth < maxDepth && !seen(root)) {
        seen = seen union exSet(root)
        f(root)
        expand(root) |> (xs => if (xs != null) xs foreach (x => loop(depth + 1, x, f)))
      }
      Each(f => loop(0, root, f))
    }
    def transitiveClosure(expand: A => View[A]): View[A] = inView { f =>
      var seen = exSet[A]()
      def loop(root: A, f: A => Unit): Unit = if (!seen(root)) {
        seen = seen add root
        f(root)
        expand(root) |> (xs => if (xs != null) xs foreach (x => loop(x, f)))
      }
      loop(root, f)
    }
  }

  implicit class Each2DOps[A](val xss: Each[Each[A]]) {
    def flatten: View[A]                  = xss flatMap identity
    def mmap[B](f: A => B): View[View[B]] = xss map (_ map f)
  }

  implicit def wrapClass(x: jClass): JavaClass                   = new JavaClass(x)
  implicit def wrapClassLoader(x: jClassLoader): JavaClassLoader = new JavaClassLoader(x)

  implicit def booleanToPredicate(value: Boolean): Predicate[Any] = if (value) ConstantTrue else ConstantFalse
  implicit def intToPreciseSize(n: Int): IntSize                  = Precise(n)
  implicit def convertNilSeq[A](x: scala.Nil.type): Direct[A]     = Direct[A]()

  implicit def convertPolicySeq[A, B](xs: Each[A])(implicit conversion: A => B): Each[B] = xs map (x => conversion(x))

  implicit def conforms[A] : (A <:< A) = new conformance[A]
}
