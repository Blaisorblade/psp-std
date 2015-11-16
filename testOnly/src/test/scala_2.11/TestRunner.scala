package psp
package tests

import std._, api._
import org.scalacheck._, Prop.forAll

object TestRunner_211 extends TestRunnerCommon {
  def scalaVersion = "2.11"

  /** How to check for function equivalence? In the absence of mathematical breakthroughs,
   *  recursively throw scalacheck at it again, verifying arbitrary inputs have the same result.
   */
  def observationalEq[M[X], A : Arbitrary, B : Eq](f: (M[A], A) => B): Eq[M[A]] = Eq[M[A]] { (xs, ys) =>
    val prop = forAll((elem: A) => f(xs, elem) === f(ys, elem))
    (Test check prop)(identity).passed
  }

  implicit def pintEq: Hash[Pint]                                        = inheritEq
  implicit def pintShow: Show[Pint]                                      = inheritShow
  implicit def predicateEq[A : Arbitrary] : Eq[InvariantPredicate[A]]    = observationalEq[InvariantPredicate, A, Boolean](_ apply _)
  implicit def intensionalEq[A : Arbitrary : Eq] : Eq[InvariantInSet[A]] = observationalEq[InvariantInSet, A, Boolean](_ apply _)

  lazy val bundles = commonBundles ++ vec[Bundle](
    new Typecheck,
    new Collections_211,
    new AlgebraSpec[Boolean]("Boolean") { override def join = "||" ; override def meet = "&&" },
    new AlgebraSpec[InvariantPredicate[Pint]]("InvariantPredicate[Pint]"),
    new AlgebraSpec[InvariantInSet[Pint]]("InvariantInSet[Pint]")
  )
}
