  scalacOptions += "-deprecation"
ivyLoggingLevel := UpdateLogging.Quiet

addSbtPlugin("org.scoverage"              % "sbt-scoverage"           %  "1.3.5")
addSbtPlugin("com.jsuereth"               % "sbt-pgp"                 %  "1.0.0")
addSbtPlugin("org.xerial.sbt"             % "sbt-sonatype"            %   "1.1")
addSbtPlugin("com.github.alexarchambault" % "coursier-sbt-plugin"     % "1.0.0-M10")
addSbtPlugin("com.updateimpact"           % "updateimpact-sbt-plugin" %  "2.1.1")
