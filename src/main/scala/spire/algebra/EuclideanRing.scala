package spire.algebra

import spire.math._

import scala.annotation.tailrec
import scala.{specialized => spec}
import scala.math.{abs, ceil, floor}

import spire.math.{ConvertableTo, ConvertableFrom, Number}
import java.lang.Math

trait EuclideanRing[@spec(Int,Long,Float,Double) A] extends Ring[A] {
  def quot(a:A, b:A):A
  def mod(a:A, b:A):A
  def quotmod(a:A, b:A) = (quot(a, b), mod(a, b))

  def gcd(a: A, b: A): A = euclid(a, b)

  def lcm(a: A, b: A): A = times(quot(a, gcd(a, b)), b)

  @tailrec protected[this] final def euclid(a:A, b:A):A =
    if (b == zero) a else euclid(b, mod(a, b))
}

final class EuclideanRingOps[@spec(Int,Long,Float,Double) A](lhs:A)(implicit ev:EuclideanRing[A]) {
  def /~(rhs:A) = ev.quot(lhs, rhs)
  def %(rhs:A) = ev.mod(lhs, rhs)
  def /%(rhs:A) = ev.quotmod(lhs, rhs)

  def /~(rhs:Int)(implicit c: Ring[A]): A = ev.quot(lhs, c.fromInt(rhs))
  def %(rhs:Int)(implicit c: Ring[A]): A = ev.mod(lhs, c.fromInt(rhs))
  def /%(rhs:Int)(implicit c: Ring[A]): (A, A) = ev.quotmod(lhs, c.fromInt(rhs))

  def /~(rhs:Double)(implicit c:ConvertableTo[A]): A = ev.quot(lhs, c.fromDouble(rhs))
  def %(rhs:Double)(implicit c:ConvertableTo[A]): A = ev.mod(lhs, c.fromDouble(rhs))
  def /%(rhs:Double)(implicit c:ConvertableTo[A]): (A, A) = ev.quotmod(lhs, c.fromDouble(rhs))

  def /~(rhs:Number)(implicit c:ConvertableFrom[A]): Number = c.toNumber(lhs) /~ rhs
  def %(rhs:Number)(implicit c:ConvertableFrom[A]): Number = c.toNumber(lhs) % rhs
  def /%(rhs:Number)(implicit c:ConvertableFrom[A]): (Number, Number) = c.toNumber(lhs) /% rhs
}

object EuclideanRing {
  implicit object IntIsEuclideanRing extends IntIsEuclideanRing
  implicit object LongIsEuclideanRing extends LongIsEuclideanRing
  implicit object FloatIsEuclideanRing extends FloatIsEuclideanRing
  implicit object DoubleIsEuclideanRing extends DoubleIsEuclideanRing
  implicit object BigIntIsEuclideanRing extends BigIntIsEuclideanRing
  implicit object BigDecimalIsEuclideanRing extends BigDecimalIsEuclideanRing
  implicit object RationalIsEuclideanRing extends RationalIsEuclideanRing
  implicit object RealIsEuclideanRing extends RealIsEuclideanRing
  implicit object SafeLongIsEuclideanRing extends SafeLongIsEuclideanRing
  implicit object NumberIsEuclideanRing extends NumberIsEuclideanRing

  implicit def complexIsEuclideanRing[A: Fractional: Trig] =
    new ComplexIsEuclideanRing[A] {
      val f = Fractional[A]
      val t = Trig[A]
    }

  implicit def gaussianIsEuclideanRing[A: Integral] =
    new GaussianIsEuclideanRing[A] {
      val f = Integral[A]
    }

  @inline final def apply[A](implicit e:EuclideanRing[A]):EuclideanRing[A] = e
}


trait IntIsEuclideanRing extends EuclideanRing[Int] with IntIsRing {
  def quot(a:Int, b:Int) = a / b
  def mod(a:Int, b:Int) = a % b
  override def gcd(a:Int, b:Int): Int = spire.math.fun.gcd(a, b).toInt
}

trait LongIsEuclideanRing extends EuclideanRing[Long] with LongIsRing {
  def quot(a:Long, b:Long) = a / b
  def mod(a:Long, b:Long) = a % b
  override def gcd(a:Long, b:Long) = spire.math.fun.gcd(a, b)
}

trait FloatIsEuclideanRing extends EuclideanRing[Float] with FloatIsRing {
  def quot(a:Float, b:Float) = (a - (a % b)) / b
  def mod(a:Float, b:Float) = a % b
  override def gcd(a:Float, b:Float):Float = _gcd(Math.abs(a), Math.abs(b))
  @tailrec private def _gcd(a:Float, b:Float):Float = if (a < 1.0F) {
    1.0F
  } else if (b == 0.0F) {
    a
  } else if (b < 1.0F) {
    1.0F
  } else {
    _gcd(b, a % b)
  }
}

trait DoubleIsEuclideanRing extends EuclideanRing[Double] with DoubleIsRing {
  def quot(a:Double, b:Double) = (a - (a % b)) / b
  def mod(a:Double, b:Double) = a % b
  override final def gcd(a:Double, b:Double):Double = _gcd(Math.abs(a), Math.abs(b))
  @tailrec private def _gcd(a:Double, b:Double):Double = if (a < 1.0) {
    1.0
  } else if (b == 0.0) {
    a
  } else if (b < 1.0) {
    1.0
  } else {
    _gcd(b, a % b)
  }
}

trait BigIntIsEuclideanRing extends EuclideanRing[BigInt] with BigIntIsRing {
  def quot(a:BigInt, b:BigInt) = a / b
  def mod(a:BigInt, b:BigInt) = a % b
  override def gcd(a:BigInt, b:BigInt) = a.gcd(b)
}

trait BigDecimalIsEuclideanRing extends EuclideanRing[BigDecimal] with BigDecimalIsRing {
  def quot(a:BigDecimal, b:BigDecimal) = a.quot(b)
  def mod(a:BigDecimal, b:BigDecimal) = a % b
  override def gcd(a:BigDecimal, b:BigDecimal):BigDecimal = _gcd(a.abs, b.abs)
  @tailrec private def _gcd(a:BigDecimal, b:BigDecimal):BigDecimal = {
    if (a < one) {
      one
    } else if (b.signum == 0) {
      a
    } else if (b < one) {
      one
    } else {
      _gcd(b, a % b)
    }
  }
}

trait RationalIsEuclideanRing extends EuclideanRing[Rational] with RationalIsRing {
  def quot(a:Rational, b:Rational) = a.quot(b)
  def mod(a:Rational, b:Rational) = a % b
  override def gcd(a:Rational, b:Rational):Rational = _gcd(a.abs, b.abs)
  @tailrec private def _gcd(a:Rational, b:Rational):Rational = {
    if (a.compareToOne < 0) {
      Rational.one
    } else if (b.signum == 0) {
      a
    } else if (b.compareToOne < 0) {
      Rational.one
    } else {
      _gcd(b, a % b)
    }
  }
}

trait RealIsEuclideanRing extends EuclideanRing[Real] with RealIsRing {
  def quot(a: Real, b: Real): Real = a /~ b
  def mod(a: Real, b: Real): Real = a % b
}

trait SafeLongIsEuclideanRing extends EuclideanRing[SafeLong] with SafeLongIsRing {
  def quot(a:SafeLong, b:SafeLong) = a / b
  def mod(a:SafeLong, b:SafeLong) = a % b
  override def gcd(a:SafeLong, b:SafeLong) = a.toBigInt.gcd(b.toBigInt)
}

trait ComplexIsEuclideanRing[@spec(Float,Double) A]
extends ComplexIsRing[A] with EuclideanRing[Complex[A]] {
  override def quotmod(a:Complex[A], b:Complex[A]) = a /% b
  def quot(a:Complex[A], b:Complex[A]) = a /~ b
  def mod(a:Complex[A], b:Complex[A]) = a % b
  override def gcd(a:Complex[A], b:Complex[A]):Complex[A] =
    _gcd(a, b, Fractional[A])
  @tailrec
  private def _gcd(a:Complex[A], b:Complex[A], f:Fractional[A]):Complex[A] = {
    if (f.lt(a.abs, f.one)) {
      one
    } else if (b == zero) {
      a
    } else if (f.lt(b.abs, f.one)) {
      one
    } else {
      _gcd(b, a % b, f)
    }
  }
}

trait GaussianIsEuclideanRing[@spec(Int, Long) A]
extends GaussianIsRing[A] with EuclideanRing[Gaussian[A]] {
  override def quotmod(a:Gaussian[A], b:Gaussian[A]) = a /% b
  def quot(a:Gaussian[A], b:Gaussian[A]) = a / b
  def mod(a:Gaussian[A], b:Gaussian[A]) = a % b
}

trait NumberIsEuclideanRing extends EuclideanRing[Number] with NumberIsRing {
  def quot(a:Number, b:Number) = a / b
  def mod(a:Number, b:Number) = a % b
}
