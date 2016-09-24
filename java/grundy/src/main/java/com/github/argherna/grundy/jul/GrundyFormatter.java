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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Print a precise summary of the {@code LogRecord} in a human readable format. The summary will
 * typically be 1 or 2 lines.
 *
 * <p>
 * <a name="formatting"> <b>Configuration:</b></a> The {@code GrundyFormatter} is initialized with
 * the {@linkplain java.util.Formatter format string} specified in the
 * {@code com.github.argherna.grundy.jul.GrundyFormatter.format} property to {@linkplain #format
 * format} the log messages. This property can be defined in the {@linkplain LogManager#getProperty
 * logging properties} configuration file or as a system property. If this property is set in both
 * the logging properties and system properties, the format string specified in the system property
 * will be used. If this property is not defined or the given format string is
 * {@linkplain java.util.IllegalFormatException illegal}, the default format is
 * implementation-specific.
 *
 * @see java.util.Formatter
 */
public class GrundyFormatter extends Formatter {

  private final Date dat = new Date();

  private static final String FORMAT = JulSupport.getFormat();

  private static final String THREAD_ID_FORMAT = JulSupport.getThreadIDFormat();

  /**
   * Format the given LogRecord.
   * <p>
   * The formatting can be customized by specifying the {@link java.util.Formatter format string} in
   * the <a href="#formatting">{@code com.github.argherna.grundy.jul.GrundyFormatter.format}</a>
   * property. The given {@code LogRecord} will be formatted as if by calling:
   *
   * <pre>
   *    {@link String#format String.format}(format, date, source, logger, level, message, thrown, threadIdentifier, sequenceNumber);
   * </pre>
   *
   * where the arguments are:<br>
   * <ol>
   * <li>{@code format} - the {@link java.util.Formatter java.util.Formatter} format string
   * specified in the {@code java.util.logging.SimpleFormatter.format} property or the default
   * format.</li>
   * <li>{@code date} - a {@link Date} object representing {@linkplain LogRecord#getMillis event
   * time} of the log record.</li>
   * <li>{@code source} - a string representing the caller, if available; otherwise, the logger's
   * name.</li>
   * <li>{@code logger} - the logger's name.</li>
   * <li>{@code level} - the {@linkplain Level#getLocalizedName log level}.</li>
   * <li>{@code message} - the formatted log message returned from the
   * {@link Formatter#formatMessage(LogRecord)} method. It uses {@link java.text.MessageFormat
   * java.text} formatting and does not use the {@code java.util.Formatter format} argument.</li>
   * <li>{@code thrown} - a string representing the {@linkplain LogRecord#getThrown throwable}
   * associated with the log record and its backtrace beginning with a newline character, if any;
   * otherwise, an empty string.</li>
   * <li>{@code threadIdentifier} - a string representing the name or thread ID of the thread that
   * created the LogRecord. This is set by the
   * {@code com.github.argherna.grundy.jul.GrundyFormatter.threadIDFormat} and defaults to
   * {@code name}. Other allowed value is {@code id} which will print the thread Id number. Logging
   * the thread name requires more analysis of the record and could result in decreased performance.
   * </li>
   * <li>{@code sequenceNumber} - a {@link Long} object representing the LogRecord's sequence
   * number.</li>
   * </ol>
   *
   * <p>
   * Stack traces are indented by 1 space to make searching the logs easier.
   * </p>
   *
   * <p>
   * Some example formats:<br>
   * <ul>
   * <li> {@code com.github.argherna.grundy.jul.GrundyFormatter.format="%4$s: %5$s [%1$tc]%n"}
   * <p>
   * This prints 1 line with the log level ({@code 4$}), the log message ({@code 5$}) and the
   * timestamp ({@code 1$}) in a square bracket.
   *
   * <pre>
   *     WARNING: warning message [Tue Mar 22 13:11:31 PDT 2011]
   * </pre>
   *
   * </li>
   * <li>
   * {@code com.github.argherna.grundy.jul.GrundyFormatter.format="%1$tc %2$s%n%4$s: %5$s%6$s%n"}
   * <p>
   * This prints 2 lines where the first line includes the timestamp ({@code 1$}) and the source (
   * {@code 2$}); the second line includes the log level ({@code 4$}) and the log message ({@code 5$}
   * ) followed with the throwable and its backtrace ({@code 6$}), if any:
   *
   * <pre>
   *     Tue Mar 22 13:11:31 PDT 2011 MyClass fatal
   *     SEVERE: several message with an exception
   *      java.lang.IllegalArgumentException: invalid argument
   *             at MyClass.mash(MyClass.java:9)
   *             at MyClass.crunch(MyClass.java:6)
   *             at MyClass.main(MyClass.java:3)
   * </pre>
   *
   * </li>
   * <li>
   * {@code com.github.argherna.grundy.jul.GrundyFormatter.format="%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%n"}
   * <p>
   * This prints 2 lines similar to the example above with a different date/time formatting and does
   * not print the throwable and its backtrace:
   *
   * <pre>
   *     Mar 22, 2011 1:11:31 PM MyClass fatal
   *     SEVERE: several message with an exception
   * </pre>
   *
   * </li>
   * </ul>
   * <p>
   * This method can also be overridden in a subclass. It is recommended to use the
   * {@link Formatter#formatMessage} convenience method to localize and format the message field.
   *
   * @param record the log record to be formatted.
   * @return a formatted log record
   */
  @Override
  public synchronized String format(LogRecord record) {

    dat.setTime(record.getMillis());
    String source;
    if (record.getSourceClassName() != null) {
      source = record.getSourceClassName();
      if (record.getSourceMethodName() != null) {
        source += " " + record.getSourceMethodName();
      }
    } else {
      source = record.getLoggerName();
    }
    String message = formatMessage(record);
    String throwable = "";
    if (record.getThrown() != null) {
      StringBuilder sb = new StringBuilder(System.lineSeparator()).append(" ");
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      record.getThrown().printStackTrace(pw);
      pw.close();
      sb.append(sw.getBuffer());
      throwable = sb.toString();
    }

    int threadID = record.getThreadID();
    String loggedThreadID =
        THREAD_ID_FORMAT.equals("name") ? JulSupport.getThreadName(threadID) : Integer
            .toString(threadID);


    return String.format(FORMAT, dat, source, record.getLoggerName(), record.getLevel()
        .getLocalizedName(), message, throwable, loggedThreadID, record.getSequenceNumber());
  }
}
