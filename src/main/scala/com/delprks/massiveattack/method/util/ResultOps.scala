package com.delprks.massiveattack.method.util

trait ResultOps {
  def avg(s: Seq[Long]): Double = s.foldLeft((0.0, 1))((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1

  def truncateAt(n: Double, p: Int): Double = {
    val s = math pow(10, p)
    (math floor n * s) / s
  }
}
