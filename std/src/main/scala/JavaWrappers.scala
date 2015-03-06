package psp
package std

import api._, StdEq._

/** Wrapping java classes.
 */
object NullClassLoader extends jClassLoader
object NullInputStream extends InputStream { def read(): Int = -1 }

object JavaComparator {
  def apply[A](f: (A, A) => Int): Comparator[A] = new impl.ComparatorImpl[A](f)
}

final class PolicyClass(val clazz: jClass) extends AnyVal with ForceShowDirect {
  private def toPolicy(x: jClass): PolicyClass = new PolicyClass(x)

  def isAnnotation     = clazz.isAnnotation
  def isAnonymousClass = clazz.isAnonymousClass
  def isArray          = clazz.isArray
  def isEnum           = clazz.isEnum
  def isInterface      = clazz.isInterface
  def isLocalClass     = clazz.isLocalClass
  def isMemberClass    = clazz.isMemberClass
  def isPrimitive      = clazz.isPrimitive
  def isSynthetic      = clazz.isSynthetic

  def ancestorNames: Direct[String]           = ancestors map (_.rawName)
  def ancestors: Direct[PolicyClass]          = this transitiveClosure (_.parents) toDirect
  def exists                                  = clazz != null
  def fields: Direct[jField]                  = clazz.getFields.toDirect
  def getCanonicalName: String                = clazz.getCanonicalName
  def getClassLoader: ClassLoader             = clazz.getClassLoader
  def getClasses: Direct[PolicyClass]         = clazz.getClasses.toDirect map toPolicy
  def getComponentType: PolicyClass           = clazz.getComponentType
  def getDeclaredClasses: Direct[PolicyClass] = clazz.getDeclaredClasses.toDirect map toPolicy
  def getDeclaringClass: PolicyClass          = clazz.getDeclaringClass
  def getEnclosingClass: PolicyClass          = clazz.getEnclosingClass
  def getInterfaces: Direct[PolicyClass]      = clazz.getInterfaces.toDirect map toPolicy
  def getSuperclass: Option[PolicyClass]      = Option(clazz.getSuperclass) map toPolicy
  def hasModuleName: Boolean                  = rawName endsWith "$"
  def methods: Direct[jMethod]                = clazz.getMethods.toDirect
  def nameSegments: Direct[String]            = rawName.dottedSegments
  def pClass: PolicyClass                     = this
  def parentInterfaces: Direct[PolicyClass]   = clazz.getInterfaces.toDirect map toPolicy
  def parents: Direct[PolicyClass]            = getSuperclass.toDirect ++ parentInterfaces
  def qualifiedName: String                   = rawName.mapSplit('.')(decodeName)
  def rawName: String                         = clazz.getName
  def shortName: String                       = unqualifiedName
  def to_s                                    = s"$clazz"
  def unqualifiedName: String                 = decodeName(nameSegments.last)
}
