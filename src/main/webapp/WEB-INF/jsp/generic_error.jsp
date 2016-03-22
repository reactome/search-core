<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:import url="header.jsp"/>
<div class="ebi-content">
    <div class="grid_23 padding">
        <c:import url="contact_form.jsp">
            <c:param name="source" value="E"/>
        </c:import>
    </div>
</div>
<div class="clear"></div>

</div>            <%--A weird thing to avoid problems--%>
<c:import url="footer.jsp"/>