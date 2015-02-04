package com.ryanharter.gradle.gitrepo

import org.eclipse.jgit.errors.RemoteRepositoryException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Upload
import org.ajoberstar.grgit.Grgit

class GitRepoPlugin implements Plugin<Project> {

    final static ENV_VAR_CLONE_DIR = 'GIT_REPO_CLONE_DIR'
    
    final static DEBUG = false

    def grgit

    void apply(Project project) {
        println 'applying plugin.'
        project.afterEvaluate {
            configureUploadTasks(project);
        }
    }

    def repoPattern = ~/(git@[\w\.@]+[:|\/]([\w,\-,\_]+\\/[\w,\-,\_]+).git){0,1}(\\/{0,1}.*)/

    void configureUploadTasks(Project project) {
        Upload uploadArchives = project.getTasks().withType(Upload.class).findByName('uploadArchives');
        if (uploadArchives == null) {
            println 'no uploadArchives task found.'
            return;
        }

        if (DEBUG) println "processing repositories: ${uploadArchives.repositories}"
        def repo
        def thePom
        uploadArchives.repositories.all {
            if (DEBUG) println "         repository: ${repository.url}"
            if (DEBUG) println " snapshotRepository: ${snapshotRepository.url}"
            thePom = pom

            if (repository.url =~ repoPattern) {
                def parts = repository.url =~ repoPattern
                repo = parts[0][1]
                repository.url = "file://${cloneBaseDir(project)}/${parts[0][2]}${parts[0][3]}"
                if (DEBUG) println "new repository.url: ${repository.url}"
            }
            if (snapshotRepository.url =~ repoPattern) {
                def parts = snapshotRepository.url =~ repoPattern
                if (repo == null) {
                    repo = parts[0][1]
                }
                snapshotRepository.url = "file://${cloneBaseDir(project)}/${parts[0][2]}${parts[0][3]}"
                if (DEBUG) println "new snapshotRepository.url: ${snapshotRepository.url}"
            }
        }

        if (repo != null) {
            uploadArchives.doFirst {
                def parts = repo =~ repoPattern
                def localPath = "${cloneBaseDir(project)}/${parts[0][2]}"
                grgit = openRepository(localPath, "${repo}")
            }

            uploadArchives.doLast {
                if (grgit == null) {
                    println "No grgit instance, exiting"
                    return
                }

                println "Adding files to git."
                grgit.add(patterns: ['.'])
                grgit.commit(message: "[git-repo] Upload ${thePom.groupId}:${thePom.artifactId}:${thePom.version}", all: true)
                grgit.push(force: true)
                println "Pushing files to git."
            }
        }
    }
    
    Grgit openRepository(GString localPath, GString repoUri) {
        def localDir = new File(localPath)
        if (!localDir.exists()) {
            // If the directory doesn't exist, create it and clone into it
            localDir.mkdirs()
            try {
                Grgit.clone(dir: localDir, uri: repoUri)
            } catch (e) {
                println "Error cloning repo $repoUri: ${e}"
            }
        } else {
            // If the directory exists, try to clone into it
            try {
                if (DEBUG) println "Opening local repository: $localDir"
                def grgit = Grgit.open(localDir)
                grgit.pull()
                return grgit
            } catch (RepositoryNotFoundException e) {
                openRepository(incrementFilename(localPath), repoUri)
            } catch (RemoteRepositoryException e) {
                println "Error: No remote repository at url: ${repoUri}"
            }
        }
    }
    
    GString incrementFilename(GString path) {
        if (path ==~ /.+-[0-9+]$/) {
            "${path.toString().next()}"
        } else {
            "${path}-1"
        }
    }

    /**
     * Returns the base path to clone git repos into.
     */
    GString cloneBaseDir(Project project) {
        if (project.hasProperty(ENV_VAR_CLONE_DIR)) {
            return project.property(ENV_VAR_CLONE_DIR);
        } else {
            return "${System.env.HOME}/.git-repo"
        }
    }
}