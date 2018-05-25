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

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class Paths {

	private Set<Path> referencePaths;

	public Paths(Set<Path> referencePaths) {
		this.referencePaths = new HashSet<>(requireNonNull(referencePaths));
	}

	public Set<Path> toRelativePaths(Path referencePath) {
		return referencePaths.stream().map(f -> referencePath.relativize(f)).collect(Collectors.toSet());
	}

	public boolean parentAnyOf(Set<Path> candidates) {
		return candidates.stream().anyMatch(this::parents);
	}

	private boolean parents(Path candidatePath) {
		return referencePaths.stream().anyMatch(path -> candidatePath.startsWith(path));
	}
}