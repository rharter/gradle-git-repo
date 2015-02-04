package com.ryanharter.gradle.gitrepo

/**
 * Created by rharter on 1/30/15.
 */
class GitRepoPluginTest extends GroovyTestCase {
    
    def baseFilename = "/some/path/to/file"
    
    GitRepoPlugin plugin
    
    void setUp() {
        plugin = new GitRepoPlugin()
    }
    
    void testOpenRepository() {
    
    }

    void testIncrementFilename_incrementsNormalFileName() {
        assertEquals("/file/name-1", plugin.incrementFilename("${"/file/name"}"))
        assertEquals("/file/name-2", plugin.incrementFilename("${"/file/name-1"}"))
        assertEquals("/file/name-3", plugin.incrementFilename("${"/file/name-2"}"))
    }

    void testCloneBaseDir() {

    }
}
