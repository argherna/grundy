package com.github.argherna.grundy.jul;

/*-
 * #%L
 * grundy
 * %%
 * Copyright (C) 2016 Andy Gherna
 * %%
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
 * #L%
 */

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Support utility for classes in this package.
 *
 * @author andy
 *
 */
class JulSupport {

  private static final String FORMAT_PROP_KEY = GrundyFormatter.class.getName() + ".format";

  private static final String DEFAULT_FORMAT = "[%1$tc] [%8$d] [%7$s] %3$s %2$s %4$s: %5$s%6$s%n";

  private static final String DEFAULT_THREAD_ID_FORMAT = "name";

  private static final String THREAD_ID_FORMAT_KEY = GrundyFormatter.class.getName()
      + ".threadIDFormat";

  private static final String UNKONWN_THREAD_NAME = "Unknown thread with ID ";

  private static final Object threadMxBeanLock = new Object();

  private static volatile ThreadMXBean threadMxBean = null;

  private static final int THREAD_NAME_CACHE_SIZE = 10000;

  private static ThreadLocal<LinkedHashMap<Integer, String>> threadNameCache =
      new ThreadLocal<LinkedHashMap<Integer, String>>() {

        @Override
        protected LinkedHashMap<Integer, String> initialValue() {
          return new LinkedHashMap<Integer, String>() {

            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Entry<Integer, String> eldest) {
              return (size() > THREAD_NAME_CACHE_SIZE);
            }
          };
        }
      };

  /**
   * Returns the value of the given System property or LogManager property if the System property is
   * not present or {@code null} if none are set.
   *
   * @param key the property name
   * @return the property value
   */
  static final String getProperty(final String key) {
    String property = AccessController.doPrivileged(new PrivilegedAction<String>() {
      @Override
      public String run() {
        return System.getProperty(key);
      }
    });

    if (property == null) {
      property = LogManager.getLogManager().getProperty(key);
    }

    return property;
  }

  /**
   * Returns the LogRecord format string.
   *
   * <p>
   * The format string is read from {@code com.github.argherna.grundy.jul.GrundyFormatter.format}
   * property. If it is not set as a System property or as a LogManager property,
   * {@value #DEFAULT_FORMAT} is returned.
   *
   * @return the format string.
   * @see GrundyFormatter#format(LogRecord)
   */
  static String getFormat() {
    String format = getProperty(FORMAT_PROP_KEY);
    if (format != null) {
      try {
        // Validate the current format
        String.format(format, new Date(), "", "", "", "", "", "", Long.MIN_VALUE);
      } catch (IllegalArgumentException e) {

        // Fall back to the default
        format = DEFAULT_FORMAT;
      }
    } else {
      format = DEFAULT_FORMAT;
    }

    return format;
  }

  static String getThreadIDFormat() {
    String threadIDFormat = getProperty(THREAD_ID_FORMAT_KEY);
    if (threadIDFormat == null) {
      threadIDFormat = DEFAULT_THREAD_ID_FORMAT;
    }

    return threadIDFormat;
  }

  /**
   * Converts a thread ID from the LogRecord to a thread name.
   *
   * <p>
   * Borrowed with <3 from Tomcat-Juli support.
   * </p>
   *
   * @param logRecordThreadId thread id from the log record
   * @return the name of the thread
   * @see <a
   *      href="https://github.com/apache/tomcat/blob/trunk/java/org/apache/juli/OneLineFormatter.java">OneLineFormatter.java</a>
   */
  static String getThreadName(int logRecordThreadId) {
    Map<Integer, String> cache = threadNameCache.get();
    String result = null;

    if (logRecordThreadId > (Integer.MAX_VALUE / 2)) {
      result = cache.get(Integer.valueOf(logRecordThreadId));
    }

    if (result != null) {
      return result;
    }

    if (logRecordThreadId > Integer.MAX_VALUE / 2) {
      result = UNKONWN_THREAD_NAME + logRecordThreadId;
    } else {
      // Double checked locking OK as threadMxBean is volatile
      if (threadMxBean == null) {
        synchronized (threadMxBeanLock) {
          if (threadMxBean == null) {
            threadMxBean = ManagementFactory.getThreadMXBean();
          }
        }
      }
      ThreadInfo threadInfo = threadMxBean.getThreadInfo(logRecordThreadId);
      if (threadInfo == null) {
        return Long.toString(logRecordThreadId);
      }
      result = threadInfo.getThreadName();
    }

    cache.put(Integer.valueOf(logRecordThreadId), result);

    return result;
  }
}
