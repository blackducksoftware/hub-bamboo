/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package ut.com.blackducksoftware.integration.hub.bamboo.tasks;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.blackducksoftware.integration.hub.bamboo.tasks.StreamRedirectThread;

public class StreamRedirectThreadTest {

	@Test
	public void testWritingData() throws Exception {
		final String inputString = "This is a test only a test...";
		final ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		final StreamRedirectThread thread = new StreamRedirectThread(input, output);

		thread.start();
		thread.join();

		assertEquals(inputString, output.toString());
	}
}
