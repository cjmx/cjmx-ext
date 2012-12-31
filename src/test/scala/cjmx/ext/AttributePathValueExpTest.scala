package cjmx.ext

import scala.collection.JavaConverters._

import java.lang.management.ManagementFactory
import javax.management.{ObjectName, Query, QueryExp}

import org.scalatest._


class AttributePathValueExpTest extends FunSuite with ShouldMatchers {
  val server = ManagementFactory.getPlatformMBeanServer

  test("querying composite data") {
    queryNonZero("HeapMemoryUsage", "max") should have size (1)
  }

  test("querying array size") {
    queryNonZero("AllThreadIds", "length") should have size (1)
  }

  test("querying bean") {
    val exp = Query.eq(attrPath("SystemProperties", "TabularType", "TypeName"), Query.value("java.util.Map<java.lang.String, java.lang.String>"))
    query(exp).size should be > (0)
  }


  private def attrPath(attr: String, path: String*) =
    new AttributePathValueExp(attr, path.asJava)

  private def queryNonZero(attr: String, path: String*) =
    query(Query.gt(attrPath(attr, path: _*), Query.value(0)))

  private def query(exp: QueryExp) = 
    server.queryNames(new ObjectName("java.lang:*"), exp).asScala
}
