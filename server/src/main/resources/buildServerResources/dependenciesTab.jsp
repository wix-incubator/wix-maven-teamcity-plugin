<jsp:useBean id="buildName" scope="request" type="java.lang.String"/>
<jsp:useBean id="dependenciesResult" scope="request" type="com.wixpress.ci.teamcity.dependenciesTab.mavenAnalyzer.DependenciesResult"/>
<%@ include file="/include.jsp" %>
Started running Maven 3 dependencies collection for ${buildName}.<br/>
${dependenciesResult.resultType}

