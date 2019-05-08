<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="uri:libwebsitetools:htmlTools" %>
<%@ taglib prefix="t" uri="uri:toilet" %><!DOCTYPE html>
<html amp lang="en-US"><head>
    <meta charset="utf-8"/>
    <script async src="https://cdn.ampproject.org/v0.js"></script>
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1,user-scalable=yes">
    <h:meta/>
    <h:localVar key="site_title"/><h:title siteTitle="${site_title}" pageTitle="${title}" siteTitleHide="${siteTitleHide}" />
    <link rel="shortcut icon" href="<h:local key="site_favicon" locale=""/>"/>
    <style amp-boilerplate>body{-webkit-animation:-amp-start 8s steps(1,end) 0s 1 normal both;-moz-animation:-amp-start 8s steps(1,end) 0s 1 normal both;-ms-animation:-amp-start 8s steps(1,end) 0s 1 normal both;animation:-amp-start 8s steps(1,end) 0s 1 normal both}@-webkit-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-moz-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-ms-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-o-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}</style><noscript><style amp-boilerplate>body{-webkit-animation:none;-moz-animation:none;-ms-animation:none;animation:none}</style></noscript>
    <style amp-custom>${css}</style>
    <link rel="alternate" href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>rss/Articles.rss" title="Articles" type="application/rss+xml"/>
    <link rel="alternate" href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>rss/Comments.rss" title="Comments" type="application/rss+xml"/>
    <link rel="alternate" href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>rss/Spruce.rss" title="Spruce" type="application/rss+xml"/>
</head><body>
<header><h1><a title="Home" href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>"><h:local key="site_title"/></a></h1></header>
<amp-pixel src="${canonical}" layout="nodisplay"></amp-pixel>
<article class="article${art.articleid}"><header><h1 id="article${art.articleid}">${art.articletitle}</h1>
<a class="ampMessage" href="<t:articleUrl article='${art}' link='false'/>"><h:local key="page_amp"/></a>
</header>
<c:out escapeXml="false" value="${art.postedamp}"/>
    <footer><h:local key="page_articleFooter"><h:param><h:time datetime="${art.posted}" pubdate="true"/></h:param>
        <h:param><t:categorizer category="${art.sectionid.name}"><a href="${_cate_url}">${_cate_group}</a></t:categorizer></h:param></h:local>
    <c:if test="${art.comments || fn:length(art.commentCollection) > 0}"><t:articleUrl article="${art}" anchor="comments" cssClass="noPrint" text="${fn:length(art.commentCollection)} ${fn:length(art.commentCollection) == 1 ? 'comment.' : 'comments.'}"/></c:if>
    </footer>
<a class="ampMessage" href="<t:articleUrl article='${art}' link='false'/>"><h:local key="page_amp"/></a>
</article>

<aside class="noPrint">
    <div class="side_top"><h:local key="page_side_top"/></div>
    <nav><h1><h:local key="page_topics" /></h1><ul><t:categorizer>
        <li><c:choose><c:when test="${_cate_group == curGroup}">${_cate_group}</c:when><c:otherwise><a href="${_cate_url}">${_cate_group}</a></c:otherwise></c:choose></li></t:categorizer>
    </ul></nav>
    <%@ include file="/WEB-INF/recent.jspf" %>
    <div class="side_bottom"><h:local key="page_side_bottom"/></div>
</aside>

<footer id="downContent" class="noPrint">
<p><h:responseTag><h:local key="page_footFormat">
    <h:param><h:time datetime="${requestTime}"/></h:param>
    <h:param object="${renderMillis}"/>
</h:local></h:responseTag></p>
</footer></body></html>
