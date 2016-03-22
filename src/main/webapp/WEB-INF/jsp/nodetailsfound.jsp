<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="mytag" uri="/WEB-INF/tags/customTag.tld" %>

<c:import url="header.jsp"/>
<div class="ebi-content">
    <div class="grid_24">
        <h2>No details found for ${search}</h2>

        <div class="no-results-div" style="margin-bottom: 200px;">
            <p class="alert">Sorry we could not find any entry matching '${search}'</p>
        </div>
    </div>
    <div class="clear"></div>
</div>

</div>            <%--A weird thing to avoid problems--%>
<c:import url="footer.jsp"/>