plugins {
    java
    application
}

group = "net.jondotcomdotorg"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq:3.17.3")
    implementation("org.postgresql:postgresql:42.4.2")
}

application {
    mainClass.set("net.jondotcomdotorg.Main")
}