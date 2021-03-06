package geotrellis.spark

import geotrellis.util._

import jp.ne.opt.chronoscala.Imports._
import java.time.{ZoneOffset, ZonedDateTime}

case class SpaceTimeKey(col: Int, row: Int, instant: Long) {
  def spatialKey: SpatialKey = SpatialKey(col, row)
  def temporalKey: TemporalKey = TemporalKey(time)
  def time: ZonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
}

object SpaceTimeKey {
  def apply(spatialKey: SpatialKey, temporalKey: TemporalKey): SpaceTimeKey =
    SpaceTimeKey(spatialKey.col, spatialKey.row, temporalKey.time)

  def apply(col: Int, row: Int, dateTime: ZonedDateTime): SpaceTimeKey =
    SpaceTimeKey(col, row, dateTime.toInstant.toEpochMilli)

  implicit val spatialComponent =
    Component[SpaceTimeKey, SpatialKey](k => k.spatialKey, (k, sk) => SpaceTimeKey(sk.col, sk.row, k.time))

  implicit val temporalComponent =
    Component[SpaceTimeKey, TemporalKey](k => k.temporalKey, (k, tk) => SpaceTimeKey(k.col, k.row, tk.instant))

  implicit def ordering: Ordering[SpaceTimeKey] =
    Ordering.by(stk => (stk.spatialKey, stk.temporalKey))

  implicit object Boundable extends Boundable[SpaceTimeKey] {
    def minBound(a: SpaceTimeKey, b: SpaceTimeKey) = {
      SpaceTimeKey(math.min(a.col, b.col), math.min(a.row, b.row), if (a.time < b.time) a.time else b.time )
    }

    def maxBound(a: SpaceTimeKey, b: SpaceTimeKey) = {
      SpaceTimeKey(math.max(a.col, b.col), math.max(a.row, b.row), if (a.time > b.time) a.time else b.time )
    }
  }
}
