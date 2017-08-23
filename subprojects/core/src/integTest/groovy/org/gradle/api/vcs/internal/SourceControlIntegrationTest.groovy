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

package org.gradle.api.vcs.internal

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class SourceControlIntegrationTest extends AbstractIntegrationSpec {
    def "can define source repositories"() {
        settingsFile << """
            sourceControl {
                repositories {
                    foo(VcsRepository) {
                    }
                    bar(VcsRepository)
                }
            }
        """
        buildFile << """
            import ${SourceControlInternal.canonicalName}

            def sourceControl = gradle.services.get(SourceControlInternal)
            assert sourceControl.repositories*.name.every { it in [ 'foo', 'bar' ] }
        """
        expect:
        succeeds("help")
    }

    def "can define mapping between repository and dependency"() {
        settingsFile << """
            sourceControl {
                repositories {
                    foo(VcsRepository)
                }
                vcsMappings {
                    add("foo", maven("com.example", "foo"))
                }
            }
        """
        buildFile << """
            import ${SourceControlInternal.canonicalName}

            def sourceControl = gradle.services.get(SourceControlInternal)
            def mappings = sourceControl.repositoryToVcsMapping.get(sourceControl.repositories.foo)
            assert !mappings.empty
            def mapping = mappings[0]
            assert mapping instanceof ${MavenVcsMapping.canonicalName}
            assert mapping.group == 'com.example'
            assert mapping.module == 'foo'
        """
        expect:
        succeeds("help")
    }

    def "can define mapping with multiple repositories and dependencies"() {
        settingsFile << """
            sourceControl {
                repositories {
                    foo(VcsRepository)
                    bar(VcsRepository)
                }
                vcsMappings {
                    add("foo", maven("com.example", "foo"))
                    add("foo", maven("com.example", "foo-sub"))
                    add("bar", maven("com.example", "bar"))
                }
            }
        """
        buildFile << """
            import ${SourceControlInternal.canonicalName}

            def sourceControl = gradle.services.get(SourceControlInternal)
            def foo = sourceControl.repositoryToVcsMapping.get(sourceControl.repositories.foo)
            assert foo.size() == 2
            assert foo.every { it instanceof ${MavenVcsMapping.canonicalName} }
            assert foo*.group == [ 'com.example', 'com.example' ]
            assert foo*.module.every { it in [ 'foo', 'foo-sub' ] }

            def bar = sourceControl.repositoryToVcsMapping.get(sourceControl.repositories.bar)
            assert !bar.empty
            assert bar[0] instanceof ${MavenVcsMapping.canonicalName}
            assert bar[0].group == 'com.example'
            assert bar[0].module == 'bar'
        """
        expect:
        succeeds("help")
    }

    def "can handle duplicates of mappings"() {
        settingsFile << """
            sourceControl {
                repositories {
                    foo(VcsRepository)
                }
                vcsMappings {
                    add("foo", maven("com.example", "foo"))
                    add("foo", maven("com.example", "foo"))
                }
            }
        """
        buildFile << """
            import ${SourceControlInternal.canonicalName}

            def sourceControl = gradle.services.get(SourceControlInternal)
            def mappings = sourceControl.repositoryToVcsMapping.get(sourceControl.repositories.foo)
            assert mappings.size() == 1
            def mapping = mappings[0]
            assert mapping instanceof ${MavenVcsMapping.canonicalName}
            assert mapping.group == 'com.example'
            assert mapping.module == 'foo'
        """
        expect:
        succeeds("help")
    }
}
