package psp
package std

import api._
import scala.{ collection => sc }
import scala.Any
import scala.Predef.StringCanBuildFrom
import scala.math.Numeric

/** Implicits handling the way in and the way out of policy collections.
 */
trait StdGateways extends Any
      with StdBuilds
      with StdOps
      with SetAndMapOps
      with StdUniversal {

  self =>

  implicit def opsDirect[A](xs: Direct[A]): ops.DirectOps[A]   = new ops.DirectOps(xs)
  implicit def opsLinear[A](xs: Linear[A]): ops.LinearOps[A]   = new ops.LinearOps(xs)

  implicit def conversionsForEach[A](xs: Each[A]): Conversions[A]         = new Conversions[A](xs)
  implicit def conversionsForJava[A](xs: jCollection[A]): Conversions[A]  = new Conversions[A](Each fromJava xs)
  implicit def conversionsForScala[A](xs: sCollection[A]): Conversions[A] = new Conversions[A](Each fromScala xs)
  implicit def conversionsForArray[A](xs: Array[A]): Conversions[A]       = new Conversions[A](Direct fromArray xs)
  implicit def conversionsForString[A](s: String): Conversions[Char]      = new Conversions[Char](Direct fromString s)
}

// Adapt CanBuildFrom to Builds, since there are zillions of implicit CanBuildFroms already lying around.
// This lets us use all our own methods yet still build the scala type at the end, e.g.
//   Vector("a", "b", "cd", "ef").m filter (_.length == 1) build
// Returns a Vector[String].
trait StdBuilds0 extends Any                 { implicit def implicitBuildsFromCBF[A, That](implicit z: CanBuild[A, That]): Builds[A, That] = Builds wrap z          }
trait StdBuilds1 extends Any with StdBuilds0 { implicit def implicitBuildsArray[A: CTag] : Builds[A, Array[A]]                             = Direct.arrayBuilder[A] }
trait StdBuilds2 extends Any with StdBuilds1 { implicit def implicitBuildsList[A] : Builds[A, Linear[A]]                                   = Linear.builder[A]  }
trait StdBuilds3 extends Any with StdBuilds2 { implicit def implicitBuildsSet[A: HashEq] : Builds[A, ExSet[A]]                             = ExSet.builder[A]   }
trait StdBuilds4 extends Any with StdBuilds3 { implicit def implicitBuildsDirect[A] : Builds[A, Direct[A]]                                 = Direct.builder[A]      }
trait StdBuilds  extends Any with StdBuilds4 { implicit def implicitBuildsString: Builds[Char, String]                                     = Direct.stringBuilder() }

trait GlobalShow0 {
  // A weaker variation of Shown - use Show[A] if one can be found and toString otherwise.
  implicit def showableToTryShown[A](x: A)(implicit z: TryShow[A]): TryShown = new TryShown(z show x)
}
trait GlobalShow extends GlobalShow0 {
  implicit def showableToShown[A](x: A)(implicit z: Show[A]): Shown   = Shown(z show x)
}

trait StdTypeclasses {
  implicit def tupleTwoPairUp[A, B] : PairUp[(A, B), A, B]                     = PairUp(_ -> _)
  implicit def productTwoPairDown[A, B] : PairDown[scala.Product2[A, B], A, B] = PairDown(fst, snd)
  implicit def linearSeqPairDown[A] : PairDown[Linear[A], A, Linear[A]]        = PairDown(_.head, _.tail)
}

trait SetAndMapOps1 extends Any {
  implicit def opsIntensionalSet[A](x: InSet[A]): ops.InSetOps[A]          = new ops.InSetOps(x)
  implicit def opsIntensionalMap[K, V](x: InMap[K, V]): ops.InMapOps[K, V] = new ops.InMapOps(x)
}
trait SetAndMapOps extends Any with SetAndMapOps1 {
  implicit def opsExtensionalSet[A](x: ExSet[A]): ops.ExSetOps[A]          = new ops.ExSetOps(x)
  implicit def opsExtensionalMap[K, V](x: ExMap[K, V]): ops.ExMapOps[K, V] = new ops.ExMapOps(x)
}

trait StdOps0 extends Any {
  implicit def opsForeach[A](xs: Each[A]): ops.ForeachOps[A] = new ops.ForeachOps(xs)

  implicit class ForeachableOps[A, Repr](repr: Repr)(implicit z: Foreachable.Coll[A, Repr]) {
    def m: AtomicView[A, Repr] = z wrap repr
  }
}
trait StdOps1 extends Any with StdOps0 {
  implicit def unViewify0[A, CC[A]](xs: View[A])(implicit z: Builds[A, CC[A]]): CC[A] = z build xs

  implicit class ForeachableSetOps[A, Repr](repr: Repr)(implicit z: ForeachableSet.Coll[A, Repr]) {
    def m: ExSetView[A, Repr] = z wrap repr
  }
}
trait StdOps2 extends Any with StdOps1 {
  implicit def opsDirectArray[A](xs: Array[A]): ops.DirectOps[A] = new ops.DirectOps(Direct fromArray xs)
  implicit def opsDirectString(s: String): ops.DirectOps[Char]   = new ops.DirectOps(Direct fromString s)

  // We buried Predef's {un,}augmentString in favor of these.
  @inline final implicit def pspAugmentString(x: String): PspStringOps   = new PspStringOps(x)

  implicit class ForeachableLinearOps[A, Repr](repr: Repr)(implicit z: ForeachableLinear.Coll[A, Repr]) {
    def m: LinearView[A, Repr] = z wrap repr
  }

  implicit def sCollectionIs[A, CC[X] <: sCollection[X]](xs: CC[A]): LinearView[A, CC[A]] = View linear (Linear fromScala xs)
  implicit def jIterableIs[A, CC[X] <: jIterable[X]](xs: CC[A]): LinearView[A, CC[A]]     = View linear (Linear fromJava xs)
  implicit def atomicForeachIs[A, CC[X] <: Each[X]](xs: CC[A]): AtomicView[A, CC[A]]      = View each xs
  implicit def opsEachView[A](x: View[A]): ops.EachApiViewOps[A]                          = new ops.EachApiViewOps(x)
}

trait StdOps3 extends Any with StdOps2 {
  implicit class ForeachableIndexedOps[A, Repr](repr: Repr)(implicit z: ForeachableIndexed.Coll[A, Repr]) {
    def m: DirectView[A, Repr] = z wrap repr
  }

  implicit def directIndexedIs[A, CC[X] <: Direct[X]](xs: CC[A]): DirectView[A, CC[A]]             = View direct xs
  implicit def directScalaIndexedIs[A, CC[X] <: sciIndexedSeq[X]](xs: CC[A]): DirectView[A, CC[A]] = View direct (Direct fromScala xs)
  implicit def directArrayIs[A](xs: Array[A]): DirectView[A, Array[A]]                             = View direct (Direct fromArray xs)
  implicit def directStringIs(xs: String): DirectView[Char, String]                                = View direct (Direct fromString xs)

  // We're (sickly) using the context bound to reduce the applicability of the implicit,
  // but then discarding it. The only way these can be value classes is if the type class
  // arrives with the method call.
  implicit def infixOpsPartialOrder[A: PartialOrder](x: A): infix.PartialOrderOps[A] = new infix.PartialOrderOps[A](x)
  implicit def infixOpsOrder[A: Order](x: A): infix.OrderOps[A]                      = new infix.OrderOps[A](x)
  implicit def infixOpsAlgebra[A: BooleanAlgebra](x: A): infix.AlgebraOps[A]         = new infix.AlgebraOps[A](x)
  implicit def infixOpsEq[A: Eq](x: A): infix.EqOps[A]                               = new infix.EqOps[A](x)
  implicit def infixOpsHash[A: Hash](x: A): infix.HashOps[A]                         = new infix.HashOps[A](x)

  implicit def opsDirectView[A, Repr](x: DirectView[A, Repr]): ops.DirectApiViewOps[A, Repr]             = new ops.DirectApiViewOps(x)
  implicit def opsPairView[R, A, B](x: View[R])(implicit z: PairDown[R, A, B]): ops.PairViewOps[R, A, B] = new ops.PairViewOps(x)
  implicit def opsBiFunction[T, R](f: (T, T) => R): ops.BiFunctionOps[T, R]                              = new ops.BiFunctionOps(f)
  implicit def opsBoolean(x: Boolean): ops.BooleanOps                                                    = new ops.BooleanOps(x)
  implicit def opsBooleanAlgebra[A](x: BooleanAlgebra[A]): ops.BooleanAlgebraOps[A]                      = new ops.BooleanAlgebraOps[A](x)
  implicit def opsChar(x: Char): ops.CharOps                                                             = new ops.CharOps(x)
  implicit def opsFileTime(x: jFileTime): ops.FileTimeOps                                                = new ops.FileTimeOps(x)
  implicit def opsFunction1[T, R](f: T => R): ops.Function1Ops[T, R]                                     = new ops.Function1Ops(f)
  implicit def opsFunction2[T1, T2, R](f: (T1, T2) => R): ops.Function2Ops[T1, T2, R]                    = new ops.Function2Ops(f)
  implicit def opsHasPreciseSize(x: HasPreciseSize): ops.HasPreciseSizeOps                               = new ops.HasPreciseSizeOps(x)
  implicit def opsJavaIterator[A](x: jIterator[A]): ops.JavaIteratorOps[A]                               = new ops.JavaIteratorOps[A](x)
  implicit def opsInputStream(x: InputStream): ops.InputStreamOps                                        = new ops.InputStreamOps(x)
  implicit def opsInt(x: Int): ops.IntOps                                                                = new ops.IntOps(x)
  implicit def opsLong(x: Long): ops.LongOps                                                             = new ops.LongOps(x)
  implicit def opsOption[A](x: Option[A]): ops.OptionOps[A]                                              = new ops.OptionOps[A](x)
  implicit def opsPartialFunction[A, B](pf: A ?=> B): ops.PartialFunctionOps[A, B]                       = new ops.PartialFunctionOps(pf)
  implicit def opsPrecise(x: Precise): ops.PreciseOps                                                    = new ops.PreciseOps(x)
  implicit def opsPredicate[A](p: Predicate[A]): ops.PredicateOps[A]                                     = new ops.PredicateOps(p)
  implicit def opsShowableSeq[A: Show](x: Each[A]): ops.ShowableSeqOps[A]                                = new ops.ShowableSeqOps(x)
  implicit def opsSize(x: Size): ops.SizeOps                                                             = new ops.SizeOps(x)
  implicit def opsStdOpt[A](x: Opt[A]): ops.StdOptOps[A]                                                 = new ops.StdOptOps[A](x)
  implicit def opsTry[A](x: Try[A]): ops.TryOps[A]                                                       = new ops.TryOps[A](x)
  implicit def opsUnit(x: Unit): ops.UnitOps.type                                                        = ops.UnitOps

  implicit def apiInSetPromote[A](x: InSet[A]): InSet.Impl[A]          = InSet impl x
  implicit def apiInMapPromote[K, V](x: InMap[K, V]): InMap.Impl[K, V] = InMap impl x
}

trait StdOps extends Any with StdOps3 {
  implicit def opsApiShowInterpolator(sc: StringContext): ShowInterpolator              = new ShowInterpolator(sc)
  implicit def predicateToDirectoryFilter[A](p: Predicate[A]): DirectoryStreamFilter[A] = new DirectoryStreamFilter[A] { def accept(entry: A) = p(entry) }
  implicit def predicateToPartialFunction[A](p: Predicate[A]): A ?=> A                  = { case x if p(x) => x }
  implicit def directoryStreamView[A](stream: DirectoryStream[A]): View[A]              = inView(BiIterable(stream) foreach _)

  // Promotion of the api type (which has as few methods as possible) to the
  // concrete type which has all the other ones.
  implicit def apiIndexPromote(x: Index): IndexImpl                    = Index impl x
  implicit def apiOrderPromote[A](ord: Order[A]): impl.OrderImpl[A]    = Order(ord.compare)
  implicit def apiExSetPromote[A](x: ExSet[A]): ExSet.Impl[A]          = ExSet impl x
  implicit def apiExMapPromote[K, V](x: ExMap[K, V]): ExMap.Impl[K, V] = ExMap impl x
}

// Prefer opsAnyRef.
trait StdUniversal0 extends Any                   { implicit def opsAny[A](x: A): ops.AnyOps[A]                 = new ops.AnyOps[A](x)    }
trait StdUniversal extends Any with StdUniversal0 { implicit def opsAnyRef[A <: AnyRef](x: A): ops.AnyRefOps[A] = new ops.AnyRefOps[A](x) }

// This doesn't work if the return type is declared as tc.VC[Repr], or if it is inferred.
// def m[CC[X] <: Walkable[X]](implicit tc: CC[Repr]): tc.VC[Repr] = tc wrap repr
//
// [error] /mirror/r/psp/std/testOnly/src/test/scala/OperationCounts.scala:56: polymorphic expression cannot be instantiated to expected type;
// [error]  found   : [CC[X] <: psp.std.Walkable[X]]tc.VC[psp.std.PolicyList[psp.std.Int]]
// [error]  required: psp.std.View[psp.std.Int]
// [error]     intRange(1, max / 2).m ++ nthRange(max / 2, max).toLinear.m
// [error]                                                               ^
// [error] one error found
