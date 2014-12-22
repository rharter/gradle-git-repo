# git-repo

A gradle plugin that facilitates using a git repository as your maven repo, useful when you want to host private, internal maven artifacts, but a full blown repository manager is more than you nee.

# Usage

Add the plugin as a buildscript dependency, then apply the plugin.

```
buildscript {
	repositories {
		mavenCentral() // not quite there yet
		maven { url "https://raw.githubusercontent.com/rharter/maven-repo/master/snapshots" }
	}
	dependencies {
		classpath 'com.ryanharter:gradle-git-repo:1.0.0-SNAPSHOT'
	}
}

apply plugin: 'git-repo'
```

Then you simply configure the maven `uploadArchives` as you normally would, but provide a git url for the repositories.

```
uploadArchives {
	repositories {
		mavenDeployer {
			repository(url: 'git@github.com:rharter/maven-repo.git/releases')
			snapshotRepository(url: 'git@github.com/rharter/maven-repo.git/snapshots')

            // standard pom settings
		}
	}
}
```

Now when you run `./gradlew uploadArchives` your artifacts will be added to your git repository and pushed.

# License

```
Copyright 2014 Ryan Harter

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```