<%@ include file="/WEB-INF/head.jspf" %>
<div><main role="main" id="leftContent"><c:forEach items="${articles}" var="art" >
<%@ include file="/WEB-INF/article.jspf" %>
</c:forEach>
<%@ include file="/WEB-INF/pagenation.jspf" %>
</main>
<%@ include file="/WEB-INF/side.jspf" %>
</div>
<%@ include file="/WEB-INF/foot.jspf" %>
