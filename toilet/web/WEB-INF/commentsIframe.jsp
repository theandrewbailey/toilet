<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="uri:libwebsitetools:htmlTools" %>
<%@ taglib prefix="t" uri="uri:toilet" %>
<%@ taglib prefix="imead" uri="uri:libwebsitetools:IMEAD" %><!DOCTYPE html>
<html lang="en-US" class="reset"><head>
    <meta charset="UTF-8"/>
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1,user-scalable=yes">
    <base href="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>"/>
    <h:meta/>
    <imead:keyValVar key="site_title" /><h:title siteTitle="${site_title}" pageTitle="${title}" siteTitleHide="${siteTitleHide}" />
    <link rel="shortcut icon" href="<imead:keyVal key="site_favicon"/>"/>
    <h:css/>
</head><body>
<footer class="articleFooter reset"><imead:local key="page_articleFooter"><imead:param><h:time datetime="${art.posted}" pubdate="true"/></imead:param>
    <imead:param><t:categorizer category="${art.sectionid.name}"><a href="${_cate_url}" target="_parent">${_cate_group}</a></t:categorizer></imead:param></imead:local>
    <c:if test="${art.comments || fn:length(art.commentCollection) > 0}">${fn:length(art.commentCollection)} ${fn:length(art.commentCollection) == 1 ? ' comment.' : ' comments.'}</c:if>
</footer>
<div class="reset">
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
</body></html>
