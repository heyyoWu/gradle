/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.scan.config

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.test.fixtures.plugin.PluginBuilder
import spock.lang.Unroll

import static org.gradle.internal.scan.config.BuildScanPluginAutoApply.BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION

class BuildScanAutoApplyIntegrationTest extends AbstractIntegrationSpec {
    private static final String BUILD_SCAN_PLUGIN_MINIMUM_VERSION = BuildScanPluginCompatibilityEnforcer.MIN_SUPPORTED_VERSION.toString()
    def setup() {
        buildFile << """
            task dummy {}
"""
        settingsFile << """
            pluginManagement {
                repositories {
                    maven { url '${mavenRepo.uri}' }
                }
            }
"""

        publishDummyBuildScanPlugin(BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION)
    }

    def "automatically applies buildscan plugin when --scan is provided on command-line"() {
        when:
        runBuildWithScanRequest()

        then:
        buildScanPluginApplied(BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION)
    }

    def "does not automatically apply buildscan plugin when --scan is not provided on command-line"() {
        when:
        runBuildWithoutScanRequest()

        then:
        buildScanPluginNotApplied()
    }

    def "does not automatically apply buildscan plugin to subprojects"() {
        when:
        settingsFile << """
            include 'a', 'b'
"""
        buildFile << """
            assert pluginManager.hasPlugin('com.gradle.build-scan')
            subprojects {
                assert !pluginManager.hasPlugin('com.gradle.build-scan')
            }
"""

        and:
        runBuildWithScanRequest()

        then:
        buildScanPluginApplied(BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION)
    }

    def "does not apply buildscan plugin to buildSrc build"() {
        when:
        file('buildSrc/build.gradle') << """
            println 'in buildSrc'
            assert !pluginManager.hasPlugin('com.gradle.build-scan')
"""

        and:
        runBuildWithScanRequest()

        then:
        outputContains 'in buildSrc'
        buildScanPluginApplied(BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION)
    }

    def "does not apply buildscan plugin to nested builds in a composite"() {
        when:
        settingsFile << """
            includeBuild 'a'
"""
        file('a/settings.gradle') << """
            rootProject.name = 'a'
"""
        file('a/build.gradle') << """
            println 'in nested build'
            assert !pluginManager.hasPlugin('com.gradle.build-scan')
"""

        and:
        runBuildWithScanRequest()

        then:
        outputContains 'in nested build'
        buildScanPluginApplied(BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION)
    }

    @Unroll
    def "uses #sequence version of plugin when explicit in plugins block"() {
        when:
        if (version != BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION) {
            publishDummyBuildScanPlugin(version)
        }
        pluginsRequest "id 'com.gradle.build-scan' version '$version'"

        and:
        runBuildWithScanRequest()

        then:
        buildScanPluginApplied(version)

        where:
        sequence | version
        "older"  | BUILD_SCAN_PLUGIN_MINIMUM_VERSION
        "same"   | BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION
        "newer"  | "100.0"
    }

    @Unroll
    def "uses #sequence version of plugin when added to buildscript classpath"() {
        when:
        if (version != BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION) {
            publishDummyBuildScanPlugin(version)
        }
        buildscriptApply "com.gradle:build-scan-plugin:$version"

        and:
        runBuildWithScanRequest()

        then:
        buildScanPluginApplied(version)

        where:
        sequence | version
        "older"  | BUILD_SCAN_PLUGIN_MINIMUM_VERSION
        "same"   | BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION
        "newer"  | "100.0"
    }

    def "does not auto-apply buildscan plugin when explicitly requested and not applied"() {
        when:
        pluginsRequest "id 'com.gradle.build-scan' version '${BUILD_SCAN_PLUGIN_AUTO_APPLY_VERSION}' apply false"

        and:
        runBuildWithScanRequest()

        then:
        buildScanPluginNotApplied()
    }

    private void publishDummyBuildScanPlugin(String version) {
        def builder = new PluginBuilder(testDirectory.file('plugin-' + version))
        builder.addPlugin("""
            def gradle = project.gradle
            
            org.gradle.internal.scan.config.BuildScanPluginMetadata buildScanPluginMetadata = { "${version}" } as org.gradle.internal.scan.config.BuildScanPluginMetadata
            gradle.services.get(org.gradle.internal.scan.config.BuildScanConfigProvider).collect(buildScanPluginMetadata)
            
            gradle.buildFinished {
                println 'PUBLISHING BUILD SCAN v${version}'
            }
""", "com.gradle.build-scan", "DummyBuildScanPlugin")
        builder.publishAs("com.gradle:build-scan-plugin:${version}", mavenRepo, executer)
    }

    private void runBuildWithScanRequest() {
        args("--scan")
        succeeds("dummy")
    }

    private void runBuildWithoutScanRequest() {
        succeeds("dummy")
    }

    private void buildScanPluginApplied(String version) {
        assert output.contains("PUBLISHING BUILD SCAN v${version}")
    }

    private void buildScanPluginNotApplied() {
        assert !output.contains("PUBLISHING BUILD SCAN")
    }

    private void pluginsRequest(String request) {
        buildFile.text = """
            plugins {
                ${request}
            }
""" + buildFile.text
    }

    private void buildscriptApply(String coordinates) {
        buildFile.text = """
            buildscript {
                repositories {
                    maven { url '${mavenRepo.uri}' }
                }
                dependencies {
                    classpath '${coordinates}'
                }
            }
            apply plugin: 'com.gradle.build-scan'
""" + buildFile.text
    }
}