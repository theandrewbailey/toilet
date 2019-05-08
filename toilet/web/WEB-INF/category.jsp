<%@ include file="/WEB-INF/head.jspf" %>
<div><main class="indexPage" id="leftContent"><c:forEach items="${articles}" var="art" ><c:out escapeXml="false" value="${art.summary}"/></c:forEach>
<%@ include file="/WEB-INF/pagenation.jspf" %>
</main>
<%@ include file="/WEB-INF/side.jspf" %>
</div>
<%@ include file="/WEB-INF/foot.jspf" %>
