<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="uri:libwebsitetools:htmlTools" %>
<%@ taglib prefix="t" uri="uri:toilet" %><!DOCTYPE html>
<html lang="en-US" class="reset"><head>
    <meta charset="UTF-8"/>
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1,user-scalable=yes">
    <base href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>"/>
    <h:meta/>
    <h:localVar key="site_title"/><h:title siteTitle="${site_title}" pageTitle="${title}" siteTitleHide="${siteTitleHide}" />
    <link rel="shortcut icon" href="<h:local key="site_favicon" locale=""/>"/>
    <c:if test="${null!=asyncFiles && asyncFiles.get()}"></c:if>
    <h:css/>
</head><body>
<footer class="articleFooter reset"><h:local key="page_articleFooter"><h:param><h:time datetime="${art.posted}" pubdate="true"/></h:param>
    <h:param><t:categorizer category="${art.sectionid.name}"><a href="${_cate_url}" target="_parent">${_cate_group}</a></t:categorizer></h:param></h:local>
    <c:if test="${art.comments || fn:length(art.commentCollection) > 0}">${fn:length(art.commentCollection)} ${fn:length(art.commentCollection) == 1 ? ' comment.' : ' comments.'}</c:if>
</footer>
<div class="reset">
<section id="comments"><c:forEach items="${art.commentCollection}" var="comm" varStatus="status"><c:if test="${status.last}"><span id="last"></span></c:if>
    <article class="comment" id="${comm.commentid}">
<c:out escapeXml="false" value="${comm.postedhtml}"/>
<footer><h:local key="page_commentFooter"><h:param><h:time datetime="${comm.posted}" pubdate="true"/></h:param><h:param object="${fn:trim(comm.postedname)}"/></h:local></footer></article></c:forEach></section>

<c:choose><c:when test="${art.comments && spamSuspected}"><h:local key="page_formReplacement"/></c:when><c:when test="${art.comments && !spamSuspected}">
<form action="${commentIframe}" id="commentSubmission" class="entryform noPrint" method="post" accept-charset="UTF-8"><fieldset>
    <legend><h:local key="page_message_legend" /></legend><h:localVar key="page_patternMismatch" var="patternMismatch" /><h:localVar key="page_valueMissing" var="valueMissing" />
    <h:localVar key="page_message_name" var="page_message_name" /><h:textbox id="name" label="${page_message_name}" maxLength="30" value="${sessionScope.LastPostedName}" required="true" valueMissing="${valueMissing}" patternMismatch="${patternMismatch}" inputMode="latin-name" autocomplete="name" /><br/>
    <h:localVar key="page_message_text" var="page_message_text" /><h:textarea id="text" label="${page_message_text}" styleClass="comment" height="10" value="${commentText}" required="true" valueMissing="${valueMissing}" patternMismatch="${patternMismatch}" /><br/>
    <input type="submit" value="Leave Message"/>
    <h:hidden id="submit-type" value="comment" />
    <h:hidden id="original-request-time" value="${$_LIBODYSSEY_REQUEST_START_TIME.getTime()}" />
</fieldset></form><br/></c:when>
<c:when test="${!art.comments}"><h:local key="page_commentDisabled" /></c:when></c:choose>
</div>
<!--<h:responseTag><h:local key="page_footFormat">
    <h:param><h:time datetime="${requestTime}"/></h:param>
    <h:param object="${renderMillis}"/>
</h:local></h:responseTag>-->
</body></html>
