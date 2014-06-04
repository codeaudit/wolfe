package ml.wolfe

import ml.wolfe.potential._
import scala.util.Random
import ml.wolfe.potential.Table
import ml.wolfe.potential.Stats

/**
 * @author Sebastian Riedel
 */
class MaxProductSpecs extends WolfeSpec {

  import FactorGraph._

  val fixedTable = TablePotential.table(Array(2, 2), {
    case Array(0, 0) => 1
    case Array(0, 1) => 2
    case Array(1, 0) => -3
    case Array(1, 1) => 0
  })
  val fixedStats = LinearPotential.stats(Array(2, 2), {
    case Array(i, j) => LinearPotential.singleton(2 * i + j, 1.0)
  })


  def tablePotential(fg: FactorGraph, n1: Node, n2: Node, table: Table) = {
    val f1 = fg.addFactor()
    val e1 = fg.addEdge(f1, n1)
    val e2 = fg.addEdge(f1, n2)
    f1.potential = TablePotential(Array(e1, e2), table)
    f1
  }

  def linearPotential(fg: FactorGraph, n1: Node, n2: Node, stats: Stats) = {
    val f1 = fg.addFactor()
    val e1 = fg.addEdge(f1, n1)
    val e2 = fg.addEdge(f1, n2)
    f1.potential = new LinearPotential(Array(e1, e2), stats, fg)
    f1
  }

  def oneFactorFG() = {
    val fg = new FactorGraph
    val n1 = fg.addNode(2)
    val n2 = fg.addNode(2)
    tablePotential(fg, n1, n2, fixedTable)
    fg.build()
    fg
  }

  def chainFG(length: Int) = {
    val fg = new FactorGraph
    val nodes = for (i <- 0 until length) yield fg.addNode(2)
    for ((n1, n2) <- nodes.dropRight(1) zip nodes.drop(1)) tablePotential(fg, n1, n2, fixedTable)
    fg.build()
    fg
  }

  def chainFGWithFeatures(length: Int) = {
    val fg = new FactorGraph
    val nodes = for (i <- 0 until length) yield fg.addNode(2)
    for ((n1, n2) <- nodes.dropRight(1) zip nodes.drop(1)) linearPotential(fg, n1, n2, fixedStats)
    fg.weights = LinearPotential.dense(4, 0 -> 1.0, 1 -> 2.0, 2 -> -3, 3 -> 0)
    fg.build()
    fg
  }


  def sameBeliefs(fg1: FactorGraph, fg2: FactorGraph) = {
    def sameBeliefs(n1: List[FactorGraph.Node], n2: List[FactorGraph.Node]): Boolean = (n1, n2) match {
      case (Nil, Nil) => true
      //todo: this should be approx. equal on array
      case (h1 :: t1, h2 :: t2) => MoreArrayOps.approxEqual(h1.b, h2.b) && sameBeliefs(t1, t2)
      case _ => false
    }
    sameBeliefs(fg1.nodes.toList, fg2.nodes.toList)
  }

  def sameVector(v1: FactorieVector, v2: FactorieVector, eps: Double = 0.00001) = {
    v1.activeDomain.forall(i => math.abs(v1(i) - v2(i)) < eps) &&
    v2.activeDomain.forall(i => math.abs(v1(i) - v2(i)) < eps)
  }

  "A Max Product algorithm" should {
    "return the exact max-marginals when given a single table potential" in {
      val fg_mp = oneFactorFG()
      val fg_bf = oneFactorFG()

      MaxProduct(fg_mp, 1)
      BruteForceSearch(fg_bf)

      sameBeliefs(fg_mp, fg_bf) should be(true)
      fg_mp.value should be (fg_bf.value)

    }
    "return the exact marginals given a chain" in {
      val fg_mp = chainFG(5)
      val fg_bf = chainFG(5)

      MaxProduct(fg_mp, 1)
      BruteForceSearch(fg_bf)

      sameBeliefs(fg_mp, fg_bf) should be(true)
      fg_mp.value should be (fg_bf.value)

    }
    "return feature vectors of argmax state" in {
      val fg_mp = chainFGWithFeatures(5)
      val fg_bf = chainFGWithFeatures(5)

      MaxProduct(fg_mp, 1)
      BruteForceSearch(fg_bf)

      sameBeliefs(fg_mp, fg_bf) should be(true)
      sameVector(fg_mp.gradient, fg_bf.gradient)
      fg_mp.value should be (fg_bf.value)


    }

  }

}
