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

class SpringBootRestConventionPluginTest {

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
          .withProjectDir(new File(rootDir, "backend/product"))
          .withPluginClasspath()
          .withDebug(true)
          .forwardOutput()
          .withArguments("generateAllCode")
          .build();

      // Then
      assertThat(result.task(":backend:product:validateOpenapiSpec").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result.task(":backend:product:generateSpringServerCode").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result.task(":backend:product:generateJavaClientCode").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result.task(":backend:product:validateOpenapiSpecFor_api1").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result.task(":backend:product:generateJavaClientCodeFor_api1").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result.task(":backend:product:validateOpenapiSpecFor_api2").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result.task(":backend:product:generateJavaClientCodeFor_api2").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result.task(":backend:product:generateAllCode").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(contentOf(new File(
          rootDir,
          "backend/product/build/generated/openapi/src/main/java/io/github/javenue/root/root/product/openapi/internal/spring/apis/InternalApi.java"
      ))).contains("package io.github.javenue.root.root.product.openapi.internal.spring.apis;");
      assertThat(contentOf(new File(
          rootDir,
          "backend/product/build/generated/openapi/src/main/java/io/github/javenue/root/root/product/openapi/internal/java/clients/InternalApi.java"
      ))).contains("package io.github.javenue.root.root.product.openapi.internal.java.clients;");
      assertThat(contentOf(new File(
          rootDir,
          "backend/product/build/generated/openapi/src/main/java/io/github/javenue/root/root/product/openapi/external/java/api1/clients/External1Api.java"
      ))).contains(
          "package io.github.javenue.root.root.product.openapi.external.java.api1.clients;"
      );
      assertThat(contentOf(new File(
          rootDir,
          "backend/product/build/generated/openapi/src/main/java/io/github/javenue/root/root/product/openapi/external/java/api2/clients/External2Api.java"
      ))).contains(
          "package io.github.javenue.root.root.product.openapi.external.java.api2.clients;"
      );
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void givenRequiredFilesWithValidConfigWhenRunGenerateAllTaskThenSucceedAndSourceFilesGenerated(
        @TempDir File rootDir
    ) throws IOException {
      // Given
      createRequiredFilesWithConfigurations(rootDir);

      // When
      var result = GradleRunner.create()
          .withProjectDir(new File(rootDir, "backend/product"))
          .withPluginClasspath()
          .withDebug(true)
          .forwardOutput()
          .withArguments("generateAllCode")
          .build();

      // Then
      assertThat(result.task(":backend:product:validateOpenapiSpec").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result.task(":backend:product:generateSpringServerCode").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result.task(":backend:product:generateJavaClientCode").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result.task(":backend:product:validateOpenapiSpecFor_API1").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result.task(":backend:product:generateJavaClientCodeFor_API1").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result.task(":backend:product:validateOpenapiSpecFor_API2").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);
      assertThat(result.task(":backend:product:generateJavaClientCodeFor_API2").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(result.task(":backend:product:generateAllCode").getOutcome())
          .isEqualTo(TaskOutcome.SUCCESS);

      assertThat(contentOf(new File(
          rootDir,
          "backend/product/build/generated/OPENAPI/src/main/java/com/example/test/openapi/internal/spring/apis/InternalApi.java"
      ))).contains("package com.example.test.openapi.internal.spring.apis;");
      assertThat(contentOf(new File(
          rootDir,
          "backend/product/build/generated/OPENAPI/src/main/java/com/example/test/openapi/internal/java/clients/InternalApi.java"
      ))).contains("package com.example.test.openapi.internal.java.clients;");
      assertThat(contentOf(new File(
          rootDir,
          "backend/product/build/generated/OPENAPI/src/main/java/com/example/test/openapi/external/java/api1/clients/External1Api.java"
      ))).contains(
          "package com.example.test.openapi.external.java.api1.clients;"
      );
      assertThat(contentOf(new File(
          rootDir,
          "backend/product/build/generated/OPENAPI/src/main/java/com/example/test/openapi/external/java/api2/clients/External2Api.java"
      ))).contains(
          "package com.example.test.openapi.external.java.api2.clients;"
      );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createRequiredFiles(File rootDir) throws IOException {
      var settingFile = new File(rootDir, "settings.gradle");
      try (var writer = new FileWriter(settingFile)) {
        writer.write("""
            rootProject.name = 'root'
            include ':backend:product'
            include ':openapi'
            """);
      }

      var productBuildFile = new File(rootDir, "backend/product/build.gradle");
      productBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(productBuildFile)) {
        writer.write("""
                plugins {
                  id 'java'
                  id 'io.github.javenue.internal.springboot-rest-convention'
                }
            """);
      }

      var openapiBuildFile = new File(rootDir, "openapi/build.gradle");
      openapiBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(openapiBuildFile)) {
        writer.write("""
            plugins {
              id 'java'
            }
            """);
      }

      var internalSpecFile = new File(
          rootDir,
          "openapi/src/main/resources/product/internal/openapi-spec.yaml"
      );
      internalSpecFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(internalSpecFile)) {
        writer.write("""
            openapi: 3.0.0
            info:
              title: Internal Api
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
            """);
      }

      var externalSpecFile1 = new File(
          rootDir,
          "openapi/src/main/resources/product/external/api1/openapi-spec.yaml"
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
          "openapi/src/main/resources/product/external/api2/openapi-spec.yaml"
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
    private void createRequiredFilesWithConfigurations(File rootDir) throws IOException {
      var settingFile = new File(rootDir, "settings.gradle");
      try (var writer = new FileWriter(settingFile)) {
        writer.write("""
            rootProject.name = 'root'
            include ':backend:product'
            include ':openapi'
            """);
      }

      var productBuildFile = new File(rootDir, "backend/product/build.gradle");
      productBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(productBuildFile)) {
        writer.write("""
                plugins {
                  id 'java'
                  id 'io.github.javenue.internal.springboot-rest-convention'
                }
                openapiGenerator {
                  internalInputSpecPath = "product/internal/OPENAPI-SPEC.yaml"
                  externalInputSpecPaths = [
                    API1: "product/external/api1/OPENAPI-SPEC.yaml",
                    API2: "product/external/api2/OPENAPI-SPEC.yaml",
                  ]
                  outputDir = "${buildDir}/generated/OPENAPI"
                  basePackage = "com.example.test"
                }
            """);
      }

      var openapiBuildFile = new File(rootDir, "openapi/build.gradle");
      openapiBuildFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(openapiBuildFile)) {
        writer.write("""
            plugins {
              id 'java'
            }
            """);
      }

      var internalSpecFile = new File(
          rootDir,
          "openapi/src/main/resources/product/internal/OPENAPI-SPEC.yaml"
      );
      internalSpecFile.getParentFile().mkdirs();
      try (var writer = new FileWriter(internalSpecFile)) {
        writer.write("""
            openapi: 3.0.0
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
            """);
      }

      var externalSpecFile1 = new File(
          rootDir,
          "openapi/src/main/resources/product/external/api1/OPENAPI-SPEC.yaml"
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
          "openapi/src/main/resources/product/external/api2/OPENAPI-SPEC.yaml"
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

  }

}
