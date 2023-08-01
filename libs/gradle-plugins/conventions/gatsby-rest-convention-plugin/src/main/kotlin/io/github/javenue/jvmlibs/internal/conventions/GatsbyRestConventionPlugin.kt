package io.github.javenue.jvmlibs.internal.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.openapitools.generator.gradle.plugin.tasks.ValidateTask

@Suppress("unused")
class GatsbyRestConventionPlugin : Plugin<Project> {

  companion object {
    private const val TASK_GROUP = "openapi generation"

    private const val TASK_DESCRIPTION = "Generates Axios client code for %s API by running Open API generator."

    private const val INTERNAL_SPEC_PATHS_VALIDATION_ERROR_MESSAGE = "Parameter with key '%s' could not be found in any elements of 'internalSpecPaths' in 'openapiGenerator' config."
  }

  private lateinit var project: Project

  private lateinit var externalOpenapiProject: Project

  private lateinit var openapiGeneratorExtension: GatsbyRestConventionExtension

  private lateinit var internalInputSpecPaths: List<Map<String, String>>

  private lateinit var externalInputSpecPaths: Map<String, String>

  private lateinit var outputRootDir: String

  override fun apply(project: Project) {
    this.project = project
    this.externalOpenapiProject =
        project.rootProject.project(":modules:all:openapi")
    this.openapiGeneratorExtension =
        project.extensions.create("openapiGenerator", GatsbyRestConventionExtension::class.java)

    project.afterEvaluate { _ ->
      processExtension()

      val validateTasks = mutableSetOf<TaskProvider<ValidateTask>>()
      val generateTasks = mutableSetOf<TaskProvider<GenerateTask>>()
      internalInputSpecPaths.forEach { spec ->
        val apiName = spec["apiName"]!!
        val inputSpecPath = spec["path"]!!

        validateTasks.add(
            project.tasks.register("validateOpenapiSpecFor_${apiName}", ValidateTask::class.java) {
              it.description = TASK_DESCRIPTION.format(apiName)
              it.group = TASK_GROUP
              it.inputSpec.set(inputSpecPath)
              it.recommend.set(true)
            }
        )

        generateTasks.add(
            project.tasks.register("generateAxiosClientCodeFor_${apiName}", GenerateTask::class.java) {
              it.dependsOn(project.tasks.getByName("validateOpenapiSpecFor_${apiName}"))
              it.description = TASK_DESCRIPTION.format(apiName)
              it.group = TASK_GROUP
              it.inputSpec.set(inputSpecPath)
              it.outputDir.set(getOutputDir(apiName))
              it.generatorName.set("typescript-axios")
              it.typeMappings.set(mapOf("Decimal" to "number"))
            }
        )
      }

      externalInputSpecPaths.forEach { (apiName, inputSpecPath) ->
        validateTasks.add(
            project.tasks.register("validateOpenapiSpecFor_${apiName}", ValidateTask::class.java) {
              it.description = TASK_DESCRIPTION.format(apiName)
              it.group = TASK_GROUP
              it.inputSpec.set(inputSpecPath)
              it.recommend.set(true)
            }
        )

        generateTasks.add(
            project.tasks.register("generateAxiosClientCodeFor_${apiName}", GenerateTask::class.java) {
              it.dependsOn(project.tasks.getByName("validateOpenapiSpecFor_${apiName}"))
              it.description = TASK_DESCRIPTION.format(apiName)
              it.group = TASK_GROUP
              it.inputSpec.set(inputSpecPath)
              it.outputDir.set(getOutputDir(apiName, true))
              it.generatorName.set("typescript-axios")
              it.typeMappings.set(mapOf("Decimal" to "number"))
            }
        )
      }

      project.tasks.register("generateAllCode") {
        it.description = "Runs all generate tasks which generate Axios client code."
        it.group = TASK_GROUP
        it.dependsOn(generateTasks)
      }
    }
  }

  private fun processExtension() {
    val modulesProject = project.parent?.parent?.parent!!
    internalInputSpecPaths =
        openapiGeneratorExtension.internalInputSpecPaths.get().ifEmpty {
          modulesProject.childProjects.filter {
            it.key != "all"
          }.map {
            mapOf(
                "serviceName" to it.key,
                "apiName" to it.key,
                "path" to "${project.name}/internal/openapi-spec.yaml",
            )
          }
        }.map {
          val serviceName = it["serviceName"]
              ?: throw IllegalArgumentException(INTERNAL_SPEC_PATHS_VALIDATION_ERROR_MESSAGE.format("serviceName"))
          val apiName = it["apiName"]
              ?: serviceName
          val path = it["path"]
              ?: throw IllegalArgumentException(INTERNAL_SPEC_PATHS_VALIDATION_ERROR_MESSAGE.format("path"))
          val openapiProject = modulesProject.project("${serviceName}:openapi")
          mapOf(
              "serviceName" to serviceName,
              "apiName" to apiName,
              "path" to "${getResourcesDir(openapiProject)}/${path}"
          )
        }

    externalInputSpecPaths = if (openapiGeneratorExtension.externalInputSpecPaths.get().isEmpty()) {
      val paths = HashMap<String, String>()
      externalOpenapiProject
          .fileTree("${getResourcesDir(externalOpenapiProject)}/${project.name}/external")
          .matching { it.include("**/openapi-spec.yaml") }
          .forEach { paths[it.parentFile.name] = it.path }
      paths
    } else {
      openapiGeneratorExtension.externalInputSpecPaths.get()
          .mapValues { "${getResourcesDir(externalOpenapiProject)}/${it.value}" }
    }

    outputRootDir = openapiGeneratorExtension.outputDir.getOrElse(
        "${project.buildDir}/generated/openapi"
    )
  }

  private fun getOutputDir(apiName: String, isExternal: Boolean = false): String {
    return "${outputRootDir}/${
      if (isExternal) {
        "external"
      } else {
        "internal"
      }
    }/${apiName}"
  }

  private fun getResourcesDir(project: Project): String {
    return "${project.projectDir.path}/src/main/resources"
  }

}
