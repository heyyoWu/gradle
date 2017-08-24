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

package org.gradle.internal.sources;

import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.SettingsInternal;
import org.gradle.api.vcs.VcsRepository;
import org.gradle.api.vcs.internal.DefaultVcsRepository;
import org.gradle.api.vcs.internal.SourceControlInternal;
import org.gradle.initialization.SettingsLoader;
import org.gradle.internal.hash.HashUtil;
import org.gradle.util.GFileUtils;

import java.io.File;

public class SourceControlSettingsLoader implements SettingsLoader {
    private final SettingsLoader delegate;
    private final SourceControlInternal sourceControlInternal;

    public SourceControlSettingsLoader(SettingsLoader delegate, SourceControlInternal sourceControlInternal) {
        this.delegate = delegate;
        this.sourceControlInternal = sourceControlInternal;
    }

    @Override
    public SettingsInternal findAndLoadSettings(GradleInternal gradle) {
        SettingsInternal settings = delegate.findAndLoadSettings(gradle);
        for (VcsRepository vcsRepository : sourceControlInternal.getResolvedImplicitBuilds()) {
            // TODO: Go checkout VCS repositories, this should happen elsewhere
            File checkoutDir = initialize(gradle.getGradleUserHomeDir(), vcsRepository);
            if (checkoutDir != null) {
                settings.includeBuild(checkoutDir);
            }
        }
        return settings;
    }

    private File initialize(File gradleUserHomeDir, VcsRepository vcsRepository) {
        if (vcsRepository instanceof DefaultVcsRepository) {
            DefaultVcsRepository simpleVcs = (DefaultVcsRepository) vcsRepository;
            String checkoutDir = "vcs/" + HashUtil.createCompactMD5(simpleVcs.getDir().getAbsolutePath());
            File destination = new File(gradleUserHomeDir, checkoutDir);
            GFileUtils.deleteDirectory(destination);
            GFileUtils.copyDirectory(simpleVcs.getDir(), destination);
            return destination;
        }
        return null;
    }
}
