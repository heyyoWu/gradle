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

package org.gradle.api.vcs;

import org.gradle.api.Action;
import org.gradle.api.Incubating;

/**
 * In settings.gradle:
 * <pre>
 * sourceControl {
 *    repositories {
 *        // TODO: Need to create GitVcs
 *        gradle(GitVcs) {
 *            url = "https://github.com/gradle/gradle"
 *        }
 *    }
 *    vcsMappings {
 *        add("gradle", maven("org.gradle", "tooling-api"))
 *    }
 * }
 * </pre>
 * @since 4.2
 */
@Incubating
public interface SourceControl {
    /**
     * Configures the source repositories available from this build.
     *
     * @param configuration the configuration action
     */
    void repositories(Action<? super VcsRepositoryHandler> configuration);

    /**
     * Returns a repository handler for adding source repositories.
     *
     * @return the repositories handler, never null.
     */
    VcsRepositoryHandler getRepositories();

    /**
     * Configures VCS mappings between dependency namespaces (e.g., Maven) and source repositories.
     *
     * @param configuration the configuration action
     */
    void vcsMappings(Action<? super VcsMappingHandler> configuration);
}
