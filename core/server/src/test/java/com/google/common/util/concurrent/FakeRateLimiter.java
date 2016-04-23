/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the “License”). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package com.google.common.util.concurrent;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter.SleepingTicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides a fake {@link RateLimiter} to facilitate testing. Note that in Guava 14.0 the
 * {@link RateLimiter$SleepingTicker} class is package private, so this fake rate limiter has to
 * be in the same package to be able to extend it. Later version of Guava makes it public, so this
 * can be moved to a package belongs to alluxio when updating to a newer version of Guava.
 */
public class FakeRateLimiter {
  private final RateLimiter mRateLimiter;
  private final FakeSleepingTicker mTicker;

  public FakeRateLimiter(double permitsPerSecond) {
    mTicker = new FakeSleepingTicker();
    mRateLimiter = RateLimiter.create(mTicker, permitsPerSecond);
  }

  public RateLimiter getGuavaRateLimiter() {
    return mRateLimiter;
  }

  public void sleepMillis(int millis) {
    mTicker.sleepMillis(millis);
  }

  public List<String> readEventsAndClear() {
    return mTicker.readEventsAndClear();
  }

  /**
   * The sleeping ticker gathers events and presents them as strings.
   * R0.6 means a delay of 0.6 seconds caused by the (R)ateLimiter
   * U1.0 means the (U)ser caused the ticker to sleep for a second.
   */
  private static class FakeSleepingTicker extends SleepingTicker {
    private long mInstant = 0L;
    private final List<String> mEvents = Lists.newArrayList();

    @Override
    public long read() {
      return mInstant;
    }

    void sleepMillis(int millis) {
      sleepMicros("U", MILLISECONDS.toMicros(millis));
    }

    void sleepMicros(String caption, long micros) {
      mInstant += MICROSECONDS.toNanos(micros);
      mEvents.add(caption + String.format(Locale.ROOT, "%3.2f", (micros / 1000000.0)));
    }

    @Override
    protected void sleepMicrosUninterruptibly(long micros) {
      sleepMicros("R", micros);
    }

    List<String> readEventsAndClear() {
      try {
        return new ArrayList<String>(mEvents);
      } finally {
        mEvents.clear();
      }
    }

    @Override
    public String toString() {
      return mEvents.toString();
    }
  }
}
