// Databricks notebook source exported at Thu, 22 Oct 2015 21:47:34 UTC
def isCleanKw(kw:String):Boolean={
  var prevSpace=true
  kw.foreach{ c=>
    if (!Character.isLetterOrDigit(c)) {
      if (c==' ') {
          if (prevSpace) return false
          else {
              prevSpace=true;
          }
      }
      else return false
    } else {
      prevSpace=false;
    }
  }
  true
}

var strs = Array("sony tv", "sony  tvs", "s* tv","sony (tv")
strs.foreach(x => println(x+": "+isCleanKw(x)))


// COMMAND ----------

val AccessKey = "AKIAIMXFDFSYSLKDMGDA"
val SecretKey = "d%2FkFPD+bMkxKlsGL%2FBqgSxKvAXZZXYSSkW0eHPou"
val kws = sqlContext.read.parquet("s3n://"+AccessKey+":"+SecretKey+"@1p-data/warehouse/wos_kw_count/part-r-000??-a47778d9-cfc6-4f72-b607-9527e8d330f1.gz.parquet")


import org.apache.spark.sql._
import org.apache.spark.sql.Row
import scala.collection.JavaConversions._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

def getListFromJson(line:String)={
    try {
    parse(line) match {
        case JArray(catList) => {
            catList.map(x => x match {
                case JString(s) => s
                case _ => None
            })
        }
        case JString(cat) => List(cat)
        case _ => List()
    }
    }
    catch {
        case _: Throwable => List()
    }
}





val kwCatCount=(for {
    row <- kws.select("query","categories")
    sRow <- row.getList[Row](0)
    qType=sRow.getString(1)
    if ("TS".equals(qType))
    q=sRow.getString(0).toLowerCase().trim();
    if (isCleanKw(q))
    count=sRow.getInt(2)
    cat <-getListFromJson(row.getString(1))
} yield((q,cat),count)).reduceByKey(_ + _).map(x=>(x._1._1,(x._1._2.toString(),x._2))).
combineByKey[(List[(String, Int)], Int)](
  (x:(String,Int)) => (List(x), x._2), 
  (acc:(List[(String, Int)],Int), x:(String, Int))=> (acc._1 ::: List(x), acc._2 + x._2),
  (acc1:(List[(String, Int)],Int), acc2:(List[(String, Int)],Int)) => (acc1._1 ::: acc2._1, acc1._2 + acc2._2)
)

kwCatCount.filter(_._2._2>=20).take(10).foreach(println)

// COMMAND ----------

result.filter(_._2>=10).map(x=> compact(("keyword",x._1._1) ~ ("category",x._1._2.toString()) ~ ("count",x._2))).coalesce(1,true).saveAsTextFile("s3://1p-data/warehouse/trieu/type-ahead-cat-combine")