/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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
