<%@ include file="/WEB-INF/head.jspf" %>
<div id="main"><section id="mainContent"><c:forEach items="${articles}" var="art" >
    <article class="entry"><header><h1><t:articleUrl article="${art}" id="article${art.articleid}"/></h1></header>
<c:out escapeXml="false" value="${art.postedtext}"/>
<footer><imead:keyVal key="page_articleFooter"><imead:param><h:time datetime="${art.posted}" pubdate="true"/></imead:param><imead:param object="${art.sectionid.name}"/></imead:keyVal>
<c:if test="${art.comments}"><t:articleUrl article="${art}" anchor="comments" text="${art.commentCount} ${art.commentCount == 1 ? 'comment.' : 'comments.'}"/></c:if>
</footer></article><c:if test="${art.commentCollection != null}"><section id="comments"><c:forEach items="${art.commentCollection}" var="comm" varStatus="status">
    <article class="comment" id="${comm.commentid}"><c:if test="${status.last}"><span id="last"></span></c:if>
<c:out escapeXml="false" value="${comm.postedtext}"/>
<footer><imead:keyVal key="page_commentFooter"><imead:param><h:time datetime="${comm.posted}" pubdate="true"/></imead:param><imead:param object="${fn:trim(comm.postedname)}"/></imead:keyVal></footer></article></c:forEach></section>
<c:choose><c:when test="${art.comments && !spamSuspected}"><form action="<t:articleUrl article="${art}" anchor="last" link="false"/>" id="commentSubmission" class="entryform noPrint" method="post" accept-charset="UTF-8"><fieldset>
<legend><imead:keyVal key="page_message_legend" /></legend>
<imead:keyValVar key="page_message_name" var="page_message_name" /><h:textbox id="name" label="${page_message_name}" size="30" maxLength="30" /><br/>
<imead:keyValVar key="page_message_text" var="page_message_text" /><h:textarea id="text" label="${page_message_text}" styleClass="comment" length="60" height="12" /><br/>
<input type="submit" value="Leave Message"/>
<h:requestToken/><h:hidden id="submit-type" value="comment" />
</fieldset></form><br/></c:when>
<c:when test="${art.comments && spamSuspected}"><imead:keyVal key="page_formReplacement"/></c:when></c:choose>
</c:if></c:forEach>
<%@ include file="/WEB-INF/pagenation.jspf" %>
</section>
<%@ include file="/WEB-INF/side.jspf" %>
</div>
<%@ include file="/WEB-INF/foot.jspf" %>
