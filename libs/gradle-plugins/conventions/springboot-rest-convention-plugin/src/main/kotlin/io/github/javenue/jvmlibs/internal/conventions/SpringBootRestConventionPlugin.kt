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

    private val TYPE_MAPPINGS = mapOf("OffsetDateTime" to "java.time.LocalDateTime")

    private val GLOBAL_PROPERTIES = mapOf(
        "apis" to "",
        "apiDocs" to "false",
        "apiTests" to "false",
        "models" to "",
        "modelDocs" to "false",
        "modelTests" to "false",
        "supportingFiles" to "",
    )

    private val SERVER_CONFIG_OPTIONS = mapOf(
        "useTags" to "true",
        "useSpringBoot3" to "true",
        "interfaceOnly" to "true",
        "requestMappingMode" to "api_interface",
        "dateLibrary" to "java8",
        "annotationLibrary" to "none",
        "documentationProvider" to "none",
    )

    private val CLIENT_CONFIG_OPTIONS = mapOf(
        "useTags" to "true",
        "useJakartaEe" to "true",
        "library" to "resttemplate",
        "dateLibrary" to "java8",
        "documentationProvider" to "none",
    )

  }

  private lateinit var project: Project

  private lateinit var docProject: Project

  private lateinit var openapiDir: String

  private lateinit var openapiGeneratorExtension: SpringBootRestConventionExtension

  private lateinit var internalInputSpecPath: String

  private lateinit var externalInputSpecPaths: Map<String, String>

  private lateinit var outputDir: String

  private lateinit var basePackage: String

  override fun apply(project: Project) {
    this.project = project
    this.docProject = project.parent!!.project("doc")
    this.openapiDir = "${docProject.projectDir.path}/src/main/openapi"
    project.plugins.apply("java-library")
    this.openapiGeneratorExtension =
        project.extensions.create("openapiGenerator", SpringBootRestConventionExtension::class.java)

    project.repositories.mavenCentral()
    project.dependencies.let {
      it.add("implementation", "org.springframework.boot:spring-boot-starter-web")

      // required dependencies by generated Java sources.
      it.add("implementation", "org.springframework:spring-web:5.3.24")
      it.add("implementation", "org.springframework:spring-context:5.3.24")
      it.add("implementation", "com.fasterxml.jackson.core:jackson-core:2.14.2")
      it.add("implementation", "com.fasterxml.jackson.core:jackson-annotations:2.14.2")
      it.add("implementation", "com.fasterxml.jackson.core:jackson-databind:2.15.1")
      it.add("implementation", "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.14.2")
      it.add("implementation", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
      it.add("implementation", "jakarta.validation:jakarta.validation-api:3.0.2")
      it.add("compileOnly", "org.openapitools:jackson-databind-nullable:0.2.6")
      it.add("compileOnly", "jakarta.annotation:jakarta.annotation-api:2.1.1")
      it.add("compileOnly", "com.google.code.findbugs:jsr305:3.0.2")
    }

    project.afterEvaluate { _ ->
      processExtension()
      addSrcDir()

      val validateTasks = mutableSetOf<TaskProvider<ValidateTask>>()
      val generateTasks = mutableSetOf<TaskProvider<GenerateTask>>()

      validateTasks.add(
          project.tasks.register("validateOpenapiSpec", ValidateTask::class.java) {
            it.description = "Validates internal openapi specification file by running OpenAPI Generator."
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
            it.invokerPackage.set("${basePackage}.openapi.apis")
            it.apiPackage.set("${basePackage}.openapi.apis")
            it.modelPackage.set("${basePackage}.openapi.apis.models")
            it.typeMappings.set(TYPE_MAPPINGS)
            it.configOptions.set(SERVER_CONFIG_OPTIONS)
            it.globalProperties.set(GLOBAL_PROPERTIES)
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
            it.invokerPackage.set("${basePackage}.openapi.clients.internal")
            it.apiPackage.set("${basePackage}.openapi.clients.internal")
            it.modelPackage.set("${basePackage}.openapi.clients.internal.models")
            it.configOptions.set(CLIENT_CONFIG_OPTIONS)
            it.typeMappings.set(TYPE_MAPPINGS)
            it.globalProperties.set(GLOBAL_PROPERTIES)
          }
      )

      externalInputSpecPaths.forEach { (apiName, inputSpecPath) ->
        validateTasks.add(
            project.tasks.register("validateOpenapiSpecFor_${apiName}", ValidateTask::class.java) {
              it.description = "Validates openapi specification file for $apiName API by running OpenAPI Generator."
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
              it.invokerPackage.set("${basePackage}.openapi.clients.external.${apiName.lowercase()}")
              it.apiPackage.set("${basePackage}.openapi.clients.external.${apiName.lowercase()}")
              it.modelPackage.set("${basePackage}.openapi.clients.external.${apiName.lowercase()}.models")
              it.configOptions.set(CLIENT_CONFIG_OPTIONS)
              it.typeMappings.set(TYPE_MAPPINGS)
              it.globalProperties.set(GLOBAL_PROPERTIES)
            }
        )
      }

      project.tasks.register("generateAllCode") {
        it.description = "Runs all generate tasks which generate Java code."
        it.group = TASK_GROUP
        it.dependsOn(generateTasks)
      }

      project.tasks.getByName("compileJava").dependsOn("generateAllCode")
    }

  }

  /**
   * SpringBootRestConventionExtensionから各設定値を取得する。
   * 設定値が与えられていない場合はデフォルト値を設定する。
   */
  private fun processExtension() {
    internalInputSpecPath = "${openapiDir}/${
      openapiGeneratorExtension.internalInputSpecPath.getOrElse("internal/openapi.yaml")
    }"

    externalInputSpecPaths = if (openapiGeneratorExtension.externalInputSpecPaths.get().isEmpty()) {
      val paths = HashMap<String, String>()
      docProject
          .fileTree("${openapiDir}/external")
          .matching { it.include("**/openapi.yaml") }
          .forEach { paths[it.parentFile.name] = it.path }
      paths
    } else {
      openapiGeneratorExtension.externalInputSpecPaths.get()
          .mapValues { "${openapiDir}/${it.value}" }
    }

    outputDir = openapiGeneratorExtension.outputDir.getOrElse(
        "${project.layout.buildDirectory.map { it.asFile.path }.get()}/generated/openapi"
    )

    val appName = project.rootProject.name.replace("-", "").lowercase()
    val contextName = project.parent?.parent?.parent?.name?.replace("-", "")?.lowercase()
    val perspectiveName = project.parent?.name?.replace("-", "")?.lowercase()
    basePackage = openapiGeneratorExtension.basePackage.getOrElse(
        "io.github.javenue.$appName.$contextName.$perspectiveName"
    )
  }

  private fun addSrcDir() {
    val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
    javaPluginExtension.sourceSets.getByName("main").java.srcDir(
        project
            .files("${outputDir}/src/main/java")
            .builtBy("generateSpringServerCode", "generateJavaClientCode")
            .builtBy(externalInputSpecPaths.map { (apiName, _) ->
              "generateJavaClientCodeFor_${apiName}"
            })
    )
  }

}
