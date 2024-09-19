package org.apache.avro.tool

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MainSpec extends AnyFlatSpec with Matchers {

  "avro-tools" should "display help" in {
    noException shouldBe thrownBy {
      // main calls System.exit. test with run
      val run = classOf[Main].getDeclaredMethod("run", classOf[Array[String]])
      run.setAccessible(true)
      run.invoke(new Main(), Array("--help"))
    }
  }
}
