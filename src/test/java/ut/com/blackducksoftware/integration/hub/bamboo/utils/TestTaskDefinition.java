package ut.com.blackducksoftware.integration.hub.bamboo.utils;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRootDirectorySelector;

public class TestTaskDefinition implements TaskDefinition {
	private static final long serialVersionUID = 6572977600672994755L;

	private Map<String, String> configMap = new HashMap<String, String>();

	public long getId() {
		return 1;
	}

	public String getPluginKey() {

		return null;
	}

	public String getUserDescription() {
		return null;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isFinalising() {
		return false;
	}

	public Map<String, String> getConfiguration() {
		return configMap;
	}

	public TaskRootDirectorySelector getRootDirectorySelector() {
		return null;
	}

	public void setConfiguration(final Map<String, String> configMap) {
		this.configMap = configMap;
	}

	public void setEnabled(final boolean arg0) {

	}

	public void setFinalising(final boolean arg0) {

	}

	public void setRootDirectorySelector(final TaskRootDirectorySelector arg0) {

	}

	public void setUserDescription(final String arg0) {

	}
}
