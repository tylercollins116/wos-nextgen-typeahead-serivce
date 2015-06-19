name := "typeahead"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-core" % "4.10.4",
  "org.apache.lucene" % "lucene-suggest" % "4.10.4",
  javaJdbc,
  javaEbean,
  cache
)     

play.Project.playJavaSettings
