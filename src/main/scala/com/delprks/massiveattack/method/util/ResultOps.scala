package com.delprks.massiveattack.method.util

object ResultOps {
  def avg(s: Seq[Int]): Int = s.foldLeft((0.0, 1))((acc, i) => (acc._1 + (i - acc._1) / acc._2, acc._2 + 1))._1.toInt
}
