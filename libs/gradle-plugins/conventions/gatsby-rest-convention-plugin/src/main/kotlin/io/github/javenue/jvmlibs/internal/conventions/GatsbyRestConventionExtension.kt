package io.github.javenue.jvmlibs.internal.conventions

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

/**
 * Extension to configure GatsbyOpenApiGenerationPlugin
 */
interface GatsbyRestConventionExtension {

  /**
   * Specify the list of Map consists of serviceName, apiName(optional), OpenAPI specification file paths after 'root/modules/SERVICE-NAME/openapi/src/main/resources'.
   * If not specified, 'product or management/internal/openapi-spec.yaml' in all services will be used as path and service name as apiName.
   */
  val internalInputSpecPaths: ListProperty<Map<String, String>>

  /**
   * Specify the external OpenAPI specification file paths for '/service/openapi/src/main/resources'.
   * If not specified, the plugin will search 'product or management/external/API-NAME/openapi-spec.yaml'.
   */
  val externalInputSpecPaths: MapProperty<String, String>

  /**
   * Specify the root destination directory of generated code.
   * If not specified, the default value 'TARGET-PROJECT-DIR/build/generated/openapi' will be used.
   */
  val outputDir: Property<String>
}
