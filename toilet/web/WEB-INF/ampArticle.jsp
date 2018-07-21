<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="uri:libwebsitetools:htmlTools" %>
<%@ taglib prefix="t" uri="uri:toilet" %>
<%@ taglib prefix="imead" uri="uri:libwebsitetools:IMEAD" %>
<%@ taglib prefix="ody" uri="uri:libwebsitetools:Odyssey" %><!DOCTYPE html>
<html amp lang="en-US"><head>
    <meta charset="utf-8"/>
    <script async src="https://cdn.ampproject.org/v0.js"></script>
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1,user-scalable=yes">
    <h:meta/>
    <imead:keyValVar key="site_title" /><h:title siteTitle="${site_title}" pageTitle="${title}" siteTitleHide="${siteTitleHide}" />
    <link rel="shortcut icon" href="<imead:keyVal key="site_favicon"/>"/>
    <link rel="alternate" href="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>rss/Articles.rss" title="Articles" type="application/rss+xml"/>
    <link rel="alternate" href="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>rss/Comments.rss" title="Comments" type="application/rss+xml"/>
    <link rel="alternate" href="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>rss/Spruce.rss" title="Spruce" type="application/rss+xml"/>
    <style amp-boilerplate>body{-webkit-animation:-amp-start 8s steps(1,end) 0s 1 normal both;-moz-animation:-amp-start 8s steps(1,end) 0s 1 normal both;-ms-animation:-amp-start 8s steps(1,end) 0s 1 normal both;animation:-amp-start 8s steps(1,end) 0s 1 normal both}@-webkit-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-moz-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-ms-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@-o-keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}@keyframes -amp-start{from{visibility:hidden}to{visibility:visible}}</style><noscript><style amp-boilerplate>body{-webkit-animation:none;-moz-animation:none;-ms-animation:none;animation:none}</style></noscript>
    <style amp-custom>${css}</style>
</head><body>
<header><h1><a title="Home" href="<imead:keyVal key="libOdyssey_guard_canonicalURL"/>"><imead:keyVal key="site_title"/></a></h1></header>
<amp-pixel src="${canonical}" layout="nodisplay"></amp-pixel>
<article class="article${art.articleid}"><header><h1 id="article${art.articleid}">${art.articletitle}</h1></header>
<c:out escapeXml="false" value="${art.postedamp}"/>
    <footer><imead:local key="page_articleFooter"><imead:param><h:time datetime="${art.posted}" pubdate="true"/></imead:param>
        <imead:param><t:categorizer category="${art.sectionid.name}"><a href="${_cate_url}">${_cate_group}</a></t:categorizer></imead:param></imead:local>
    <c:if test="${art.comments || fn:length(art.commentCollection) > 0}"><t:articleUrl article="${art}" anchor="comments" cssClass="noPrint" text="${fn:length(art.commentCollection)} ${fn:length(art.commentCollection) == 1 ? 'comment.' : 'comments.'}"/></c:if>
    </footer>
<p class="ampMessage"><a href="<t:articleUrl article='${art}' link='false'/>"><imead:local key="page_amp"/></a></p>
</article>

<aside class="noPrint">
    <div class="side_top"><imead:local key="page_side_top"/></div>
    <nav><h1><imead:local key="page_topics" /></h1><ul><t:categorizer>
        <li><c:choose><c:when test="${_cate_group == curGroup}">${_cate_group}</c:when><c:otherwise><a href="${_cate_url}">${_cate_group}</a></c:otherwise></c:choose></li></t:categorizer>
    </ul></nav>

    <c:if test="${articleCategory ne null}"><nav id="recentCategoryEntries"><h1><imead:local key="page_sideRecentCategory"><imead:param object="${articleCategory}"/></imead:local></h1><ul><t:recentArticles number="10" category="${articleCategory}">
        <li><t:articleUrl article="${_recentEntry}"/></li></t:recentArticles>
    </ul></nav></c:if>
    <nav id="recentEntries"><h1><imead:local key="page_sideRecent"/></h1><ul><t:recentArticles number="10">
        <li><t:articleUrl article="${_recentEntry}"/></li></t:recentArticles>
    </ul></nav>
    <div class="side_bottom"><imead:local key="page_side_bottom"/></div>
</aside>

<footer id="downContent" class="noPrint">
<p><ody:responseTag><imead:local key="page_footFormat">
    <imead:param><h:time datetime="${requestTime}"/></imead:param>
    <imead:param object="${renderMillis}"/>
</imead:local></ody:responseTag></p>
</footer></body></html>
