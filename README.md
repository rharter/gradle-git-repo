# git-repo

A gradle plugin that facilitates using a git repository as your maven repo, useful when you want to host private, internal maven artifacts, but a full blown repository manager is more than you nee.

# Usage

## Creating a repo

Create a git repo to house your artifacts.  If you'd like, the snapshots and releases repositories can be different, or they can be subdirectories of the same repo.

You are welcome to use your git repository's built in access controls, but note that the git-repo plugin only works with ssh urls.

## Deploying

Add the plugin as a `buildscript` dependency, then apply the plugin.

```
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath 'com.ryanharter.gradle-git-repo:gradle-plugin:1.0.1'
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

## Consuming

In the project in which you want to consume archives from your github repo, simply add the repo as a repository in your dependencies section.

```
dependencies {
    repositories {
        mavenCentral()
        maven { url "https://raw.githubusercontent.com/rharter/maven-repo/master/releases" }
        maven { url "https://raw.githubusercontent.com/rharter/maven-repo/master/snapshots" }
    }
    ...
}
```

## Customizing

### Gradle Repo Directory

By default, gradle-git-repo will clone repositories into `~/.git-repo`.  This can be changed using the `GIT_REPO_CLONE_DIR` environment variable.

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