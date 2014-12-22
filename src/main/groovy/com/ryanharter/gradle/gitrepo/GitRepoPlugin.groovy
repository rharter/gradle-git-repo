package com.ryanharter.gradle.gitrepo

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Upload
import org.ajoberstar.grgit.Grgit

class GitRepoPlugin implements Plugin<Project> {

    final static ENV_VAR_CLONE_DIR = 'GIT_REPO_CLONE_DIR'

    def grgit;

    void apply(Project project) {
        println 'applying plugin.'
        project.afterEvaluate {
            configureUploadTasks(project);
        }
    }

    def repoPattern = ~/(git@[\w\.@]+[:|\/]([\w,\-,\_]+\\/[\w,\-,\_]+).git){0,1}(\\/{0,1}.*)/

    private void configureUploadTasks(Project project) {
        println 'configuring upload tasks'
        Upload uploadArchives = project.getTasks().withType(Upload.class).findByName('uploadArchives');
        if (uploadArchives == null) {
            println 'no uploadArchives task found.'
            return;
        }

        println "processing repositories: ${uploadArchives.repositories}"
        def repo
        def thePom
        uploadArchives.repositories.all {
            println "         repository: ${repository.url}"
            println " snapshotRepository: ${snapshotRepository.url}"
            thePom = pom

            if (repository.url =~ repoPattern) {
                def parts = repository.url =~ repoPattern
                repo = parts[0][1]
                repository.url = "file://${cloneBaseDir(project)}/${parts[0][2]}${parts[0][3]}"
                println "new repository.url: ${repository.url}"
            }
            if (snapshotRepository.url =~ repoPattern) {
                def parts = snapshotRepository.url =~ repoPattern
                if (repo == null) {
                    repo = parts[0][1]
                }
                snapshotRepository.url = "file://${cloneBaseDir(project)}/${parts[0][2]}${parts[0][3]}"
                println "new snapshotRepository.url: ${snapshotRepository.url}"
            }
        }

        if (repo != null) {
            uploadArchives.doFirst {
                def parts = repo =~ repoPattern
                def localPath = "${cloneBaseDir(project)}/${parts[0][2]}"
                def localDir = new File(localPath)
                if (!localDir.exists()) {
                    localDir.mkdirs()
                    grgit = Grgit.clone(dir: localDir, uri: url);
                } else {
                    grgit = Grgit.open(localDir)
                    grgit.pull()
                }
            }

            uploadArchives.doLast {
                if (grgit == null) {
                    return
                }

                grgit.add(patterns: ['.'])
                grgit.commit(message: "[git-repo] Upload ${thePom.groupId}:${thePom.artifactId}:${thePom.version}", all: true)
                grgit.push(force: true)
            }
        }
    }

    /**
     * Returns the base path to clone git repos into.
     */
    private GString cloneBaseDir(Project project) {
        if (project.hasProperty(ENV_VAR_CLONE_DIR)) {
            return project.property(ENV_VAR_CLONE_DIR);
        } else {
            return "${System.env.HOME}/.git-repo"
        }
    }
}