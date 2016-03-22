<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<ul class="tree">
<c:forEach var="node" items="${node.children}">
    <li>
        <c:choose>
            <c:when test="${not empty node.url}">
                <img src="../resources/images/${node.type}.png" title="${node.type}" width="12" height="11" />
                <a href="${node.url}" class=""  title="Show Details" rel="nofollow">${node.name} <c:if test="${not empty node.species}">(${node.species})</c:if></a>
            </c:when>
            <c:otherwise>
                ${node.name}(${node.species})
            </c:otherwise>
        </c:choose>
    <c:set var="node" value="${node}" scope="request"/>
    <c:import url="/WEB-INF/jsp/node.jsp"/>
    </li>
</c:forEach>
</ul>
