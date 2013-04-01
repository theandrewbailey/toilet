<%@ include file="/WEB-INF/head.jspf" %>
<form action="adminSession" method="POST" class="adminform">
<c:forEach var="sess" items="${sesses}">
<h:time datetime="${sess.atime}" pattern="EEE MM/dd/yy h:mm:ss a" />, from ${sess.ip} <c:if test="${fn:length(sess.commentCollection) == 0}"><input type="submit" name="s${sess.httpsessionid}" value="Delete"/></c:if>
<c:if test="${sess.useragent != null}"><br/>using: <c:out value="${sess.useragent}"/></c:if><c:if test="${sess.referrer != null}"><br/>referrer: <c:out value="${sess.referrer.location}"/></c:if>
<br/><div class="secondmin" >
<c:forEach var="v" items="${sess.pagerequestCollection}"><fmt:formatDate value="${v.pagerequestPK.atime}" pattern="h:mm:ss a" />: ${v.url.location}<br/>
</c:forEach><c:forEach var="c" items="${sess.commentCollection}"><br/>
    <t:articleUrl article="${c.articleid}" anchor="${c.commentid}" text="${c.articleid.articletitle}" /> on <h:time datetime="${c.posted}" pattern="EEE MM/dd/yy h:mm a" /> by ${c.postedname}: <c:out value="${fn:substring(c.postedtext,0,20)}"/></c:forEach>
<br/><br/></div>
</c:forEach>
<input type="hidden" name="day" value="${day}"/><input type="hidden" name="month" value="${month}"/><input type="hidden" name="year" value="${year}"/>
<h:requestToken/>
</form>
<%@ include file="/WEB-INF/manFoot.jspf" %>
