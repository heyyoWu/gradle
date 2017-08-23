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

package org.gradle.api.vcs.internal;

import com.google.common.collect.Multimap;
import org.gradle.api.vcs.VcsMapping;
import org.gradle.api.vcs.VcsMappingHandler;
import org.gradle.api.vcs.VcsRepository;

public interface VcsMappingHandlerInternal extends VcsMappingHandler {
    // TODO: Expose this as extensions or on the public interface
    VcsMapping maven(String group, String name);

    /**
     * Provides association between repository and mapped dependencies
     */
    Multimap<VcsRepository, VcsMapping> getVcsMappings();
}
