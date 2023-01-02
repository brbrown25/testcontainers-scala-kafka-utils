ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("ch.epfl.scala"      % "sbt-scalafix"       % "0.10.4")
addSbtPlugin("com.github.sbt"     % "sbt-unidoc"         % "0.5.0")
addSbtPlugin("com.github.sbt"    %% "sbt-release"        % "1.1.0")
addSbtPlugin("com.github.sbt"     % "sbt-pgp"            % "2.2.1")
addSbtPlugin("com.typesafe"       % "sbt-mima-plugin"    % "1.1.1")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"            % "0.4.3")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"       % "3.9.12")
addSbtPlugin("com.47deg"          % "sbt-microsites"     % "1.4.0")
addSbtPlugin("org.scalameta"      % "sbt-mdoc"           % "2.2.24")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"       % "2.5.0")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"      % "0.11.0")
addSbtPlugin("com.codecommit"     % "sbt-github-actions" % "0.14.2")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"      % "2.0.6")
