lazy val lns =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo) // Enable the Scala.js and Indigo plugins
    .settings( // Standard SBT settings
      name := "pps-lns",
      version := "0.0.1",
      scalaVersion := "3.0.2",
      organization := "org.pps-lns"
    )
    .settings( // Indigo specific settings
      showCursor := true,
      title := "Lost 'n Souls",
      gameAssetsDirectory := "assets",
      windowStartWidth := 720, // Width of Electron window, used with `indigoRun`.
      windowStartHeight := 480, // Height of Electron window, used with `indigoRun`.
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo" % "0.9.2",
        "io.indigoengine" %%% "indigo-extras"     % "0.9.2",
        "io.indigoengine" %%% "indigo-json-circe" % "0.9.2",
        "org.junit.jupiter" % "junit-jupiter" % "5.8.0" % Test, // aggregator of junit-jupiter-api and junit-jupiter-engine (runtime)
        "org.junit.jupiter" % "junit-jupiter-engine" % "5.8.0" % Test, // for org.junit.platform
        "org.scalatest" %% "scalatest" % "3.2.9" % Test
      )
    )

addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuild")
addCommandAlias("runGame", ";compile;fastOptJS;indigoRun")
addCommandAlias("buildGameFull", ";compile;fullOptJS;indigoBuildFull")
addCommandAlias("runGameFull", ";compile;fullOptJS;indigoRunFull")
