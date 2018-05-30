package com.tasktop.maven.change.impact;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.junit.Test;

public class GitIntrospectorTest {

	private static final File REPOSITORY_LOCATION = new File("target/git-repo");
	private GitIntrospector introspector = GitIntrospector.create(REPOSITORY_LOCATION);

	@Test
	public void getRepository() {
		Repository repository = introspector.getRepository();
		assertThat(repository).isNotNull();
		assertThat(repository.getRepositoryState()).isEqualTo(RepositoryState.SAFE);
		assertThat(repository.getDirectory().getAbsolutePath())
				.isEqualTo(REPOSITORY_LOCATION.getAbsolutePath() + "/.git");
	}

	@Test
	public void foo() {
		Set<Path> paths = introspector.computeAffectedPathsOfLastCommit();
		assertThat(paths).containsExactlyInAnyOrder(FileSystems.getDefault().getPath("first", "third.txt"),
				FileSystems.getDefault().getPath("second", "fourth.txt"));
	}
}
