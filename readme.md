Maven Dependencies Tab Plugin
======================

The Maven dependencies tab plugin adds a tab to a build configuration displaying the TeamCity build dependencies based on the Maven dependencies of the project.
The plugin displays result such as

![Maven Dependencies Tab example](https://github.com/wix/wix-maven-teamcity-plugin/blob/master/example.jpg?raw=true "Maven Dependencies Tab example")

Build Dependencies
-------------------------

In the Build Dependencies section we see a list of Build Configurations that are dependencies of the current build configuration (with links to those configurations).
For each, we see a label "require building" if it has pending changes or if it has a dependency that its last build is newer compared to that build configuration last build.


Module Dependencies
---------------------

In the module dependencies section we see the modules of our build configuration (marked with the yellow "module" label) and the maven dependencies of each module (in the green box).
The dependencies are captured match like maven dependencies:tree command, except that test and provided scope artifacts are only displayed for first level dependencies (direct module dependencies).
