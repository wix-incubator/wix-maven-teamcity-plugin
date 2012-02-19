<jsp:useBean id="module" scope="request" type="java.lang.String"/>
<jsp:useBean id="fullTrace" scope="request" type="java.lang.String"/>
<jsp:useBean id="resultType" scope="request" type="java.lang.String"/>
<%@ include file="/include.jsp" %>
<c:if test="${resultType == 'dependencies'}">
Module Dependencies: <br/>
${module}
</c:if>
<c:if test="${resultType == 'error'}">
Error getting dependencies: <br/>
${fullTrace}
</c:if>
<c:if test="${resultType == 'notRun'}">
Getting dependencies was not run: <br/>
</c:if>
<c:if test="${resultType == 'running'}">
Getting dependencies is running: <br/>
</c:if>

