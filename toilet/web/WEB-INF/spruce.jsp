<%@ taglib prefix="sp" uri="uri:spruce" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set scope="request" var="title" value="Spruce"/>
<c:set scope="request" var="page.titleAfter" value="true"/><%@ include file="/WEB-INF/head.jspf" %>
<main class="spruceMain">
    <article class="entry">
    <header><h1><imead:keyVal key="page_spruce_header" /></h1></header>
    <div id="spruceQuote">
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    <p><sp:sentence>${spruce_sentence}</sp:sentence></p>
    </div></article>
<hr/><article class="entry">
<imead:keyVal key="entry_spruce" />
</article></main>
<%@ include file="/WEB-INF/foot.jspf" %>
