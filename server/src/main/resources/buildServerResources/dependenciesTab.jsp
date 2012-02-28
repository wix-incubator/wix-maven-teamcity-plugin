<jsp:useBean id="module" scope="request" type="java.lang.String"/>
<jsp:useBean id="fullTrace" scope="request" type="java.lang.String"/>
<jsp:useBean id="resultType" scope="request" type="java.lang.String"/>
<jsp:useBean id="buildTypeId" scope="request" type="java.lang.String"/>
<jsp:useBean id="buildPlan" scope="request" type="java.lang.String"/>
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
    .test {font-size:10px; color:#5229A3;background-color: #E0D5F9; border:solid #5229A3 1px; display:inline-block;border-radius: 4px; padding:0 3px;}
    .provided {font-size:10px; color:#666;background-color: #ddd; border:solid #666 1px; display:inline-block;border-radius: 4px; padding:0 3px;}
    .optional {font-size:10px; color:#5A6986;background-color: #DEE5F2; border:solid #5A6986 1px; display:inline-block;border-radius: 4px; padding:0 3px;}
    .build-type-label {font-size:10px; color: #274c85;background-color: #DEE5F2; border:solid #274c85 1px; display:inline-block;border-radius: 4px; padding:0 3px;}
    .build-plan-item {font-size:12px; color: #274c85;padding:0 3px; height: 15px;text-decoration: none;}
    .requires-build-label {font-size:10px; color: #781515;background-color: #fcc3c4; border:solid #781515 1px; display:inline-block;border-radius: 4px; padding:0 3px;height: 13px}
    .groupBox{padding: 4px 0 0 5px}
    .module-dependencies-div{padding: 10px}

    .dep-m-info {color:#38761D}
    .dep-m-progress {color:#777}
    .dep-m-error {color:#B71515}

</style>
<script type="text/javascript">

    DA = (function() {
        var counter = 0;

        return {
            progressToken: 0,

            init: function(dependencies, resultType, fullTrace, buildPlan) {
                jQuery("#dep-message").empty();
                switch (resultType) {
                    case "current":
                        this.renderBuildPlan("#dep-build-dependencies", buildPlan)
                        this.renderTree("#dep-module-dependencies", dependencies);
                        this.showTitles(true, true, false);
                        break;
                    case "needsRefresh":
                        jQuery("#dep-message").html("<p>New revisions detected in VCS. Project structure may have changed. It is suggested to refresh the dependencies.</p>");
                        this.renderBuildPlan("#dep-build-dependencies", buildPlan)
                        this.renderTree("#dep-module-dependencies", dependencies);
                        this.showTitles(true, true, true);
                        break;
                    case "exception":
                        jQuery("#dep-message").html("<h3>Exception during Dependencies Collection</h3>");
                        this.showTitles(false, false, true);
                        this.renderTrace("dep-message", fullTrace);
                        break;
                    case "runningAsync":
                        jQuery("#dep-message").html("<p>Dependencies collection is running...</p>");
                        this.showTitles(false, false, true);
                        this.startProgressPuller(buildTypeId);
                        break;
                    case "notRun":
                        jQuery("#dep-message").html("<p>Dependencies collection was not run for the current build configuration</p>");
                        this.showTitles(false, false, true);
                        break;
                    default:
                        jQuery("#dep-message").html("<p>Should not get here - got unexpected result type ["+resultType+"]</p>");
                        this.showTitles(false, false, true);
                }
            },

            showTitles: function(build, module, messages) {
                if (build)
                    jQuery("#h2-build-dependencies").show();
                else
                    jQuery("#h2-build-dependencies").hide();
                if (module)
                    jQuery("#h2-module-dependencies").show();
                else
                    jQuery("#h2-module-dependencies").hide();
                if (messages)
                    jQuery("#h2-message").show();
                else
                    jQuery("#h2-message").hide();
            },

            renderBuildPlan: function (target, buildPlanItems) {
                var html = "<ul>";
                for (var d=0; d < buildPlanItems.length; d++) {
                    var item = buildPlanItems[d];
                    var renderLink = (d>0) && item.buildTypeId.known;
                    var thisItem = d==0;
                    html += "<li style=\'line-height: 1em;\'>"+
                            (item.needsBuild?"<div class=\'requires-build-label\'>Requires Building</div>":"")+
                            (thisItem?"<div class=\'module\'>this</div>  ":"")+
                            (renderLink?"<a href=\'/viewType.html?buildTypeId="+item.buildTypeId.buildTypeId+"\' target=\'_blank\'>":"") +
                            "<span class=\'build-plan-item\'>"+
                            item.buildTypeId.projectName +":" + item.buildTypeId.name +
                            "</span>" +
                            (renderLink?"</a>":"") +
                            (item.needsBuild?" - " + item.description:"") + "</li>";
                }
                html += "</ul>";
                jQuery(target).html(html)
            },

            renderTree: function (target, module) {
                this.markDependenciesWhoAreModules(module);
                jQuery(target).html(this.renderModule(module))
            },

            renderModule: function (module) {
                var hasChildren = (module.dependencyTree && module.dependencyTree.dependencies && module.dependencyTree.dependencies.length > 0) ||
                        (module.subModules && module.subModules.length > 0);

                var html = "";
                if (module.dependencyTree && module.dependencyTree.dependencies && module.dependencyTree.dependencies.length > 0) {
                    module.dependencyTree.dependencies.sort(this.compareDependencies);
                    html += "<div class=\'module-dependencies\'>";
                    for (var d =0; d < module.dependencyTree.dependencies.length; d++) {
                        html += this.renderDependency(module.dependencyTree.dependencies[d])
                    }
                    html += "</div>";
                }
                module.subModules.sort(this.compareModules);
                for (var m=0; m < module.subModules.length; m++) {
                    html += this.renderModule(module.subModules[m])
                }

                var isOpen = module.subModules && module.subModules.length > 0;

                return this.renderNode("<div class=\'module\'> module </div><span class='tree-text'> " + module.groupId + ":" + module.artifactId + ":" + module.version + "</span>",
                        html, isOpen, hasChildren);
            },

            compareModules: function (moduleA, moduleB){
                var groupCompare = moduleA.groupId.localeCompare(moduleB.groupId);
                if (groupCompare == 0)
                    return moduleA.artifactId.localeCompare(moduleB.artifactId);
                else
                    return groupCompare;
            },

            renderDependency: function (dependency) {
                var hasChildren = (dependency.dependencies && dependency.dependencies.length > 0);
                var html = "";
                if (dependency.dependencies) {
                    dependency.dependencies.sort(this.compareDependencies);
                    for (var d = 0; d <dependency.dependencies.length; d++) {
                        html += this.renderDependency(dependency.dependencies[d])
                    }
                }
                return this.renderNode(
                        "<div class=\'dependency\'> dependency </div>"+
                                (dependency.isModule?" <div class=\'module\'> module </div>":"") +
                                (dependency.scope=="test"?" <div class=\'test\'> test </div>":"") +
                                (dependency.scope=="provided"?" <div class=\'provided\'> provided </div>":"") +
                                (dependency.scope=="isOptional"?" <div class=\'optional\'> optional </div>":"") +
                                ((dependency.buildTypeId && !dependency.isModule)?" <div class=\'build-type-label\'> "+dependency.buildTypeId.projectName+":"+dependency.buildTypeId.name+" </div>":"") +
                                "<span class='tree-text'> " + dependency.groupId + ":" + dependency.artifactId + ":" + dependency.version + "</span>"
                        ,
                        html, false, hasChildren);
            },

            compareDependencies: function (depA, depB) {
                var moduleCompare = (depA.isModule == depB.isModule)?0:(depA.isModule)?-1:1;
                var scopeCompare = depA.scope.localeCompare(depB.scope);
                var groupCompare = depA.groupId.localeCompare(depB.groupId);
                if (moduleCompare != 0)
                    return moduleCompare;
                else if (scopeCompare != 0)
                    return scopeCompare;
                else if (groupCompare != 0)
                    return groupCompare;
                else
                    return depA.artifactId.localeCompare(depB.artifactId);
            },

            renderNode: function(nodeHtml, childrenHtml, isOpen, hasChildren) {
                var id = counter++;
                var moduleBoxId = "tbox_" + id;
                var childId = "tchild_" + id;

                if (hasChildren)
                    return this.renderNodeWithChildren(nodeHtml, childrenHtml, isOpen, hasChildren, moduleBoxId, childId);
                else
                    return this.renderNodeNoChildren(nodeHtml);
            },

            renderNodeWithChildren: function (nodeHtml, childrenHtml, isOpen, hasChildren, moduleBoxId, childId) {
                return "<div class='tree-div'>"+
                        "<a href=\"javascript:DA.toggleNode(\'"+moduleBoxId+"\', \'"+childId+"\')\">"+
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
            },

            renderNodeNoChildren: function (nodeHtml) {
                return "<div class='tree-div'>" +
                        "<div class='tree-nochildren-box' style='margin: 5px 2px 0 2px; display:inline-block;'></div>" +
                        nodeHtml +
                        "</div>";
            },

            toggleNode: function (boxDivId, childId) {
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

            },

            markDependenciesWhoAreModules: function (module) {
                var allModuleIDs = this._getAllModules(module);
                this._markDependenciesWhoAreModulesOfModule(module, allModuleIDs);
            },

            _getAllModules: function (module) {
                var allModules = [this.artifactId(module)];
                for (var m=0; m < module.subModules.length; m++) {
                    allModules = allModules.concat(this._getAllModules(module.subModules[m]));
                }
                return allModules;
            },

            _markDependenciesWhoAreModulesOfModule: function (module, allModuleIDs) {
                for (var m=0; m < module.subModules.length; m++) {
                    this._markDependenciesWhoAreModulesOfModule(module.subModules[m], allModuleIDs);
                }
                this._markDependenciesWhoAreModulesOfDependency(module.dependencyTree, allModuleIDs);
            },

            _markDependenciesWhoAreModulesOfDependency: function (dependency, allModuleIDs) {
                var aId = this.artifactId(dependency);
                dependency.isModule = (allModuleIDs.indexOf(aId) > -1);
                if (dependency.dependencies) {
                    for (var d = 0; d <dependency.dependencies.length; d++) {
                        this._markDependenciesWhoAreModulesOfDependency(dependency.dependencies[d], allModuleIDs)
                    }
                }
            },

            artifactId: function (artifact) {
                return artifact.groupId+":"+artifact.artifactId+":"+artifact.version;
            },

            renderTrace: function (target, fullTrace) {
                var html = "";
                for (var m=0; m<fullTrace.length; m++) {
                    html += this.renderMessage(fullTrace[m]);
                    if (m < fullTrace.length -1)
                        html += "<br/>";
                }
                jQuery("#"+target).html(html)
            },

            appendMessages: function (target, messages) {
                var html = "";
                for (var m=0; m < messages.length; m++) {
                    html += "<br/>" + this.renderMessage(messages[m]);
                }
                jQuery(target).append(html)
            },

            // gets a message object {
            //   messageType:[info/progress/error],
            //   exception:{exceptionClass:String, exceptionMessage:String, cause: Exception},
            //   message:String}
            renderMessage: function(message) {
                var messageClass = this.getMessageClass(message);
                var html = "<span class=\'" + messageClass +"\'>"+message.message;
                if (message.exception)
                  html += this.renderMessageException(message.exception);
                html += "</span>";
                return html;
            },

            renderMessageException: function(messageException) {
                var html = "<br/>caused by " + messageException.exceptionClass + ": " + messageException.exceptionMessage ;
                if (messageException.cause)
                    html += this.renderMessageException(messageException.cause);
                return html;
            },

            getMessageClass: function(message) {
                return "dep-m-" + message.messageType;
            },

            collectDependencies: function (buildTypeId) {
                new Ajax.Request("/maven-dependencies-plugin.html", {
                    method: 'get',
                    parameters: {action:"forceAnalyzeDependencies", id:buildTypeId},
                    onSuccess: function(transport){
                        DA.showTitles(false, false, true);
                        jQuery("#dep-build-dependencies").empty();
                        jQuery("#dep-module-dependencies").empty();
                        jQuery("#dep-message").empty();
                        DA.startProgressPuller(buildTypeId);
                    },
                    onFailure: function(){ alert('Failed to start collecting dependencies') }
                });
            },

            startProgressPuller: function(buildTypeId) {
                DA.progressToken = 0;
                jQuery("#refreshDependencies").hide();
                new PeriodicalExecuter(function(pe) {
                    new Ajax.Request("/maven-dependencies-plugin.html", {
                        method: 'get',
                        parameters: {action:"progress", id:buildTypeId, token: DA.progressToken},
                        onSuccess: function(transport){
                            DA.progressToken = transport.responseJSON.position;
                            DA.appendMessages("#dep-message", transport.responseJSON.messages);
                            if (transport.responseJSON.completed) {
                                pe.stop();
                                jQuery("#refreshDependencies").show();
                                if (transport.responseJSON.ok) {
                                    DA.getDependencies(buildTypeId);
                                }
                            }
                        },
                        onFailure: function(){ }
                    });
                }, 2);
            },

            getDependencies: function(buildTypeId) {
                new Ajax.Request("/maven-dependencies-plugin.html", {
                    method: 'get',
                    parameters: {action:"getBuildDependencies", id:buildTypeId},
                    onSuccess: function(transport){
                        DA.init(transport.responseJSON.module, transport.responseJSON.resultType, transport.responseJSON.fullTrace, transport.responseJSON.buildPlan)
                    },
                    onFailure: function(){ alert('Failed to get dependencies') }
                });
            }

        }})();

</script>
<div class="module-dependencies-div">
    <h2  id="h2-build-dependencies" class="groupTitle">Build Dependencies:</h2>
    <div id="dep-build-dependencies" class="groupBox"></div>
    <h2  id="h2-module-dependencies" class="groupTitle">Module Dependencies:</h2>
    <div id="dep-module-dependencies" class="groupBox"></div>
    <h2  id="h2-message" class="groupTitle">Messages:</h2>
    <div id="dep-message" class="groupBox"></div>
    <div id="dep-actions" class="groupBox">
        <input title="Click to run Maven dependencies collection" id="refreshDependencies" type="button" class="action"
               onclick="DA.collectDependencies(buildTypeId);" value="Collect Dependencies"/>
    </div>
</div>
<script type="text/javascript">
    var dependencies = ${module};
    var resultType = "${resultType}";
    var fullTrace = ${fullTrace};
    var buildTypeId = "${buildTypeId}";
    var buildPlan = ${buildPlan};
    DA.init(dependencies, resultType, fullTrace, buildPlan);
</script>
