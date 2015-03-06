package psp
package dmz

import scala._
import scala.{ collection => sc }
import sc.{ generic => scg, mutable => scm, immutable => sci }
import java.nio.{ file => jnf }

/** Building a default namespace consciously rather than accretively.
 */
trait ScalaLibrary extends Any {
  type BigDecimal                      = scala.math.BigDecimal
  type BigInt                          = scala.math.BigInt
  type CanBuildFrom[-From, -Elem, +To] = scg.CanBuildFrom[From, Elem, To]
  type Codec                           = scala.io.Codec
  type Failure[+A]                     = scala.util.Failure[A]
  type Function0[+R]                   = scala.Function0[R]
  type Function1[-T, +R]               = scala.Function1[T, R]
  type Function2[-T1, -T2, +R]         = scala.Function2[T1, T2, R]
  type Function3[-T1, -T2, -T3, +R]    = scala.Function3[T1, T2, T3, R]
  type GenTraversableLike[+A, +Repr]   = scala.collection.GenTraversableLike[A, Repr]
  type GenSeqLike[+A, +Repr]           = scala.collection.GenSeqLike[A, Repr]
  type SeqLike[+A, +Repr]              = scala.collection.SeqLike[A, Repr]
  type IsTraversableLike[Repr]         = scala.collection.generic.IsTraversableLike[Repr]
  type Option[+A]                      = scala.Option[A]
  type Ordered[A]                      = scala.math.Ordered[A]
  type Ordering[A]                     = scala.math.Ordering[A]
  type Some[+A]                        = scala.Some[A]
  type StringBuilder                   = scala.collection.mutable.StringBuilder
  type Success[+A]                     = scala.util.Success[A]
  type Try[+A]                         = scala.util.Try[A]

  // common annotations
  type inline    = scala.inline
  type spec      = scala.specialized
  type switch    = scala.annotation.switch
  type tailrec   = scala.annotation.tailrec
  type transient = scala.transient
  type uV        = scala.annotation.unchecked.uncheckedVariance
  type unchecked = scala.unchecked
  type volatile  = scala.volatile

  // scala types which I won't let win.
  type sCollection[+A]        = sc.GenTraversable[A]
  type scIndexedSeq[+A]       = sc.IndexedSeq[A]
  type scIterable[+A]         = sc.Iterable[A]
  type scIterator[+A]         = sc.Iterator[A]
  type scLinearSeq[+A]        = sc.LinearSeq[A]
  type scMap[K, +V]           = sc.Map[K, V]
  type scSeq[+A]              = sc.Seq[A]
  type scSeqLike[+A, +Repr]   = sc.SeqLike[A, Repr]
  type scSet[A]               = sc.Set[A]
  type scSortedMap[K, +V]     = sc.SortedMap[K, V]
  type scTraversableOnce[+A]  = sc.TraversableOnce[A]
  type scTraversable[+A]      = sc.Traversable[A]
  type sciBitSet              = sci.BitSet
  type sciIndexedSeq[+A]      = sci.IndexedSeq[A]
  type sciLinearSeq[+A]       = sci.LinearSeq[A]
  type sciList[+A]            = sci.List[A]
  type sciMap[K, +V]          = sci.Map[K, V]
  type sciRange               = sci.Range
  type sciSeq[+A]             = sci.Seq[A]
  type sciSet[A]              = sci.Set[A]
  type sciStream[+A]          = sci.Stream[A]
  type sciTraversable[+A]     = sci.Traversable[A]
  type sciVector[+A]          = sci.Vector[A]
  type scmBuilder[-Elem, +To] = scm.Builder[Elem, To]
  type scmMap[K, V]           = scm.Map[K, V]
  type scmSeq[A]              = scm.Seq[A]
  type scmSet[A]              = scm.Set[A]
  type scmWrappedArray[A]     = scm.WrappedArray[A]
  type sucControlThrowable    = scala.util.control.ControlThrowable

  // original convenience aliases.
  type Array2[A]                 = Array[Array[A]]
  type Array3[A]                 = Array[Array[Array[A]]]
  type Array4[A]                 = Array[Array[Array[Array[A]]]]
  type Array5[A]                 = Array[Array[Array[Array[Array[A]]]]]
  type Bytes                     = Array[Byte]
  type CanBuildSelf[-Elem, Self] = scg.CanBuildFrom[Self, Elem, Self]
  type CanBuild[-Elem, +To]      = scg.CanBuildFrom[_, Elem, To]
  type Chars                     = Array[Char]
  type GTOnce[+A]                = sc.GenTraversableOnce[A]
  type PathDirStream             = jnf.DirectoryStream[jnf.Path]
  type PathPredicate             = jnf.Path => Boolean
  type Predicate2[-A1, -A2]      = (A1, A2) => Boolean
  type Ref[+A]                   = A with AnyRef
  type UShort                    = Char
  type BinOp[A]                  = (A, A) => A
}
