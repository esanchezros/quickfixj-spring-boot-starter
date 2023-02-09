/*
 * Copyright 2017-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package quickfix;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;

public class MockSystemTimeSource implements SystemTimeSource {
    private long[] systemTimes = {System.currentTimeMillis()};
    private int offset;

    public MockSystemTimeSource() {
        // empty
    }

    public MockSystemTimeSource(long time) {
        setSystemTimes(time);
    }

    public void setSystemTimes(long[] times) {
        systemTimes = times;
    }

    void setSystemTimes(long time) {
        systemTimes = new long[]{time};
    }

    public void setTime(Calendar c) {
        setSystemTimes(c.getTimeInMillis());
    }

    @Override
    public long getTime() {
        if (systemTimes.length - offset > 1) {
            offset++;
        }
        return systemTimes[offset];
    }

    @Override
    public long getTimeFromNanos() {
        return 0;
    }

    public void increment(long delta) {
        if (systemTimes.length - offset == 1) {
            systemTimes[offset] += delta;
        }
    }

    @Override
    public LocalDateTime getNow() {
        // TODO maybe we need nano-precision later on
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(getTime()), ZoneOffset.UTC);
    }

}
