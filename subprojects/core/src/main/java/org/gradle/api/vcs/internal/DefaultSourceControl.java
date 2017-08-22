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

import com.google.common.collect.ImmutableSetMultimap;
import org.gradle.api.Action;
import org.gradle.api.vcs.VcsMapping;
import org.gradle.api.vcs.VcsMappingHandler;
import org.gradle.api.vcs.VcsRepository;
import org.gradle.api.vcs.VcsRepositoryHandler;
import org.gradle.internal.reflect.Instantiator;

public class DefaultSourceControl implements SourceControlInternal {
    private final VcsRepositoryHandler vcsRepositoryHandler;
    private final VcsMappingHandlerInternal vcsMappingHandler;

    public DefaultSourceControl(Instantiator instantiator) {
        this.vcsRepositoryHandler = instantiator.newInstance(DefaultVcsRepositoryHandler.class, VcsRepository.class, instantiator);
        // TODO: This should be removed and the binding should be provided by a Git-vcs-repository plugin
        vcsRepositoryHandler.registerBinding(VcsRepository.class, DefaultVcsRepository.class);
        this.vcsMappingHandler = new DefaultVcsMappingHandler(vcsRepositoryHandler);
    }

    DefaultSourceControl(VcsRepositoryHandler vcsRepositoryHandler, VcsMappingHandlerInternal vcsMappingHandler) {
        this.vcsRepositoryHandler = vcsRepositoryHandler;
        this.vcsMappingHandler = vcsMappingHandler;
    }

    @Override
    public void repositories(Action<? super VcsRepositoryHandler> configuration) {
        configuration.execute(vcsRepositoryHandler);
    }

    @Override
    public VcsRepositoryHandler getRepositories() {
        return vcsRepositoryHandler;
    }

    @Override
    public void vcsMappings(Action<? super VcsMappingHandler> configuration) {
        configuration.execute(vcsMappingHandler);
    }

    @Override
    public ImmutableSetMultimap<VcsRepository, VcsMapping> getRepositoryToVcsMapping() {
        return ImmutableSetMultimap.copyOf(vcsMappingHandler.getVcsMappings());
    }
}
