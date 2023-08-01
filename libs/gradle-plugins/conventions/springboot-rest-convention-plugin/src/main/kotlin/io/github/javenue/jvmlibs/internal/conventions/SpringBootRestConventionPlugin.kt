package io.github.javenue.jvmlibs.internal.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.TaskProvider
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.openapitools.generator.gradle.plugin.tasks.ValidateTask

@Suppress("unused")
class SpringBootRestConventionPlugin : Plugin<Project> {

  companion object {
    private const val TASK_GROUP = "openapi generation"
  }

  private lateinit var project: Project

  private lateinit var openapiProject: Project

  private lateinit var openapiResourcesDir: String

  private lateinit var openapiGeneratorExtension: SpringBootRestConventionExtension

  private lateinit var internalInputSpecPath: String

  private lateinit var externalInputSpecPaths: Map<String, String>

  private lateinit var outputDir: String

  private lateinit var basePackage: String

  override fun apply(project: Project) {
    this.project = project
    this.openapiProject = project.parent!!.parent!!.project("openapi")
    this.openapiResourcesDir = "${openapiProject.projectDir.path}/src/main/resources"
    project.plugins.apply("java-library")
    this.openapiGeneratorExtension =
        project.extensions.create("openapiGenerator", SpringBootRestConventionExtension::class.java)

    project.repositories.mavenCentral()
    project.dependencies.let {
      it.add("api", "io.swagger.core.v3:swagger-annotations:2.1.13")
      it.add("compileOnly", "org.springframework.boot:spring-boot-starter-web")
      it.add("compileOnly", "com.fasterxml.jackson.core:jackson-annotations:2.13.1")
      it.add("compileOnly", "org.openapitools:jackson-databind-nullable:0.2.2")
      it.add("compileOnly", "io.springfox:springfox-swagger2:3.0.0")
      it.add("compileOnly", "com.google.code.findbugs:jsr305:3.0.2")
    }

    project.afterEvaluate { _ ->
      processExtension()
      addSrcDir()

      val validateTasks = mutableSetOf<TaskProvider<ValidateTask>>()
      val generateTasks = mutableSetOf<TaskProvider<GenerateTask>>()

      validateTasks.add(
          project.tasks.register("validateOpenapiSpec", ValidateTask::class.java) {
            it.description = "Validates internal openapi-spec file by running OpenAPI Generator."
            it.group = TASK_GROUP
            it.inputSpec.set(internalInputSpecPath)
            it.recommend.set(true)
          }
      )

      generateTasks.add(
          project.tasks.register("generateSpringServerCode", GenerateTask::class.java) {
            it.dependsOn(project.tasks.getByName("validateOpenapiSpec"))
            it.description = "Generates Spring server code by running OpenAPI Generator."
            it.group = TASK_GROUP
            it.inputSpec.set(internalInputSpecPath)
            it.outputDir.set(outputDir)
            it.generatorName.set("spring")
            it.generateApiTests.set(false)
            it.generateModelTests.set(false)
            it.invokerPackage.set("${basePackage}.openapi.internal.spring")
            it.apiPackage.set("${basePackage}.openapi.internal.spring.apis")
            it.modelPackage.set("${basePackage}.openapi.internal.spring.models")
            it.configOptions.set(
                mapOf(
                    "useTags" to "true",
                    "interfaceOnly" to "true",
                    "useOptional" to "true",
                )
            )
            it.typeMappings.set(
                mapOf(
                    "OffsetDateTime" to "java.time.LocalDateTime",
                )
            )
          }
      )

      generateTasks.add(
          project.tasks.register("generateJavaClientCode", GenerateTask::class.java) {
            it.dependsOn(project.tasks.getByName("validateOpenapiSpec"))
            it.description = "Generates Java client code by running OpenAPI Generator."
            it.group = TASK_GROUP
            it.inputSpec.set(internalInputSpecPath)
            it.outputDir.set(outputDir)
            it.generatorName.set("java")
            it.generateApiTests.set(false)
            it.generateModelTests.set(false)
            it.invokerPackage.set("${basePackage}.openapi.internal.java")
            it.apiPackage.set("${basePackage}.openapi.internal.java.clients")
            it.modelPackage.set("${basePackage}.openapi.internal.java.models")
            it.configOptions.set(
                mapOf(
                    "useTags" to "true",
                    "library" to "resttemplate",
                    "dateLibrary" to "java8",
                )
            )
            it.typeMappings.set(
                mapOf(
                    "OffsetDateTime" to "java.time.LocalDateTime",
                )
            )
          }
      )

      externalInputSpecPaths.forEach { (apiName, inputSpecPath) ->
        validateTasks.add(
            project.tasks.register("validateOpenapiSpecFor_${apiName}", ValidateTask::class.java) {
              it.description = "Validates openapi-spec file for $apiName API by running OpenAPI Generator."
              it.group = TASK_GROUP
              it.inputSpec.set(inputSpecPath)
              it.recommend.set(true)
            }
        )

        generateTasks.add(
            project.tasks.register("generateJavaClientCodeFor_${apiName}", GenerateTask::class.java) {
              it.dependsOn(project.tasks.getByName("validateOpenapiSpecFor_${apiName}"))
              it.description = "Generates Java client code for $apiName API by running OpenAPI Generator."
              it.group = TASK_GROUP
              it.inputSpec.set(inputSpecPath)
              it.outputDir.set(outputDir)
              it.generatorName.set("java")
              it.generateApiTests.set(false)
              it.generateModelTests.set(false)
              it.invokerPackage.set("${basePackage}.openapi.external.java.${apiName.lowercase()}")
              it.apiPackage.set("${basePackage}.openapi.external.java.${apiName.lowercase()}.clients")
              it.modelPackage.set("${basePackage}.openapi.external.java.${apiName.lowercase()}.models")
              it.configOptions.set(
                  mapOf(
                      "useTags" to "true",
                      "library" to "resttemplate",
                      "dateLibrary" to "java8",
                  )
              )
              it.typeMappings.set(
                  mapOf(
                      "OffsetDateTime" to "java.time.LocalDateTime",
                  )
              )
            }
        )
      }

      project.tasks.register("generateAllCode") {
        it.description = "Runs all generate tasks which generate Java code."
        it.group = TASK_GROUP
        it.dependsOn(generateTasks)
      }

      project.tasks.getByName("compileJava").dependsOn(
          "generateAllCode",
      )
    }

  }

  private fun processExtension() {
    internalInputSpecPath = "${openapiResourcesDir}/${
      openapiGeneratorExtension.internalInputSpecPath.getOrElse(
          "${project.name}/internal/openapi-spec.yaml"
      )
    }"

    externalInputSpecPaths = if (openapiGeneratorExtension.externalInputSpecPaths.get().isEmpty()) {
      val paths = HashMap<String, String>()
      openapiProject
          .fileTree("${openapiResourcesDir}/${project.name}/external")
          .matching { it.include("**/openapi-spec.yaml") }
          .forEach { paths[it.parentFile.name] = it.path }
      paths
    } else {
      openapiGeneratorExtension.externalInputSpecPaths.get()
          .mapValues { "${openapiResourcesDir}/${it.value}" }
    }

    outputDir = openapiGeneratorExtension.outputDir.getOrElse(
        "${project.buildDir}/generated/openapi"
    )

    basePackage = openapiGeneratorExtension.basePackage.getOrElse(
        "io.github.javenue.${project.rootProject.name.replace("-", "")}.${project.parent?.parent?.name}.${project.name}"
    )
  }

  private fun addSrcDir() {
    val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
    javaPluginExtension.sourceSets.getByName("main").java.srcDir(
        project
            .files("${outputDir}/src/main/java")
            .builtBy(
                "generateSpringServerCode",
                "generateJavaClientCode",
            )
            .builtBy(externalInputSpecPaths.map { (apiName, _) ->
              "generateJavaClientCodeFor_${apiName}"
            })
    )
  }

}
