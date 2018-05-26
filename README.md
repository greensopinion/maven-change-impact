What Is This?
=============

A Maven plug-in that conditionally sets a property based on whether the latest Git commit affects any files in the transitive dependency chain of the current project.

How Is It Useful?
-----------------

We can dynamically decide which projects should run their tests in a review verification build.  The idea is to build everything, but only run tests for projects that are affected by the latest commit - either directly or via a change to a transitive dependency.

This is useful in cases where some tests (such as integration tests) take a long time to run.  For code review verification it's usually unnecessary to run tests that are not affected by a change.  By avoiding those tests, in some cases we can shorten the feedback cycle time significantly.

How To Use
==========

Configuring
-----------

Install the plug-in in your local Maven repository, then add this to the root pom of your project:

```
  <build>
    <plugins>
      <plugin>
        <groupId>com.tasktop.maven</groupId>
        <artifactId>change-impact</artifactId>
        <version>0.0.2</version>
        <executions>
          <execution>
            <phase>initialize</phase>
            <goals>
              <goal>set-property</goal>
            </goals>
            <configuration>
              <skip>false</skip>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

Properties:
* `<skip>` - defaults to `true`, must be set to `false` for this plug-in to have any effect
* `<property-name>` - defaults to `maven.test.skip`, defines the name of the property to set
* `<true-value>` - the value to set the property to if the latest Git commit affects the current project or one of it's transitive dependencies, defaults to `false`
* `<false-value>` - the value to set the property to if the latest Git commit _does not_ affect the current project or one of it's transitive dependencies, defaults to `true`  

This assumes that your maven-surefire-plugin has the following: `<skip>${maven.test.skip}</skip>`

Running
-------

Run your maven build normally, for example:

```
$ mvn clean package
```

You should see that tests are skipped in every project except for those that are affected by a change in the latest commit.


Status
======

This is a proof-of-concept.

Build status on Travis CI: [![Build Status](https://travis-ci.org/greensopinion/maven-change-impact.svg?branch=master)](https://travis-ci.org/greensopinion/maven-change-impact)


How To Release
--------------

From the command-line:

````
mvn -Possrh -Psign -DpushChanges=false -DlocalCheckout=true -Darguments=-Dgpg.passphrase=thesecret release:clean release:prepare
mvn -Possrh -Psign -DpushChanges=false -DlocalCheckout=true -Darguments=-Dgpg.passphrase=thesecret release:perform
````

Then push changes:

````
git push
````

Search for the staging repository, close and relase it: https://oss.sonatype.org/#stagingRepositories


License
=======

Copyright (c) 2018 Tasktop Technologies

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
