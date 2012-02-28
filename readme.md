Maven Dependencies Tab Plugin
======================

The Maven dependencies tab plugin adds a tab to a build configuration displaying the TeamCity build dependencies based on the Maven dependencies of the project.
The plugin displays result such as

![Maven Dependencies Tab example](https://github.com/wix/wix-maven-teamcity-plugin/blob/master/example.jpg?raw=true "Maven Dependencies Tab example")

Build Dependencies
-------------------------

In the Build Dependencies section we see a list of Build Configurations that are dependencies of the current build configuration. The dependencies are sorted (via topological sorting), 
such that our build configuration is on the top, and each build configuration in the list depends only on build configurations below it.
For each, we see a label "require building" if we determine it requires building, as well as a text explaining why we think so.

We compute the "require building" by looking at each of the build configurations, checking for pending changes or if it does not have any successful builds. 
We then compare the last successful build dates of build configurations, requiring that a dependent build configuration latest build will have a later date then the build configuration it depends on.


Module Dependencies
---------------------

In the module dependencies section we see the modules of our build configuration (marked with the yellow "module" label) and the maven dependencies of each module (in the green box).
The dependencies are captured match like maven dependencies:tree command, except that test and provided scope artifacts are only displayed for first level dependencies (direct module dependencies).

We show labels on the dependencies for 

+  "module" - dependency on another module in the same build configuration
+  "<project name>:<build name>" - dependency on an artifact from another build configuration
+  "test" - test scope dependency
+  "provided" - provided scope dependency
