plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("distribution") {
            id = "dormnet.distribution"
            implementationClass = "io.github.sgpublic.dormnet.buildlogic.DormnetDistributionPlugin"
        }
    }
}
