import sbt.ExclusionRule

lazy val commonSettings = Seq(
  name := "archivespark2triples",
  organization := "de.l3s",
  version := "1.0.0",
  scalaVersion := "2.11.8",
  fork := true
)

lazy val archivespark2triples = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "2.0.1" % "provided" excludeAll(
        ExclusionRule(organization = "org.apache.hadoop"),
        ExclusionRule(organization = "org.scala-lang"),
        ExclusionRule(organization = "com.google.guava")),
      "org.apache.hadoop" % "hadoop-client" % "2.5.0" % "provided" excludeAll(
        ExclusionRule(organization = "org.scala-lang"),
        ExclusionRule(organization = "com.google.guava")),
      "de.l3s" %% "archivespark" % "2.1.2" % "provided" excludeAll(
        ExclusionRule(organization = "org.apache.hadoop"),
        ExclusionRule(organization = "org.scala-lang"))
    ),
    resolvers ++= Seq(
      "internetarchive" at "http://builds.archive.org/maven2",
      Resolver.mavenLocal
    )
  )

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
