package cilib

import scalaz.Foldable
import scalaz.syntax.foldable._

import spire.math.{abs,max}
import spire.implicits._
import spire.algebra.{Field,NRoot,Order,Ring,Signed}

object Distance {

  type Distance[F[_],A] = (F[A], F[A]) => A

  def chebyshev[F[_]: Foldable, A: Order : Signed](implicit ev: Ring[A]): Distance[F,A] =
    (a: F[A], b: F[A]) => (a.toList zip b.toList)
      .map { case (ai, bi) => abs(ai - bi) }
      .foldLeft(ev.zero) { (ai, bi) => max(ai, bi) }

  def minkowski[F[_]: Foldable, A: NRoot : Signed](alpha: Int)(implicit ev: Field[A]): Distance[F,A] =
    (a: F[A], b: F[A]) => (a.toList zip b.toList)
      .map { case (ai, bi) => abs(ai - bi) ** alpha }
      .foldLeft(ev.zero) { (ai, bi) => (ai + bi) ** (1.0 / alpha) }

  def manhattan[F[_]: Foldable, A: Field : NRoot : Signed]: Distance[F,A] = minkowski[F,A](1)
  def euclidean[F[_]: Foldable, A: Field : NRoot : Signed]: Distance[F,A] = minkowski[F,A](2)

}
