<jsp:useBean id="commitsToIgnore" scope="request" type="java.lang.String"/>
<%@ include file="/include.jsp" %>
<script type="text/javascript">
    DA = (function() {
        return {
            save: function() {
                var commitsToIgnore = jQuery("#commitsToIgnore").val();

                new Ajax.Request("/maven-dependencies-plugin.html", {
                    method: 'post',
                    parameters: {action:"saveConfig", commitsToIgnore: commitsToIgnore},
                    onSuccess: function(transport){
                        jQuery("#message-target").html("<div class=\'successMessage\' style=\'display: block\'>Maven Dependencies Settings Saved</div>")
                    },
                    onFailure: function(){
                        jQuery("#message-target").html("<div class=\'successMessage\' style=\'display: block\'>Failed to Save Maven Dependencies Settings</div>")
                    }
                });
            }
        };
    })();
</script>
<div class="serverConfigPage">
    <div id="message-target"></div>
    <table class="runnerFormTable">
        <tr class="groupingTitle">
            <td colspan="2">Maven Dependencies - Version Control Settings</td>
        </tr>

        <tr>
            <th>Commits to Ignore:</th>
            <td>
                <textarea id="commitsToIgnore" rows="8" cols="100">${commitsToIgnore}</textarea>
                <div class="smallNote">Each line is a separate match expression, evaluated on the VCS commit messages. The expressions may contain one or more wildcards '*'.</div>
            </td>
        </tr>

    </table>
    <div class="saveButtonsBlock">
        <input class="submitButton" type="button" value="Save" title="Click to save the maven plugin settings" onclick="DA.save();"/>
    </div>
</div>