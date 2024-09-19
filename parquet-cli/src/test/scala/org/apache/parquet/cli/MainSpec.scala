package org.apache.parquet.cli

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.util.ToolRunner
import org.apache.log4j.PropertyConfigurator
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.LoggerFactory

class MainSpec extends AnyFlatSpec with Matchers {

  "parquet-cli" should "display help" in {
    noException shouldBe thrownBy {
      PropertyConfigurator.configure(classOf[Main].getResource("/cli-logging.properties"));
      val console = LoggerFactory.getLogger(classOf[Main])
      ToolRunner.run(new Configuration, new Main(console), Array("--help"))
    }
  }
}
