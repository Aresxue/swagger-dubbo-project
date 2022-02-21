/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.come2future.boot.swagger.dubbo.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtil {

  private static final int EOF = -1;
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  public static int copy(InputStream input, OutputStream output) throws IOException {
    long count = copyLarge(input, output);
    if (count > Integer.MAX_VALUE) {
      return -1;
    }
    return (int) count;
  }

  public static long copyLarge(InputStream input, OutputStream output)
      throws IOException {
    return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
  }

  public static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
      throws IOException {
    long count = 0;
    int n = 0;
    while (EOF != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }
}
