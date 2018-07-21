<%@ include file="/WEB-INF/head.jspf" %>
<div>
<section id="comments"><c:forEach items="${art.commentCollection}" var="comm" varStatus="status"><c:if test="${status.last}"><span id="last"></span></c:if>
    <article class="comment" id="${comm.commentid}">
<c:out escapeXml="false" value="${comm.postedhtml}"/>
<footer><imead:local key="page_commentFooter"><imead:param><h:time datetime="${comm.posted}" pubdate="true"/></imead:param><imead:param object="${fn:trim(comm.postedname)}"/></imead:local></footer></article></c:forEach></section>

<c:choose><c:when test="${art.comments && spamSuspected}"><imead:local key="page_formReplacement"/></c:when><c:when test="${art.comments && !spamSuspected}">
<form action="${commentIframe}" id="commentSubmission" class="entryform noPrint" method="post" accept-charset="UTF-8"><h:requestToken/><fieldset>
    <legend><imead:local key="page_message_legend" /></legend><imead:localVar key="page_patternMismatch" var="patternMismatch" /><imead:localVar key="page_valueMissing" var="valueMissing" />
    <imead:localVar key="page_message_name" var="page_message_name" /><h:textbox id="name" label="${page_message_name}" maxLength="30" value="${sessionScope.LastPostedName}" required="true" valueMissing="${valueMissing}" patternMismatch="${patternMismatch}" inputMode="latin-name" autocomplete="name" /><br/>
    <imead:localVar key="page_message_text" var="page_message_text" /><h:textarea id="text" label="${page_message_text}" styleClass="comment" height="10" required="true" valueMissing="${valueMissing}" patternMismatch="${patternMismatch}" /><br/>
    <input type="submit" value="Leave Message"/>
    <h:hidden id="submit-type" value="comment" />
</fieldset></form><br/></c:when>
<c:when test="${!art.comments}"><imead:local key="page_commentDisabled" /></c:when></c:choose>
</div>
<%@ include file="/WEB-INF/foot.jspf" %>
