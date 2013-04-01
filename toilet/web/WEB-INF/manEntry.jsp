<%@ include file="/WEB-INF/head.jspf" %>

<form action="adminPost" method="POST" class="adminform"><c:forEach items="${arts}" var="art">
<p class="adminform"><input type="submit" name="e${art.articleid}" value="Edit"/> <t:articleUrl article="${art}"/>&nbsp;<c:if test="${art.sectionid.name != ' '}">under ${art.sectionid.name},&nbsp;</c:if><h:time datetime="${art.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/> <%--<input type="submit" name="a${art.articleid}" value="Delete"/>--%></p>
<p class="secondmin indentMore" ><c:forEach items="${art.commentCollection}" var="comm"><h:time datetime="${comm.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/> by ${comm.postedname} <input type="submit" name="c${comm.commentid}" value="Delete"/><br/>
</c:forEach></p></c:forEach><h:requestToken/></form>
<%@ include file="/WEB-INF/manFoot.jspf" %>
