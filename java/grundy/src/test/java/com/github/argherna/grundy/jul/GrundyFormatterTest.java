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

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class GrundyFormatterTest {

  @Test
  public void defaultFormattedRecordIsOK() {
    LogRecord lr = new LogRecord(Level.INFO, "From unit test");
    Throwable t = new Throwable();
    StackTraceElement[] stackTrace = t.getStackTrace();
    lr.setSourceClassName(stackTrace[0].getClassName());
    lr.setSourceMethodName(stackTrace[0].getMethodName());
    lr.setLoggerName("unit_test_logger");
    lr.setThrown(t);
    GrundyFormatter formatter = new GrundyFormatter();
    String actual = formatter.format(lr);
    System.err.printf("%s%n", actual);
  }
}
