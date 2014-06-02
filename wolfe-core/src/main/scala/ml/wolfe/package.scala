package ml

import cc.factorie.la.{SingletonTensor1, DenseTensor1, SparseTensor1, Tensor1}
import cc.factorie.maths.ArrayOps

/**
 * @author Sebastian Riedel
 */
package object wolfe {
  type FactorieVector = Tensor1
  type SparseVector = SparseTensor1
  type DenseVector = DenseTensor1
  type SingletonVector = SingletonTensor1
  type AnyFunction = PartialFunction[Nothing,Any]
  type DefaultIndex = HierarchicalIndexAndBuilder //SimpleIndexAndBuilder

  object MoreArrayOps extends ArrayOps {
    def maxValue(s: A): Double = { var result = s(0); var i = 0; while (i < s.length) {if (s(i) > result) result = s(i); i += 1}; result }
    def maxNormalize(s: A) { val norm = maxValue(s); this -=(s, norm) }
    def approxEqual(a1: A,a2:A,eps:Double = 0.00001) =  {
      a1.length == a2.length && a1.indices.forall(i => math.abs(a1(i) - a2(i)) < eps)
    }

    def fill(s: A, v: Double) { java.util.Arrays.fill(s, v) }
  }


}
