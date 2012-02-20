<jsp:useBean id="module" scope="request" type="java.lang.String"/>
<jsp:useBean id="fullTrace" scope="request" type="java.lang.String"/>
<jsp:useBean id="resultType" scope="request" type="java.lang.String"/>
<%@ include file="/include.jsp" %>
<style type="text/css">
    .tree-div {width: 100%; line-height: 1.0em;}
    .tree-text {font-size:12px; font-family: 'lucida grande', tahoma, verdana, arial, sans-serif;text-decoration: none;}
    .tree-div a {text-decoration: none; color: #000}
    .tree-box {width:7px; height:7px;border: solid #777 1px;position: relative}
    .tree-nochildren-box {width:9px; height:9px;position: relative}
    .tree-box-minus-div {position:absolute; background: #333; left: 1px; width:5px; height: 1px; top: 3px;}
    .tree-closed .tree-box-plus-div {position:absolute; background: #333; left: 3px; width:1px; height: 5px; top: 1px;}
    .tree-children {margin-left: 12px}

    .module-dependencies {border: solid #38761D 1px;background-color: #e9fae2}
    .module {font-size:10px; color:#EC7000;background-color: #FFF0E1; border:solid #EC7000 1px; display:inline-block;border-radius: 4px; padding:0 3px; }
    .dependency {font-size:10px; color:#38761D;background-color: #D9EAD3; border:solid #38761D 1px; display:inline-block;border-radius: 4px; padding:0 3px;}
</style>
<h2>Module Dependencies:</h2>
<script type="text/javascript">
    var dependencies = ${module};
    var resultType = "${resultType}";
    var fullTrace = ${fullTrace};
</script>
<script type="text/javascript">

    function renderTree(target, module) {
        var divTarget = document.getElementById(target);
        markDependenciesWhoAreModules(module);
        divTarget.innerHTML = renderModule(module);
    }

    counter = 0;

    function renderModule(module) {
        var hasChildren = (module.dependencyTree && module.dependencyTree.dependencies && module.dependencyTree.dependencies.length > 0) ||
                (module.subModules && module.subModules.length > 0);

        var html = "";
        if (module.dependencyTree && module.dependencyTree.dependencies && module.dependencyTree.dependencies.length > 0) {
            html += "<div class=\'module-dependencies\'>";
            for (var d =0; d < module.dependencyTree.dependencies.length; d++) {
                html += renderDependency(module.dependencyTree.dependencies[d])
            }
            html += "</div>";
        }
        for (var m=0; m < module.subModules.length; m++) {
            html += renderModule(module.subModules[m])
        }

        var isOpen = module.subModules && module.subModules.length > 0;

        return renderNode("<div class=\'module\'> module </div><span class='tree-text'> " + module.groupId + ":" + module.artifactId + ":" + module.version + "</span>",
                html, isOpen, hasChildren);
    }

    function renderDependency(dependency) {
        var hasChildren = (dependency.dependencies && dependency.dependencies.length > 0);
        var html = "";
        if (dependency.dependencies) {
            for (var d = 0; d <dependency.dependencies.length; d++) {
                html += renderDependency(dependency.dependencies[d])
            }
        }
        return renderNode(
                "<div class=\'dependency\'> dependency </div>"+
                        (dependency.isModule?" <div class=\'module\'> module </div>":"") +
                "<span class='tree-text'> " + dependency.groupId + ":" + dependency.artifactId + ":" + dependency.version + "</span>"
                ,
                html, false, hasChildren);
    }

    function renderNode(nodeHtml, childrenHtml, isOpen, hasChildren) {
        var id = counter++;
        var moduleBoxId = "tbox_" + id;
        var childId = "tchild_" + id;

        if (hasChildren)
            return renderNodeWithChildren(nodeHtml, childrenHtml, isOpen, hasChildren, moduleBoxId, childId);
        else
            return renderNodeNoChildren(nodeHtml);
    }

    function renderNodeWithChildren(nodeHtml, childrenHtml, isOpen, hasChildren, moduleBoxId, childId) {
        return "<div class='tree-div'>"+
                "<a href=\"javascript:toggleNode(\'"+moduleBoxId+"\', \'"+childId+"\')\">"+
                "<div id='"+moduleBoxId+"' class='tree-box "+(isOpen?"tree-open":"tree-closed")+"' style='margin: 5px 2px 0 2px; display:inline-block;'>"+
                "<div class='tree-box-minus-div'></div>"+
                "<div class='tree-box-plus-div'></div>"+
                "</div>"+
                nodeHtml+
                "</a>"+
                "<div style='display:"+(isOpen?"block":"none")+"' class='tree-children' id='"+childId+"'>" +
                (hasChildren?childrenHtml:"")+
                "</div>"+
                "</div>";
    }

    function renderNodeNoChildren(nodeHtml) {
        return "<div class='tree-div'>" +
                "<div class='tree-nochildren-box' style='margin: 5px 2px 0 2px; display:inline-block;'></div>" +
                nodeHtml +
                "</div>";
    }

    function toggleNode(boxDivId, childId) {
        var divTarget = document.getElementById(boxDivId);
        if (divTarget.className.indexOf(" tree-open") > -1)
            divTarget.className = divTarget.className.replace(" tree-open", " tree-closed");
        else
            divTarget.className = divTarget.className.replace(" tree-closed", " tree-open");

        divTarget = document.getElementById(childId);
        if(divTarget.style.display == "none")
            divTarget.style.display = "block";
        else
            divTarget.style.display = "none";

    }

    function markDependenciesWhoAreModules(module) {
        var allModuleIDs = _getAllModules(module);
        _markDependenciesWhoAreModulesOfModule(module, allModuleIDs);
    }

    function _getAllModules(module) {
        var allModules = [artifactId(module)];
        for (var m=0; m < module.subModules.length; m++) {
            allModules = allModules.concat(_getAllModules(module.subModules[m]));
        }
        return allModules;
    }

    function _markDependenciesWhoAreModulesOfModule(module, allModuleIDs) {
        for (var m=0; m < module.subModules.length; m++) {
            _markDependenciesWhoAreModulesOfModule(module.subModules[m], allModuleIDs);
        }
        _markDependenciesWhoAreModulesOfDependency(module.dependencyTree, allModuleIDs);
    }

    function _markDependenciesWhoAreModulesOfDependency(dependency, allModuleIDs) {
        var aId = artifactId(dependency);
        dependency.isModule = (allModuleIDs.indexOf(aId) > -1);
        if (dependency.dependencies) {
            for (var d = 0; d <dependency.dependencies.length; d++) {
                _markDependenciesWhoAreModulesOfDependency(dependency.dependencies[d], allModuleIDs)
            }
        }
    }

    function artifactId(artifact) {
        return artifact.groupId+":"+artifact.artifactId+":"+artifact.version;
    }

</script>
<div id="message"></div>
<div id="target"></div>
<script type="text/javascript">
    switch (resultType) {
        case "current":
            renderTree("target", dependencies);
            break;
        case "needsRefresh":
            document.getElementById("message").innerHTML = "<p>need refresh</p>";
            renderTree("target", dependencies);
            break;
        case "exception":
            document.getElementById("message").innerHTML = "<p>exception</p>";
            break;
        case "runningAsync":
            document.getElementById("message").innerHTML = "<p>running</p>";
            break;
        case "notRun":
            document.getElementById("message").innerHTML = "<p>not run</p>";
            break;
        default:
            document.getElementById("message").innerHTML = "<p>default</p>";
    }

</script>
