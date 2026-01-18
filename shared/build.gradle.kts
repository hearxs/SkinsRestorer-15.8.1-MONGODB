plugins {
    id("sr.base-logic")
    id("com.gradleup.shadow")
}

dependencies {
    api(projects.skinsrestorerBuildData)
    api(projects.skinsrestorerApi)
    implementation(projects.skinsrestorerScissors)
    implementation("org.mongodb:mongodb-driver-sync:4.11.2")

    api(libs.gson)
    implementation(libs.mariadb.java.client) {
        exclude("com.github.waffle", "waffle-jna")
    }
    implementation(libs.postgresql)
    implementation(libs.hikari.cp)
    implementation(libs.mongodb.driver)

    api(libs.configme)
    api(libs.injector) {
        exclude("javax.annotation")
    }

    api(libs.cloud.annotations)
    annotationProcessor(libs.cloud.annotations)
    api(libs.cloud.processors.requirements)
    api(libs.cloud.processors.cooldown)
    api(libs.cloud.brigadier)
    api(libs.cloud.translations.core)
    api(libs.cloud.minecraft.extras)
    api(libs.cloud.translations.minecraft.extras)
    api(libs.reflect)

    implementation(libs.bstats.base) {
        isTransitive = false
    }

    compileOnly(libs.floodgate.api)

    api(libs.bundles.adventure.shared)
}

tasks {
    shadowJar {
        mergeServiceFiles()
        relocate("net.kyori", "net.skinsrestorer.shadow.kyori")
        failOnDuplicateEntries = true
    }
}
