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
