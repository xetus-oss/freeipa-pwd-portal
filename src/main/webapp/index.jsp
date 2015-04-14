<%@page import="com.xetus.freeipa.pwdportal.PasswordResetRequestCache"%>
<%@page import="com.xetus.freeipa.pwdportal.FreeIPARestService"%>

<%
  String requestId = request.getParameter(FreeIPARestService.RESET_REQUEST_ID_PARAM);
  PasswordResetRequestCache cache = PasswordResetRequestCache.getInstance();
%>

<!doctype html>
<html data-context-path="<%= request.getContextPath() %>" ng-strict-di>
<head>
  <%@ include file="partials/head.jsp"%>
</head>
<body ng-app="pwdPortalApp">

	<div class="row">
		<div class="medium-10 small-centered columns">
			<h3 class="text-center">Welcome to the Free IPA Password Portal</h3>
			<hr />
      <div id="content">
  			 
        <div ng-view></div>

 			</div>
		</div>

	</div>
	
	<div password-reset-directive
	    <% if (requestId != null) { %>
	      request-id="<%= requestId %>"
	      request-expired="<%= requestId != null
	          && cache.getRequest(requestId) == null %>"
      <% } %>></div>

</body>
</html>