package com.neu.Connector
import org.apache.commons.csv.{CSVFormat, CSVPrinter}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import java.io.StringWriter
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PGDataUtils {
  def readDataPG(
      spark: SparkSession,
      jdbcUrl: String,
      inputSchema: StructType,
      tableName: String,
      properties: Properties
  ): Future[DataFrame] =
    Future {
      spark.read.schema(inputSchema).jdbc(jdbcUrl, tableName, properties)
    }

  // More performant method for writing using DataFrame write, writeDataPG
  def writeDataPG(dataFrame: DataFrame, jdbcUrl: String, tableName: String, properties: Properties): Future[Unit] =
    Future {
      try {

        val stringWriter = new StringWriter()
        val csvPrinter   = new CSVPrinter(stringWriter, CSVFormat.DEFAULT)

        println(s"Dataframe count: ${dataFrame.count()}")

        dataFrame.write
          .format("jdbc")
          .option("url", jdbcUrl)
          .option("dbtable", tableName)
          .option("user", properties.getProperty("user"))
          .option("password", properties.getProperty("password"))
          .mode(SaveMode.Append)
          .save()

      } catch {
        case e: Exception =>
          e.printStackTrace()
          throw e
      }

    }
}
