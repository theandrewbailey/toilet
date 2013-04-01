<%@ include file="/WEB-INF/head.jspf" %>
<div class="adminform">
    user agents (browsers):
    <table class="secondmin">
        <c:forEach items="${agent}" var="a"><tr><td><c:out value="${a.key}"/></td><td>${a.value} (<fmt:formatNumber type="percent" maxIntegerDigits="3" value="${a.value/sessions}" />)</td></tr>
    </c:forEach></table>
    <br/>Operating Systems:
    <table class="secondmin">
        <c:forEach items="${os}" var="a"><tr><td><c:out value="${a.key}"/></td><td>${a.value} (<fmt:formatNumber type="percent" maxIntegerDigits="3" value="${a.value/sessions}" />)</td></tr>
    </c:forEach></table>
    <br/>IP addresses:
    <table class="secondmin">
        <c:forEach items="${ip}" var="a"><tr><td>${a.key}</td><td>${a.value} (<fmt:formatNumber type="percent" maxIntegerDigits="3" value="${a.value/sessions}" />)</td></tr>
    </c:forEach></table>
    <br/>References:
    <table class="secondmin">
        <c:forEach items="${referrer}" var="a"><tr><td><c:out value="${a.key}"/></td><td>${a.value} (<fmt:formatNumber type="percent" maxIntegerDigits="3" value="${a.value/sessions}" />)</td></tr>
    </c:forEach></table>
    <br/>Hits:
    <table class="secondmin">
        <c:forEach items="${visits}" var="a"><tr><td><c:out value="${a.key}"/></td><td>${a.value}</td></tr>
    </c:forEach></table>
    <form action="admin" method="POST"><p><input type="submit" value="reanal" name="re-anal"/><input type="hidden" name="answer" value="${answer}"/></p><h:requestToken/></form>
</div>
<%@ include file="/WEB-INF/manFoot.jspf" %>
