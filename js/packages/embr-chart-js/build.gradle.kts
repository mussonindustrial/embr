plugins {
    id("embr.js-library-conventions")
}

dependencies {
    compileOnly(projects.js.packages.embrTagStream)
}