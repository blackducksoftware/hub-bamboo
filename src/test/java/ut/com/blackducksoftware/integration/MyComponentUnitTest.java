package ut.com.blackducksoftware.integration;

import org.junit.Test;
import com.blackducksoftware.integration.MyPluginComponent;
import com.blackducksoftware.integration.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}