import korlibs.korge.gradle.*

plugins {
  alias(libs.plugins.korge)
}

korge {
  id = "com.sample.demo"

  targetJvm()
  targetJs()

  jvmMainClassName = "omnipede.MainKt" // delete this line to run 2048

  serializationJson()
}

dependencies {
  add("commonMainApi", project(":deps"))
  //add("commonMainApi", project(":korge-dragonbones"))
}

