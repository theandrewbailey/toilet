<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="uri:libwebsitetools:htmlTools" %>
<%@ taglib prefix="t" uri="uri:toilet" %><!DOCTYPE html>
<html lang="en-US"><head>
    <meta charset="UTF-8"/>
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1,user-scalable=yes">
    <c:if test="${null!=asyncFiles && asyncFiles.get()}"></c:if>
    <h:css/>
    <base href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>"/>
    <h:meta/>
    <h:localVar key="site_title"/><h:title siteTitle="${site_title}" pageTitle="${title}" siteTitleHide="${siteTitleHide}" />
    <link rel="shortcut icon" href="<h:local key="site_favicon" locale=""/>"/>
    <link rel="alternate" href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>rss/Articles.rss" title="Articles" type="application/rss+xml"/>
    <link rel="alternate" href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>rss/Comments.rss" title="Comments" type="application/rss+xml"/>
    <link rel="alternate" href="<h:local key="libOdyssey_guard_canonicalURL" locale=""/>rss/Spruce.rss" title="Spruce" type="application/rss+xml"/>
</head><body>
<c:out escapeXml="false" value="${art.summary}"/>
</body></html>
