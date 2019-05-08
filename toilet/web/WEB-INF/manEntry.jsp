<%@ include file="/WEB-INF/head.jspf" %>

<form action="adminPost" method="POST" class="adminform"><c:forEach items="${articles}" var="art">
<p class="adminform"><button type="submit" name="editarticle" value="${art.articleid}">Edit</button> <t:articleUrl article="${art}"/>&nbsp;<c:if test="${art.sectionid.name != ' '}">under ${art.sectionid.name},&nbsp;</c:if><h:time datetime="${art.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/> </p>
<p class="secondmin" ><c:forEach items="${art.commentCollection}" var="comm"><h:time datetime="${comm.posted}" pubdate="true" pattern="EEE MM/dd/yy h:mm a"/> by ${comm.postedname} <button type="submit" name="deletecomment" value="${comm.commentid}">Delete</button><br/>
</c:forEach></p></c:forEach></form>
<%@ include file="/WEB-INF/manFoot.jspf" %>
