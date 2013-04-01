<%@ include file="/WEB-INF/head.jspf" %>
<form class="adminform" action="admin" method="post">
    <imead:keyValVar key="admin_door" />
    <h:password id="answer" label="${admin_door}" size="40"/>
    <h:requestToken/>
</form>
<script type="text/javascript">document.getElementById("answer").focus();</script>
<%@ include file="/WEB-INF/manFoot.jspf" %>
