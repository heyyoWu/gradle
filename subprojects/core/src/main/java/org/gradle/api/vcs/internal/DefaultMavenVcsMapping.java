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

public class DefaultMavenVcsMapping implements MavenVcsMapping {
    private final String group;
    private final String module;

    public DefaultMavenVcsMapping(String group, String module) {
        this.group = group;
        this.module = module;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultMavenVcsMapping that = (DefaultMavenVcsMapping) o;

        if (!group.equals(that.group)) {
            return false;
        }
        return module.equals(that.module);
    }

    @Override
    public int hashCode() {
        int result = group.hashCode();
        result = 31 * result + module.hashCode();
        return result;
    }
}
