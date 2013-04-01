<%@ include file="/WEB-INF/head.jspf" %>
<div class="error"><div class="adminform">
        <form action="admin" method="POST"><p><br/><input type="submit" value="reanal" name="re-anal"/><input type="hidden" name="answer" value="${answer}"/></p><h:requestToken/></form>
    <c:forEach items="${days}" var="d"><c:url var="x" value="admin"><c:param name="day" value="${d[0]}"/><c:param name="month" value="${d[1]}"/><c:param name="year" value="${d[2]}"/></c:url><a href="<c:out value="${x}"/>" class="secondmin">${d[1]}/${d[0]}/${d[2]}</a><br/></c:forEach>
</div></div>
<%@ include file="/WEB-INF/manFoot.jspf" %>
