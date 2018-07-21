<%@ include file="/WEB-INF/head.jspf" %>
<div class="adminform">
    <br/>ETags:
    <table class="secondmin">
    <c:forEach items="${etags}" var="a"><tr><td><c:out value="${a.key}"/></td><td>${a.value}</td></tr>
    </c:forEach></table>
</div>
<%@ include file="/WEB-INF/manFoot.jspf" %>
