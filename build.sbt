organization := "com.github.cjmx"

name := "cjmx-ext"

version := "1.0.0.RELEASE"

scalaVersion := "2.10.2"

autoScalaLibrary := false

crossPaths := false

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-optimise",
  "-Xcheckinit",
  "-Xlint",
  "-Xverify",
  "-Yclosure-elim",
  "-Yinline",
  "-Ywarn-all",
  "-feature")

javacOptions ++= Seq("-source", "1.5", "-target", "1.5", "-Xlint:deprecation")

javacOptions in doc := Seq("-source", "1.5")

licenses += ("Three-clause BSD-style", url("http://github.com/cjmx/cjmx/blob/master/LICENSE"))

triggeredMessage := (_ => Watched.clearScreen)

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.RC1-SNAP4" % "test"

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := (
  <url>http://github.com/cjmx/cjmx-ext</url>
  <scm>
    <url>git@github.com:cjmx/cjmx-ext.git</url>
    <connection>scm:git:git@github.com:cjmx/cjmx-ext.git</connection>
  </scm>
  <developers>
    <developer>
      <id>mpilquist</id>
      <name>Michael Pilquist</name>
      <url>http://github.com/mpilquist</url>
    </developer>
  </developers>
)

pomPostProcess := { (node) =>
  import scala.xml._
  import scala.xml.transform._
  def stripIf(f: Node => Boolean) = new RewriteRule {
    override def transform(n: Node) =
      if (f(n)) NodeSeq.Empty else n
  }
  val stripTestScope = stripIf { n => n.label == "dependency" && (n \ "scope").text == "test" }
  new RuleTransformer(stripTestScope).transform(node)(0)
}

