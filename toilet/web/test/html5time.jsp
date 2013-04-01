<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<jsp:useBean id="now" class="java.util.Date" />
<html><head><title>JSTL HTML5 Time Test</title></head>
    <body>The time is now
        <time datetime="<fmt:formatDate value="${now}" pattern="yyyy-MM-dd'T'HH:mm:ss'Z'"/>">
            <fmt:formatDate value="${now}" pattern="h:mm a z 'on' EEEE, MMMM d, yyyy" />.
        </time>
</body></html>
