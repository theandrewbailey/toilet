<%@ include file="/WEB-INF/head.jspf" %>
<c:set var="autofocus" value="true" scope="page"/>
<%@ include file="/WEB-INF/riddleForm.jspf" %>
<br/><footer id="downContent">
<p><h:responseTag><h:local key="page_footFormat">
    <h:param><h:time datetime="${requestTime}"/></h:param>
    <h:param object="${renderMillis}"/>
</h:local></h:responseTag></p>
</footer><h:javascript/></body></html>
