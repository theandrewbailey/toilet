<%@ include file="/WEB-INF/head.jspf" %>
<form class="adminform" action="admin" method="post"><h:requestToken/>
    <imead:localVar key="page_admin_door" />
    <h:password id="answer" label="${page_admin_door}" size="40" autofocus="true"/>
</form>
<script type="text/javascript">document.querySelector("input").focus();</script>
<%@ include file="/WEB-INF/manFoot.jspf" %>
