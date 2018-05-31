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

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor;

import com.google.inject.internal.util.ImmutableSet;

class MavenDependencyService {
	private MavenSession session;
	private DependencyGraphBuilder graphBuilder;

	public MavenDependencyService(MavenSession session, DependencyGraphBuilder graphBuilder) {
		this.session = requireNonNull(session);
		this.graphBuilder = requireNonNull(graphBuilder);
	}

	public Set<File> computeTransitiveHierarchyFiles(MavenProject project) throws MojoExecutionException {
		Set<File> files = new HashSet<>();
		MavenProject ancestor = project.getParent();
		while (ancestor != null) {
			File file = ancestor.getFile();
			if (file != null) {
				files.add(file);
			}
			ancestor = ancestor.getParent();
		}
		return files;
	}

	public Set<File> computeTransitiveDependencyFolders(MavenProject project) throws MojoExecutionException {
		CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
		ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
		buildingRequest.setProject(project);
		try {
			DependencyNode rootNode = graphBuilder.buildDependencyGraph(buildingRequest, a -> true);
			rootNode.accept(visitor);
		} catch (DependencyGraphBuilderException e) {
			throw new MojoExecutionException("Couldn't build dependency graph", e);
		}
		return ImmutableSet.<File>builder()
				.addAll(visitor.getNodes().stream().map(this::findDependencyProject).filter(Optional::isPresent)
						.map(Optional::get).map(p -> p.getFile().getParentFile()).collect(Collectors.toSet()))
				.add(project.getFile().getParentFile()).build();
	}

	private Optional<MavenProject> findDependencyProject(DependencyNode node) {
		return session.getProjects().stream().filter(p -> p.getGroupId().equals(node.getArtifact().getGroupId())
				&& p.getArtifactId().equals(node.getArtifact().getArtifactId())).findAny();
	}
}
