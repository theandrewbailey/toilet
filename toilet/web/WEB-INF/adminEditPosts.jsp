<%@ page trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/head.jspf" %>

<form action="adminArticle" method="GET" class="adminform" accept-charset="UTF-8">
    <button type="submit" name="action" value="Add Article"><h:local key="page_articleAdd"/></button>
</form>

<form action="adminDingus" method="POST" class="adminform" accept-charset="UTF-8">
    <button type="submit" name="action" value="Markdown Dingus"><h:local key="page_markdown_dingus"/></button>
</form>

<form action="adminPost" method="POST" class="adminform adminArticle" accept-charset="UTF-8"><c:forEach items="${articles}" var="art">
<article class="adminform">
    <c:choose><c:when test="${null==art.imageurl}"><h:checkbox name="selectedArticle" label="" value="${art.articleid}" styleClass="articleCheckbox"/></c:when>
    <c:otherwise><h:checkbox name="selectedArticle" value="${art.articleid}" styleClass="articleCheckbox articleCheckboxWithImage"/></c:otherwise></c:choose>
    <button type="submit" formmethod="GET" formaction="edit/${art.articleid}"><h:local key="page_edit"/></button>&nbsp;
    <t:articleUrl article="${art}" cssClass="nocache"/>&nbsp;<c:if test="${art.sectionid.name != ' '}">under ${art.sectionid.name},&nbsp;</c:if>
    <h:time datetime="${art.posted}" pattern="EEE MM/dd/yy h:mm a"/>&nbsp;
    <c:if test="${0!=fn:length(art.commentCollection)}"><p class="secondmin" >
    <c:forEach items="${art.commentCollection}" var="comm"><h:time datetime="${comm.posted}" pattern="EEE MM/dd/yy h:mm a"/> by ${comm.postedname} <button type="submit" name="deletecomment" value="${comm.commentid}"><h:local key="page_delete"/></button><br/>
    </c:forEach></p></c:if>
</article></c:forEach>
<button name="selectAll" data-check="articleCheckbox"><h:local key="page_select_all"/></button>
<button name="selectImages" data-check="articleCheckboxWithImage"><h:local key="page_select_all_images"/></button>
<button type="submit" name="rewrite" value="rewrite"><h:local key="page_rewrite"/></button>
<button type="submit" name="disablecomments" value="disablecomments"><h:local key="page_comment_disable"/></button>
</form>
<%@ include file="/WEB-INF/adminFoot.jspf" %>
