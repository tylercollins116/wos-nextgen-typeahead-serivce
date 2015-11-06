// Databricks notebook source exported at Thu, 22 Oct 2015 21:49:22 UTC
val AccessKey = "AKIAIMXFDFSYSLKDMGDA"
val SecretKey = "d%2FkFPD+bMkxKlsGL%2FBqgSxKvAXZZXYSSkW0eHPou"
val kws = sqlContext.read.parquet("s3n://"+AccessKey+":"+SecretKey+"@1p-data/warehouse/wos_kw_count") 


import org.apache.spark.sql._
import org.apache.spark.sql.Row
import scala.collection.JavaConversions._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

val result=(for {
    row <- kws.select("query")
    sRow <- row.getList[Row](0)
    qType=sRow.getString(1)
    if ("TS".equals(qType))
    q=sRow.getString(0).toLowerCase();
    count=sRow.getInt(2)
    
} yield (q,count)).reduceByKey(_ + _).filter(_._2>=5)
result.take(10).foreach(println)

// COMMAND ----------

result.coalesce(1,true).map(x=> compact(("keyword",x._1) ~ ("count",x._2))).saveAsTextFile("s3://1p-data/warehouse/trieu/type-ahead-kw")