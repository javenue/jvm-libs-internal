package io.github.javenue.jvmlibs.internal.conventions

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

/**
 * Extension to configure SpringBootOpenApiGenerationPlugin
 */
interface SpringBootRestConventionExtension {

  /**
   * Specify the internal OpenAPI specification file path after 'root/modules/SERVICE-NAME/openapi/src/main/resources'.
   * If not specified, the default value 'product or management/internal/openapi-spec.yaml' will be used.
   */
  val internalInputSpecPath: Property<String>

  /**
   * Specify the external OpenAPI specification file paths for 'root/modules/SERVICE-NAME/openapi/src/main/resources'.
   * If not specified, the plugin will search 'product or management/external/API-NAME/openapi-spec.yaml'.
   */
  val externalInputSpecPaths: MapProperty<String, String>

  /**
   * Specify the destination directory of generated code.
   * If not specified, the default value 'TARGET-PROJECT-DIR/build/generated/openapi' will be used.
   */
  val outputDir: Property<String>

  /**
   * Specify the base package name of generated code.
   * If not specified, the default value 'io.github.javenue.root project name.service name.product or management' will be used.
   */
  val basePackage: Property<String>

}
