<%@ include file="/WEB-INF/head.jspf" %>
<form class="adminform" action="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>adminLogin" method="post">
    <h:localVar key="page_admin_door" /><h:localVar key="page_valueMissing" var="valueMissing" />
    <h:password id="answer" label="${page_admin_door}" size="40" autofocus="true" valueMissing="${valueMissing}" required="true"/>
</form>
<%@ include file="/WEB-INF/manFoot.jspf" %>
