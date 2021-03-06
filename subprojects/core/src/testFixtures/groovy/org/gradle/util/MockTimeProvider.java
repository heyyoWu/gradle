/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.util;

import org.gradle.internal.time.TimeProvider;

public class MockTimeProvider implements TimeProvider {

    long current;

    public MockTimeProvider() {
        this(System.currentTimeMillis());
    }

    public MockTimeProvider(long startTime) {
        current = startTime;
    }

    public void increment(long diff) {
        current += diff;
    }

    /** Increments the time by 10ms and returns it. */
    @Override
    public long getCurrentTime() {
        current += 10L;
        return current;
    }

}
