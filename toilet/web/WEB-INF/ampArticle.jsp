<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="uri:libwebsitetools:htmlTools" %>
<%@ taglib prefix="t" uri="uri:toilet" %><!DOCTYPE html>
<html amp lang="${$_LIBIMEAD_PRIMARY_LOCALE.toLanguageTag()}"><head>
    <meta charset="utf-8"/>
    <script async src="https://cdn.ampproject.org/v0.js"></script>
    <meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1,user-scalable=yes">
    <h:localVar key="page_title"/><h:title siteTitle="${page_title}" pageTitle="${title}" siteTitleHide="${siteTitleHide}" />
    <style amp-boilerplate>body{-webkit-animation:-amp-start 8s steps(1,end) 0s 1 normal both;-moz-animation:-amp-start 8s steps(1,end) 0s 1 normal both;-ms-animation:-amp-start 8s steps(1,end) 0s 1 normal both;animation:-amp-start 8s steps(1,end) 0s 1 normal both}@-webkit-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-moz-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-ms-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-o-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}</style><noscript><style amp-boilerplate>body{-webkit-animation:none;-moz-animation:none;-ms-animation:none;animation:none}</style></noscript>
    <style amp-custom>${css}</style>
    <h:meta showCss="false"/>
</head><body>
<header><h1><a title="Home" href="<h:local key="security_baseURL" locale=""/>"><h:local key="page_title"/></a></h1></header>
<amp-pixel src="${canonical}" layout="nodisplay"></amp-pixel>
<article class="article${Article.articleid}"><header><h1 id="article${Article.articleid}">${Article.articletitle}</h1>
<a class="ampMessage" href="<t:articleUrl article='${Article}' link='false'/>"><h:local key="page_amp"/></a>
</header>
<c:out escapeXml="false" value="${Article.postedamp}"/>
    <footer><h:local key="page_articleFooter"><h:param><h:time datetime="${Article.posted}" pubdate="true"/></h:param>
        <h:param><t:categorizer category="${Article.sectionid.name}"><a href="${_cate_url}">${_cate_group}</a></t:categorizer></h:param></h:local>
    <c:if test="${Article.comments || fn:length(art.commentCollection) > 0}">${" "}<t:articleUrl article="${Article}" anchor="comments" cssClass="noPrint" text="${fn:length(Article.commentCollection)} ${fn:length(Article.commentCollection) == 1 ? ' complaint.' : ' complaints.'}"/></c:if>
    </footer>
<a class="ampMessage" href="<t:articleUrl article='${Article}' link='false'/>"><h:local key="page_amp"/></a>
</article>
<aside class="noPrint">
    <div class="side_top"><h:local key="page_sideTop"/></div>
    <nav><h1><h:local key="page_topics" /></h1><ul><t:categorizer>
        <li><c:choose><c:when test="${_cate_group == curGroup}">${_cate_group}</c:when><c:otherwise><a href="${_cate_url}">${_cate_group}</a></c:otherwise></c:choose></li></t:categorizer>
    </ul></nav>
    <%@ include file="/WEB-INF/recent.jspf" %>
    <div class="side_bottom"><h:local key="page_sideBottom"/></div>
</aside>
<footer id="downContent" class="noPrint">
<p><h:responseTag><h:local key="page_footerFormat">
    <h:param><h:time datetime="${requestTime}"/></h:param>
    <h:param object="${renderMillis}"/>
</h:local></h:responseTag></p>
</footer></body></html>
