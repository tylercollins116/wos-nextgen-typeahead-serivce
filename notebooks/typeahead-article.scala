// Databricks notebook source exported at Thu, 22 Oct 2015 21:43:57 UTC
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
    row <- kws.select("wosId","query","json")
    wosId=row.getString(0)
    json=(parse(row.getString(2)) \ "title")
    if (json!=JNothing)
    JString(title)=json(0)
    sRow <- row.getList[Row](1)
    qType=sRow.getString(1)
    if ("TS".equals(qType))
    q=sRow.getString(0).toLowerCase();
    count=sRow.getInt(2)
    if (count>=10)
} yield (compact(("keyword",q) ~ ("count",count) ~ ("id",wosId) ~ ("title",title))))
result.take(10)

// COMMAND ----------

result.coalesce(1,true).saveAsTextFile("s3://1p-data/warehouse/trieu/type-ahead-article")