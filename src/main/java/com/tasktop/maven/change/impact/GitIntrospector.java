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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.google.inject.internal.util.ImmutableSet;

class GitIntrospector {

	public static GitIntrospector create(File directory) {
		return new GitIntrospector(findRepository(directory));
	}

	private static Repository findRepository(File directory) {
		try {
			return new FileRepositoryBuilder().findGitDir(directory).build();
		} catch (IOException e) {
			throw new GitIntrospectionException("Cannot find Git dir: " + e.getMessage(), e);
		}
	}

	private Repository repository;

	private GitIntrospector(Repository repository) {
		this.repository = requireNonNull(repository);
	}

	public Repository getRepository() {
		return repository;
	}

	public Set<Path> computeAffectedPathsOfLastCommit() {
		try {
			Set<Path> paths = new HashSet<>();
			try (RevWalk walk = new RevWalk(repository)) {
				ObjectId head = repository.resolve(Constants.HEAD);
				if (head == null) {
					throw new IllegalStateException("No head!");
				}
				RevCommit headCommit = walk.parseCommit(head);
				if (headCommit == null) {
					throw new IllegalStateException("No latest commit!");
				}
				RevCommit parentCommit = headCommit.getParent(0);
				parentCommit = walk.parseCommit(parentCommit.getId());

				AbstractTreeIterator commitIterator = createTreeIterator(repository, headCommit.getTree().getId());
				AbstractTreeIterator parentCommitIterator = createTreeIterator(repository,
						parentCommit.getTree().getId());

				try (DiffFormatter diffFormatter = new DiffFormatter(new ByteArrayOutputStream())) {
					diffFormatter.setRepository(repository);
					List<DiffEntry> entries = diffFormatter.scan(parentCommitIterator, commitIterator);
					for (DiffEntry entry : entries) {
						addPath(paths, entry.getNewPath());
						addPath(paths, entry.getOldPath());
					}
				}
			}
			return ImmutableSet.copyOf(paths);
		} catch (IOException e) {
			throw new GitIntrospectionException("Cannot compute latest change: " + e.getMessage(), e);
		}
	}

	private void addPath(Set<Path> paths, String path) {
		if (path != null) {
			paths.add(FileSystems.getDefault().getPath(path));
		}
	}

	private AbstractTreeIterator createTreeIterator(Repository repository, ObjectId treeId)
			throws IncorrectObjectTypeException, IOException {
		try (ObjectReader reader = repository.newObjectReader()) {
			return new CanonicalTreeParser(null, reader, treeId);
		}
	}
}
