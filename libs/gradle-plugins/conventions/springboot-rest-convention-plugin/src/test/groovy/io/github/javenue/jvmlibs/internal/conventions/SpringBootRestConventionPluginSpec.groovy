package io.github.javenue.jvmlibs.internal.conventions

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

class SpringBootRestConventionPluginSpec extends Specification {

  @TempDir
  File rootDir

  def perspectiveProject = ":artifacts:contexts:my-context:perspectives:my-perspective"

  def perspectiveDir = "artifacts/contexts/my-context/perspectives/my-perspective"

  def "compileJavaタスクを実行する"() {
    given: "設定ファイル, ビルドスクリプト, OpenAPIファイル"
    createFiles(withConfig)

    when: "compileJavaタスクを実行したとき"
    def result = GradleRunner.create()
        .withProjectDir(new File(rootDir, "$perspectiveDir/app"))
        .withPluginClasspath()
        .withDebug(true)
        .forwardOutput()
        .withArguments("compileJava")
        .build()

    then: "全てのvalidateタスクとgenerateタスクが実行され、成功する"
    result.task("$perspectiveProject:app:validateOpenapiSpec").getOutcome() == TaskOutcome.SUCCESS
    result.task("$perspectiveProject:app:generateSpringServerCode").getOutcome() == TaskOutcome.SUCCESS
    result.task("$perspectiveProject:app:generateJavaClientCode").getOutcome() == TaskOutcome.SUCCESS
    result.task("$perspectiveProject:app:validateOpenapiSpecFor_$api1Name").getOutcome() == TaskOutcome.SUCCESS
    result.task("$perspectiveProject:app:generateJavaClientCodeFor_$api1Name").getOutcome() == TaskOutcome.SUCCESS
    result.task("$perspectiveProject:app:validateOpenapiSpecFor_$api2Name").getOutcome() == TaskOutcome.SUCCESS
    result.task("$perspectiveProject:app:generateJavaClientCodeFor_$api2Name").getOutcome() == TaskOutcome.SUCCESS

    and: "全てのJavaソースファイルが生成されている"
    new File(
        rootDir,
        "$perspectiveDir/app/build/generated/$outputDirName/src/main/java/$packageDir/openapi/apis/InternalApi.java"
    ).exists()
    new File(
        rootDir,
        "$perspectiveDir/app/build/generated/$outputDirName/src/main/java/$packageDir/openapi/apis/models/Pet.java"
    ).exists()
    new File(
        rootDir,
        "$perspectiveDir/app/build/generated/$outputDirName/src/main/java/$packageDir/openapi/clients/internal/InternalApi.java"
    ).exists()
    new File(
        rootDir,
        "$perspectiveDir/app/build/generated/$outputDirName/src/main/java/$packageDir/openapi/clients/internal/models/Pet.java"
    ).exists()
    new File(
        rootDir,
        "$perspectiveDir/app/build/generated/$outputDirName/src/main/java/$packageDir/openapi/clients/external/${api1Name.toLowerCase()}/External1Api.java"
    ).exists()
    new File(
        rootDir,
        "$perspectiveDir/app/build/generated/$outputDirName/src/main/java/$packageDir/openapi/clients/external/${api1Name.toLowerCase()}/models/Pet1.java"
    ).exists()
    new File(
        rootDir,
        "$perspectiveDir/app/build/generated/$outputDirName/src/main/java/$packageDir/openapi/clients/external/${api2Name.toLowerCase()}/External2Api.java"
    ).exists()
    new File(
        rootDir,
        "$perspectiveDir/app/build/generated/$outputDirName/src/main/java/$packageDir/openapi/clients/external/${api2Name.toLowerCase()}/models/Pet2.java"
    ).exists()

    where:
    withConfig | api1Name | api2Name | outputDirName | packageDir
    false      | "api1"   | "api2"   | "openapi"     | "io/github/javenue/myapp/mycontext/myperspective"
    true       | "API1"   | "API2"   | "OPENAPI"     | "com/example/test"
  }

  private def createFiles(boolean withConfig) {
    def openapiVersion = "3.0.3"

    new File(rootDir, "settings.gradle") << """
      rootProject.name = 'my-app'
      include '$perspectiveProject:app'
      include '$perspectiveProject:doc'
    """
    def appBuildFile = new File(rootDir, "$perspectiveDir/app/build.gradle")
    appBuildFile.parentFile.mkdirs()
    appBuildFile << """
      plugins {
        id 'java'
        id 'org.springframework.boot' version '3.2.0'
        id 'io.spring.dependency-management' version '1.1.4'
        id 'io.github.javenue.internal.springboot-rest-convention'
      }
    """
    if (withConfig) {
      appBuildFile << """
        openapiGenerator {
          internalInputSpecPath = "internal/OPENAPI.yaml"
          externalInputSpecPaths = [
            API1: "external/api1/OPENAPI.yaml",
            API2: "external/api2/OPENAPI.yaml",
          ]
          outputDir = "\${buildDir}/generated/OPENAPI"
          basePackage = "com.example.test"
        }
      """
    }
    def specFileName = withConfig ? "OPENAPI.yaml" : "openapi.yaml"
    def internalSpecFile = new File(rootDir, "$perspectiveDir/doc/src/main/openapi/internal/$specFileName")
    internalSpecFile.parentFile.mkdirs()
    internalSpecFile << """
      openapi: ${openapiVersion}
      info:
        title: Internal API
        version: '1.0'
      servers:
        - url: 'http://localhost:8080'
      paths:
        /internal:
          post:
            summary: 'internal'
            operationId: internal
            responses:
              '200':
                description: OK
            tags:
              - internal
      components:
        schemas:
          Pet:
            type: object
            properties:
              id:
                type: integer
                format: int64
              name:
                type: string
              tag:
                type: string
    """
    def externalSpecFile1 = new File(rootDir, "$perspectiveDir/doc/src/main/openapi/external/api1/$specFileName")
    externalSpecFile1.parentFile.mkdirs()
    externalSpecFile1 << """
      openapi: ${openapiVersion}
      info:
        title: External API1
        version: '1.0'
      servers:
        - url: 'http://localhost:8080'
      paths:
        /external1:
          post:
            summary: 'external1'
            operationId: external1
            responses:
              '200':
                description: OK
            tags:
              - external1    
      components:
        schemas:
          Pet1:
            type: object
            properties:
              id:
                type: integer
                format: int64
              name:
                type: string
              tag:
                type: string
    """
    def externalSpecFile2 = new File(rootDir, "$perspectiveDir/doc/src/main/openapi/external/api2/$specFileName")
    externalSpecFile2.parentFile.mkdirs()
    externalSpecFile2 << """
      openapi: ${openapiVersion}
      info:
        title: External API2
        version: '1.0'
      servers:
        - url: 'http://localhost:8080'
      paths:
        /external2:
          post:
            summary: 'external2'
            operationId: external2
            responses:
              '200':
                description: OK
            tags:
              - external2
      components:
        schemas:
          Pet2:
            type: object
            properties:
              id:
                type: integer
                format: int64
              name:
                type: string
              tag:
                type: string
    """
  }

}
