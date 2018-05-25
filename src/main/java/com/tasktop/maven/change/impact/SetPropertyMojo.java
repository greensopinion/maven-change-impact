/*******************************************************************************
 * Copyright (c) 2018 Tasktop Technologies.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.tasktop.maven.change.impact;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

@Mojo(name = SetPropertyMojo.MOJO_NAME, defaultPhase = LifecyclePhase.INITIALIZE)
public class SetPropertyMojo extends AbstractMojo {
	static final String MOJO_NAME = "set-property";
	private static final String LOG_PREFIX = MOJO_NAME + " mojo: ";

	@Parameter(defaultValue = "${project}")
	private org.apache.maven.project.MavenProject project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Parameter(name = "property-name", defaultValue = "affected-by-change")
	private String propertyName;

	@Parameter(name = "true-value", defaultValue = "false")
	private String trueValue;

	@Parameter(name = "false-value", defaultValue = "true")
	private String falseValue;

	@Component(hint = "default")
	private DependencyGraphBuilder dependencyGraphBuilder;

	public void execute() throws MojoExecutionException {
		Set<File> transitiveFolders = new MavenDependencyService(session, dependencyGraphBuilder)
				.computeTransitiveDependencyFolders(project);
		GitIntrospector gitIntrospector = GitIntrospector.create(project.getBasedir());
		File workTree = gitIntrospector.getRepository().getWorkTree();
		Set<Path> dependencyPaths = new Paths(transitiveFolders.stream().map(f -> f.toPath()).collect(toSet()))
				.toRelativePaths(workTree.toPath());
		Set<Path> changedPaths = gitIntrospector.computeAffectedPathsOfLastCommit();

		logDependencyPaths(dependencyPaths);
		logCommitPaths(changedPaths);

		if (new Paths(dependencyPaths).parentAnyOf(changedPaths)) {
			logAffected();
			setProperty(trueValue);
		} else {
			logUnaffected();
			setProperty(falseValue);
		}
	}

	private void setProperty(String value) {
		getLog().info(format("Setting {0} to {1}", propertyName, value));
		project.getProperties().setProperty(propertyName, value);
	}

	private void logDependencyPaths(Set<Path> dependencyPaths) {
		Log log = getLog();
		if (log.isDebugEnabled()) {
			dependencyPaths.stream().sorted().forEach(f -> log.debug(LOG_PREFIX + "Dependency path: " + f));
		}
	}

	private void logAffected() {
		getLog().debug("Changes in commit affect this project");
	}

	private void logUnaffected() {
		getLog().debug("No changes in commit affect this project");
	}

	private void logCommitPaths(Set<Path> commitPaths) {
		Log log = getLog();
		if (log.isDebugEnabled()) {
			commitPaths.stream().sorted().forEach(f -> log.debug(LOG_PREFIX + "Commit paths: " + f));
		}
	}
}
