package com.newzly.phantom.dsl.crud

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Primitive, Primitives }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class DeleteTest extends BaseTest {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace: String = "deleteTest"

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Primitives.insertSchema()
    }
  }

  "Delete" should "work fine, when deleting the whole row" in {
    val row = Primitive.sample
    val rcp =  Primitives.insert
      .value(_.pkey, row.pkey)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi).future()

    val result = rcp flatMap {
      _ => {
        for {
          inserted <- Primitives.select.fetch
          delete <- Primitives.delete.where(_.pkey eqs row.pkey).future()
          deleted <- Primitives.select.where(_.pkey eqs row.pkey).one
        } yield (inserted.contains(row), deleted.isEmpty)
      }
    }

    result successful {
      r => {
        assert(r._1)
        assert(r._2)
        info("success")
      }
    }
  }

  "Delete" should "work fine with Twitter Futures, when deleting the whole row" in {
    val row = Primitive.sample
    val rcp =  Primitives.insert
      .value(_.pkey, row.pkey)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi).execute()

    val result = rcp flatMap {
      _ => {
        for {
          inserted <- Primitives.select.collect()
          delete <- Primitives.delete.where(_.pkey eqs row.pkey).execute()
          deleted <- Primitives.select.where(_.pkey eqs row.pkey).get
        } yield (inserted.contains(row), deleted.isEmpty)
      }
    }

    result successful {
      r => {
        assert(r._1)
        assert(r._2)
        info("success")
      }
    }
  }
}
