package com.blackducksoftware.integration.hub.bamboo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.atlassian.bamboo.fileserver.SystemDirectory;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContext;
import com.atlassian.bamboo.plan.artifact.ImmutableArtifactDefinitionBase;

public class BambooFileStorageHelper {

	public final static String ILLEGAL_ARG_MESSAGE_RESULT_KEY = "ResultKey field cannot be null";
	public final static String ILLEGAL_ARG_MESSAGE_ARTIFACT_DEFINITION = "ArtifactDefinition field cannot be null";

	private PlanResultKey resultKey;
	private ArtifactDefinitionContext artifactDefinition;

	public PlanResultKey getResultKey() {
		return resultKey;
	}

	public void setResultKey(final PlanResultKey resultKey) {
		this.resultKey = resultKey;
	}

	public ArtifactDefinitionContext getArtifactDefinition() {
		return artifactDefinition;
	}

	public void setArtifactDefinition(final ArtifactDefinitionContext artifactDefinition) {
		this.artifactDefinition = artifactDefinition;
	}

	public File buildArtifactRootDirectory() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (getResultKey() == null) {
			throw new IllegalArgumentException(ILLEGAL_ARG_MESSAGE_RESULT_KEY);
		}

		if (getArtifactDefinition() == null) {
			throw new IllegalArgumentException(ILLEGAL_ARG_MESSAGE_ARTIFACT_DEFINITION);
		}

		File planRoot = null;

		try { // pre-bamboo 5.11 servers
			planRoot = getArtifactRootDirectory(getResultKey(), getArtifactDefinition());
		} catch (final Throwable t) { // bamboo 5.11 servers and higher
			planRoot = getArtifactRootDirectoryPost5_11(getResultKey(), getArtifactDefinition());
		}

		return planRoot;
	}

	private File getArtifactRootDirectory(final PlanResultKey resultKey,
			final ArtifactDefinitionContext artifactDefinition) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		// From Bamboo 5.4.2 up to 5.10 SystemDirectory contained a
		// getArtifactStorage method
		final Class<?>[] rootFileMethodTypes = { PlanResultKey.class };
		final Object[] artifactStorageParams = { resultKey };
		// SystemDirectory method: getArtifactStorage returns: ArtifactStorage
		// ArtifactStorage method: getArtifactDirectory returns: File
		final File planRoot = getArtifactStorageRoot(resultKey, "getArtifactStorage", "getArtifactDirectory",
				rootFileMethodTypes, artifactStorageParams);
		return new File(planRoot, artifactDefinition.getName());
	}

	private File getArtifactRootDirectoryPost5_11(final PlanResultKey resultKey,
			final ArtifactDefinitionContext artifact) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// From Bamboo 5.11 SystemDirectory.getArtifactStorage was removed.
		final Class<?>[] rootFileMethodTypes = { PlanResultKey.class, ImmutableArtifactDefinitionBase.class };
		final Object[] artifactStorageParams = { resultKey, artifact };
		// SystemDirectory method: getArtifactDirectoryBuilder returns:
		// ArtifactDirectoryBuilder
		// ArtifactDirectoryBuilder method: getPlanOrientedArtifactDirectory
		// returns: File
		// Note the directory for the Artifact Name is included in the path
		// since the definition is passed to the method.
		return getArtifactStorageRoot(resultKey, "getArtifactDirectoryBuilder", "getPlanOrientedArtifactDirectory",
				rootFileMethodTypes, artifactStorageParams);

	}

	private File getArtifactStorageRoot(final PlanResultKey resultKey, final String storageGetMethodName,
			final String rootFileGetMethodName, final Class<?>[] rootFileGetMethodTypes,
			final Object[] getMethodParameters) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// use reflection to find and invoke the correct method to get the
		// artifact storage directory for the plan. Different versions of the
		// Bamboo servers have breaking changes in the API calls to get the
		// directory are different. Using reflection here, although not ideal by
		// any means, allows support for Bamboo 5.10 and higher without
		// additional code changes.
		File planRoot = null;
		try {
			final Class<SystemDirectory> sysDirectoryClass = SystemDirectory.class;
			final Class<?>[] sysDirParamTypes = new Class<?>[0];
			Method method = sysDirectoryClass.getMethod(storageGetMethodName, sysDirParamTypes);
			// call the method on the SystemDirectory class to get the object
			// that contains the method to access the file object.
			final Object[] sysDirMethodParamTypes = null;
			final Object storageObject = method.invoke(null, sysDirMethodParamTypes);

			// find the method on the object returned from the invoked method
			// on SystemDirectory
			method = storageObject.getClass().getMethod(rootFileGetMethodName, rootFileGetMethodTypes);
			// invoke the method to get the file object representing the root
			// directory where artifacts are stored with the build.
			planRoot = (File) method.invoke(storageObject, getMethodParameters);
		} catch (final Throwable t) {
			throw t;
		}
		return planRoot;
	}
}
