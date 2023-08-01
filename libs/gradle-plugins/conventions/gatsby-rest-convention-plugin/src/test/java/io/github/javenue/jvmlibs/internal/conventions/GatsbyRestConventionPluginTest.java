package io.github.javenue.jvmlibs.internal.conventions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GatsbyRestConventionPluginTest {

  @Nested
  class TestsWithGradleRunner {

    @SuppressWarnings("ConstantConditions")
    @Test
    void givenRequiredFilesWhenRunGenerateAllTaskThenTasksSucceedAndSourceFilesGenerated(
        @TempDir File rootDir
    ) throws IOException {
      // Given
      createRequiredFiles(rootDir);

      // When
      var result = GradleRunner.create()
          .withProjectDir(new File(rootDir, "modules/all/frontend/product"))
          .withPluginClasspath()
          .withDebug(true)
          .forwardOutput()
          .withArguments("generateAllCode")
          .build();

      // Then
      assertThat(result
          .task(":modules:all:frontend:product:validateOpenapiSpecFor_service1")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result
          .task(":modules:all:frontend:product:generateAxiosClientCodeFor_service1")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result
          .task(":modules:all:frontend:product:validateOpenapiSpecFor_service2")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result
          .task(":modules:all:frontend:product:generateAxiosClientCodeFor_service2")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result
          .task(":modules:all:frontend:product:validateOpenapiSpecFor_api1")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result
          .task(":modules:all:frontend:product:generateAxiosClientCodeFor_api1")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result
          .task(":modules:all:frontend:product:validateOpenapiSpecFor_api2")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result
          .task(":modules:all:frontend:product:generateAxiosClientCodeFor_api2")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result
          .task(":modules:all:frontend:product:generateAllCode")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(contentOf(new File(
          rootDir,
          "modules/all/frontend/product/build/generated/openapi/internal/service1/api.ts"
      ))).contains("export class Internal1Api extends BaseAPI");
      assertThat(contentOf(new File(
          rootDir,
          "modules/all/frontend/product/build/generated/openapi/internal/service2/api.ts"
      ))).contains("export class Internal2Api extends BaseAPI");
      assertThat(contentOf(new File(
          rootDir,
          "modules/all/frontend/product/build/generated/openapi/external/api1/api.ts"
      ))).contains("export class External1Api extends BaseAPI");
      assertThat(contentOf(new File(
          rootDir,
          "modules/all/frontend/product/build/generated/openapi/external/api2/api.ts"
      ))).contains("export class External2Api extends BaseAPI");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void givenRequiredFilesWithValidConfigWhenRunGenerateAllTaskThenSucceedAndSourceFilesGenerated(
        @TempDir File rootDir
    ) throws IOException {
      // Given
      createRequiredFilesWithConfig(rootDir);

      // When
      var result = GradleRunner.create()
          .withProjectDir(new File(rootDir, "modules/all/frontend/product"))
          .withPluginClasspath()
          .withDebug(true)
          .forwardOutput()
          .withArguments("generateAllCode")
          .build();

      // Then
      assertThat(result
          .task(":modules:all:frontend:product:validateOpenapiSpecFor_SERVICE1")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result
          .task(":modules:all:frontend:product:generateAxiosClientCodeFor_SERVICE1")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result
          .task(":modules:all:frontend:product:validateOpenapiSpecFor_SERVICE2")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result
          .task(":modules:all:frontend:product:generateAxiosClientCodeFor_SERVICE2")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result
          .task(":modules:all:frontend:product:validateOpenapiSpecFor_API1")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result
          .task(":modules:all:frontend:product:generateAxiosClientCodeFor_API1")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result
          .task(":modules:all:frontend:product:validateOpenapiSpecFor_API2")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result
          .task(":modules:all:frontend:product:generateAxiosClientCodeFor_API2")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result
          .task(":modules:all:frontend:product:generateAllCode")
          .getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(contentOf(new File(
          rootDir,
          "modules/all/frontend/product/build/generated/OPENAPI/internal/SERVICE1/api.ts"
      ))).contains("export class Internal1Api extends BaseAPI");
      assertThat(contentOf(new File(
          rootDir,
          "modules/all/frontend/product/build/generated/OPENAPI/internal/SERVICE2/api.ts"
      ))).contains("export class Internal2Api extends BaseAPI");
      assertThat(contentOf(new File(
          rootDir,
          "modules/all/frontend/product/build/generated/OPENAPI/external/API1/api.ts"
      ))).contains("export class External1Api extends BaseAPI");
      assertThat(contentOf(new File(
          rootDir,
          "modules/all/frontend/product/build/generated/OPENAPI/external/API2/api.ts"
      ))).contains("export class External2Api extends BaseAPI");
    }

    @Test
    void givenRequiredFilesWithoutServiceNameWhenRunGenerateAllTaskThenFail(
        @TempDir File rootDir
    ) throws IOException {
      // Given
      createRequiredFilesWithConfigWithoutApiName(rootDir);

      // When
      var result = GradleRunner.create()
          .withProjectDir(new File(rootDir, "modules/all/frontend/product"))
          .withPluginClasspath()
          .withDebug(true)
          .forwardOutput()
          .withArguments("generateAllCode")
          .buildAndFail();

      assertThat(result.getOutput()).contains(
          "Parameter with key 'serviceName' could not be found in any elements of 'internalSpecPaths' in 'openapiGenerator' config."
      );
    }

    @Test
    void givenRequiredFilesWithoutPathWhenRunGenerateAllTaskThenFail(
        @TempDir File rootDir
    ) throws IOException {
      // Given
      createRequiredFilesWithConfigWithoutPath(rootDir);

      // When
      var result = GradleRunner.create()
          .withProjectDir(new File(rootDir, "modules/all/frontend/product"))
          .withPluginClasspath()
          .withDebug(true)
          .forwardOutput()
          .withArguments("generateAllCode")
          .buildAndFail();

      assertThat(result.getOutput()).contains(
          "Parameter with key 'path' could not be found in any elements of 'internalSpecPaths' in 'openapiGenerator' config."
      );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createRequiredFiles(File rootDir) throws IOException {
      var settingFile = new File(rootDir, "settings.gradle");
      try (var writer = new FileWriter(settingFile)) {
        writer.write("""
            rootProject.name = 'root'
            include ':modules:all:frontend:product'
            include ':modules:all:openapi'
            include ':modules:service1:openapi'
            include ':modules:service2:openapi'
            """);
      }

      var productBuildFile = new File(rootDir, "modules/all/frontend/product/build.gradle");
      productBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(productBuildFile)) {
        writer.write("""
            plugins {
              id 'io.github.javenue.internal.gatsby-rest-convention'
            }
            """);
      }

      var openapiBuildFile = new File(rootDir, "modules/all/openapi/build.gradle");
      openapiBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(openapiBuildFile)) {
        writer.write("""
            plugins {
              id 'java'
            }
            """);
      }

      var service1OpenapiBuildFile = new File(rootDir, "modules/service1/openapi/build.gradle");
      service1OpenapiBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(service1OpenapiBuildFile)) {
        writer.write("""
            plugins {
              id 'java'
            }
            """);
      }

      var service2OpenapiBuildFile = new File(rootDir, "modules/service2/openapi/build.gradle");
      service2OpenapiBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(service2OpenapiBuildFile)) {
        writer.write("""
            plugins {
              id 'java'
            }
            """);
      }

      var internalSpecFile1 = new File(
          rootDir,
          "modules/service1/openapi/src/main/resources/product/internal/openapi-spec.yaml"
      );
      internalSpecFile1.getParentFile().mkdirs();
      try (var writer = new FileWriter(internalSpecFile1)) {
        writer.write("""
            openapi: 3.0.0
            info:
              title: Internal Service1
              version: '1.0'
            servers:
              - url: 'http://localhost:8080'
            paths:
              /internal1:
                post:
                  summary: 'internal1'
                  operationId: internal1
                  responses:
                    '200':
                      description: OK
                  tags:
                    - internal1
            """);
      }

      var internalSpecFile2 = new File(
          rootDir,
          "modules/service2/openapi/src/main/resources/product/internal/openapi-spec.yaml"
      );
      internalSpecFile2.getParentFile().mkdirs();
      try (var writer = new FileWriter(internalSpecFile2)) {
        writer.write("""
            openapi: 3.0.0
            info:
              title: Internal Service2
              version: '1.0'
            servers:
              - url: 'http://localhost:8080'
            paths:
              /internal2:
                post:
                  summary: 'internal2'
                  operationId: internal2
                  responses:
                    '200':
                      description: OK
                  tags:
                    - internal2
            """);
      }

      var externalSpecFile1 = new File(
          rootDir,
          "modules/all/openapi/src/main/resources/product/external/api1/openapi-spec.yaml"
      );
      externalSpecFile1.getParentFile().mkdirs();
      try (var writer = new FileWriter(externalSpecFile1)) {
        writer.write("""
            openapi: 3.1.0
            info:
              title: External API1
              version: '1.0'
            servers:
              - url: 'http://localhost:8080'
            paths:
              /external1:
                post:
                  summary: external1
                  operationId: external1
                  responses:
                    '200':
                      description: OK
                  tags:
                    - external1
            """);
      }

      var externalSpecFile2 = new File(
          rootDir,
          "modules/all/openapi/src/main/resources/product/external/api2/openapi-spec.yaml"
      );
      externalSpecFile2.getParentFile().mkdirs();
      try (var writer = new FileWriter(externalSpecFile2)) {
        writer.write("""
            openapi: 3.0.0
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
            """);
      }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createRequiredFilesWithConfig(File rootDir) throws IOException {
      var settingFile = new File(rootDir, "settings.gradle");
      try (var writer = new FileWriter(settingFile)) {
        writer.write("""
            rootProject.name = 'root'
            include ':modules:all:frontend:product'
            include ':modules:all:openapi'
            include ':modules:service1:openapi'
            include ':modules:SERVICE2:openapi'
            """);
      }

      var productBuildFile = new File(rootDir, "modules/all/frontend/product/build.gradle");
      productBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(productBuildFile)) {
        writer.write("""
            plugins {
              id 'io.github.javenue.internal.gatsby-rest-convention'
            }
            openapiGenerator {
              internalInputSpecPaths = [
                [serviceName: "service1", apiName: "SERVICE1", path: "/product/internal/OPENAPI-SPEC.yaml"],
                [serviceName: "SERVICE2", path: "/product/internal/OPENAPI-SPEC.yaml"],
              ]
              externalInputSpecPaths = [
                API1: "product/external/api1/OPENAPI-SPEC.yaml",
                API2: "product/external/api2/OPENAPI-SPEC.yaml",
              ]
              outputDir = "${buildDir}/generated/OPENAPI"
            }
            """);
      }

      var openapiBuildFile = new File(rootDir, "modules/all/openapi/build.gradle");
      openapiBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(openapiBuildFile)) {
        writer.write("""
            plugins {
              id 'java'
            }
            """);
      }

      var service1OpenapiBuildFile = new File(rootDir, "modules/service1/openapi/build.gradle");
      service1OpenapiBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(service1OpenapiBuildFile)) {
        writer.write("""
            plugins {
              id 'java'
            }
            """);
      }

      var service2OpenapiBuildFile = new File(rootDir, "modules/SERVICE2/openapi/build.gradle");
      service2OpenapiBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(service2OpenapiBuildFile)) {
        writer.write("""
            plugins {
              id 'java'
            }
            """);
      }

      var internalSpecFile1 = new File(
          rootDir,
          "modules/service1/openapi/src/main/resources/product/internal/OPENAPI-SPEC.yaml"
      );
      internalSpecFile1.getParentFile().mkdirs();
      try (var writer = new FileWriter(internalSpecFile1)) {
        writer.write("""
            openapi: 3.0.0
            info:
              title: Internal Service1
              version: '1.0'
            servers:
              - url: 'http://localhost:8080'
            paths:
              /internal1:
                post:
                  summary: 'internal1'
                  operationId: internal1
                  responses:
                    '200':
                      description: OK
                  tags:
                    - internal1
            """);
      }

      var internalSpecFile2 = new File(
          rootDir,
          "modules/SERVICE2/openapi/src/main/resources/product/internal/OPENAPI-SPEC.yaml"
      );
      internalSpecFile2.getParentFile().mkdirs();
      try (var writer = new FileWriter(internalSpecFile2)) {
        writer.write("""
            openapi: 3.0.0
            info:
              title: Internal Service2
              version: '1.0'
            servers:
              - url: 'http://localhost:8080'
            paths:
              /internal2:
                post:
                  summary: 'internal2'
                  operationId: internal2
                  responses:
                    '200':
                      description: OK
                  tags:
                    - internal2
            """);
      }

      var externalSpecFile1 = new File(
          rootDir,
          "modules/all/openapi/src/main/resources/product/external/api1/OPENAPI-SPEC.yaml"
      );
      externalSpecFile1.getParentFile().mkdirs();
      try (var writer = new FileWriter(externalSpecFile1)) {
        writer.write("""
            openapi: 3.0.0
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
            """);
      }

      var externalSpecFile2 = new File(
          rootDir,
          "modules/all/openapi/src/main/resources/product/external/api2/OPENAPI-SPEC.yaml"
      );
      externalSpecFile2.getParentFile().mkdirs();
      try (var writer = new FileWriter(externalSpecFile2)) {
        writer.write("""
            openapi: 3.0.0
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
            """);
      }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createRequiredFilesWithConfigWithoutApiName(File rootDir) throws IOException {
      var settingFile = new File(rootDir, "settings.gradle");
      try (var writer = new FileWriter(settingFile)) {
        writer.write("""
            rootProject.name = 'root'
            include ':modules:all:frontend:product'
            include ':modules:all:openapi'
            """);
      }

      var productBuildFile = new File(rootDir, "modules/all/frontend/product/build.gradle");
      productBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(productBuildFile)) {
        writer.write("""
            plugins {
              id 'io.github.javenue.internal.gatsby-rest-convention'
            }
            openapiGenerator {
              internalInputSpecPaths = [
                [apiName: "service1", path: "/product/internal/OPENAPI-SPEC.yaml"],
              ]
            }
            """);
      }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createRequiredFilesWithConfigWithoutPath(File rootDir) throws IOException {
      var settingFile = new File(rootDir, "settings.gradle");
      try (var writer = new FileWriter(settingFile)) {
        writer.write("""
            rootProject.name = 'root'
            include ':modules:all:frontend:product'
            include ':modules:all:openapi'
            """);
      }

      var productBuildFile = new File(rootDir, "modules/all/frontend/product/build.gradle");
      productBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(productBuildFile)) {
        writer.write("""
            plugins {
              id 'io.github.javenue.internal.gatsby-rest-convention'
            }
            openapiGenerator {
              internalInputSpecPaths = [
                [serviceName: "SERVICE1", apiName: "service1"],
              ]
            }
            """);
      }
    }

  }

}
