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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class PathsTest {

	@Test
	public void toRelativePathsEmpty() {
		Paths paths = new Paths(Collections.emptySet());
		assertThat(paths.toRelativePaths(createPath("a-path"))).isEmpty();
	}

	@Test
	public void toRelativePaths() {
		Paths paths = new Paths(Collections.singleton(createPath("a-path/file.txt")));
		assertThat(paths.toRelativePaths(createPath("a-path"))).containsExactly(createPath("file.txt"));
	}

	@Test
	public void parentAnyOf() {
		Paths paths = new Paths(Arrays.asList(createPath("a-path/file.txt"), createPath("another-path")));
		assertThat(paths.parentAnyOf(Arrays.asList(createPath("another-path")))).isTrue();
		assertThat(paths.parentAnyOf(Arrays.asList(createPath("another-path/with/child.txt")))).isTrue();
		assertThat(paths.parentAnyOf(Arrays.asList(createPath("a-path")))).isFalse();
		assertThat(paths.parentAnyOf(Arrays.asList(createPath("yet-another-path")))).isFalse();
	}

	private Path createPath(String path) {
		return FileSystems.getDefault().getPath(path);
	}
}
