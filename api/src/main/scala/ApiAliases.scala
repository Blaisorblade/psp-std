package psp
package api

// This mostly consists of "magic" types which cannot be avoided due to language privilege.
trait ApiAliases extends scala.Any {
  type Array[A]                = scala.Array[A]
  type CTag[A]                 = scala.reflect.ClassTag[A]
  type Dynamic                 = scala.Dynamic
  type PartialFunction[-A, +B] = scala.PartialFunction[A, B]
  type Product                 = scala.Product
  type ScalaNumber             = scala.math.ScalaNumber
  type String                  = java.lang.String
  type StringContext           = scala.StringContext

  // The top types.
  type Any    = scala.Any
  type AnyRef = scala.AnyRef
  type AnyVal = scala.AnyVal

  // The bottom types.
  type Null    = scala.Null
  type Nothing = scala.Nothing

  // The eight primitive types of the jvm, plus the scala version of void.
  type Boolean = scala.Boolean
  type Byte    = scala.Byte
  type Char    = scala.Char
  type Double  = scala.Double
  type Float   = scala.Float
  type Int     = scala.Int
  type Long    = scala.Long
  type Short   = scala.Short
  type Unit    = scala.Unit

  // policy-original type aliases which appear in api signatures or
  // which landed here to be near their buddy.
  type ?=>[-A, +B]   = PartialFunction[A, B]
  type Predicate[-A] = A => Boolean
  type Relation[-A]  = (A, A) => Boolean
  type PairOf[+A]    = (A, A)
  type Shower[-A]    = A => String
  type Suspended[+A] = (A => Unit) => Unit
  type Unary[A]      = A => A
  type Bag[A]        = ExMap[A, Precise]
}

// Necessary to use those aliases within the api package.
object ApiAliases extends ApiAliases

trait ApiMethods extends scala.Any {
  import ApiAliases._

  def ?[A](implicit value: A): A                 = value
  def emptyValue[A](implicit z: Empty[A]): A     = z.empty
  def identity[A](x: A): A                       = x
  def implicitly[A](implicit x: A): A            = x
  def newArray[A: CTag](size: Precise): Array[A] = new Array[A](size.intValue)
  def show[A: Show] : Show[A]                    = ?
  def longCmp(diff: Long): Cmp                   = if (diff < 0) Cmp.LT else if (diff > 0) Cmp.GT else Cmp.EQ
}
object ApiMethods extends ApiMethods
