<%@ include file="/WEB-INF/head.jspf" %>
<div><main class="indexPage" id="leftContent"><c:forEach items="${articles}" var="art" >
    <article class="article${art.articleid}"><c:out escapeXml="false" value="${art.summary}"/>
</article></c:forEach>
<%@ include file="/WEB-INF/pagenation.jspf" %>
</main>
<%@ include file="/WEB-INF/side.jspf" %>
</div>
<%@ include file="/WEB-INF/foot.jspf" %>
