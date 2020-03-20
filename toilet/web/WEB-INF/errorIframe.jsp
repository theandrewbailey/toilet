<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="uri:libwebsitetools:htmlTools" %>
<%@ taglib prefix="t" uri="uri:toilet" %><!DOCTYPE html>
<html lang="en-US" class="reset"><head>
    <meta charset="UTF-8"/>
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,minimum-scale=1,initial-scale=1,user-scalable=yes">
    <base href="<h:local key="security_baseURL" locale=""/>"/>
    <h:meta/>
    <h:localVar key="page_title"/><h:title siteTitle="${site_title}" pageTitle="${title}" siteTitleHide="${siteTitleHide}" />
    <link rel="shortcut icon" href="<h:local key="page_favicon" locale=""/>"/>
    <c:if test="${null!=asyncFiles && asyncFiles.get()}"></c:if>
    <h:css/>
</head><body class="reset">
<p class="error">${ERROR_MESSAGE}</p>
<!--<h:responseTag><h:local key="page_footFormat">
    <h:param><h:time datetime="${requestTime}"/></h:param>
    <h:param object="${renderMillis}"/>
</h:local></h:responseTag>-->
<h:javascript/></body></html>
