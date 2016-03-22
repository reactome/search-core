<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="mytag" uri="/WEB-INF/tags/customTag.tld"%>

<c:import url="header.jsp"/>
<div class="ebi-content" >

  <div class="grid_23 padding">
    <h3>${entry.name}</h3>
  </div>

  <div class="grid_23  padding">
      <!-- INTERACTORS TABLE -->
      <c:if test="${not empty entry.interactions}">
        <div class="grid_23  padding">
          <h5>Interactions for <a href="${entry.url}" class="" title="Show ${entry.accession}" rel="nofollow">${entry.accession}</a></h5>
          <div class="wrap">
            <table class="fixedTable">
              <thead>
              <tr class="tableHead">
                <td style="width: 6%">Confidence Score</td>
                <td style="width: 6%">Interactor Accession</td>
                <td>Reactome Entry</td>
                <td style="width: 6%">Evidences</td>
              </tr>
              </thead>
            </table>
            <div class="inner_table_div" style="height: 400px;">
              <table>
                <c:forEach var="interaction" items="${entry.interactions}">
                  <tr>
                    <td  style="width: 6%">${interaction.score}</td>
                    <td style="width: 6%"><a href="${interaction.accessionURL}" class="" title="Show ${interaction.accession}" rel="nofollow">${interaction.accession}</a></td>
                    <td>
                      <c:forEach var="reactomeEntry" items="${interaction.interactorReactomeEntries}">
                        <ul  class="list overflowList">
                          <li>
                            <a href="/content/detail/${reactomeEntry.reactomeId}" class="" title="Show Details" rel="nofollow">${reactomeEntry.reactomeName}<span> (${reactomeEntry.reactomeId})</span></a>
                          </li>
                        </ul>
                      </c:forEach>
                    </td>
                    <td style="width: 6%">
                      <a href="${interaction.evidencesURL}" title="Open evidences" rel="nofollow" target="_blank">${fn:length(interaction.interactionEvidences)}</a>
                    </td>
                  </tr>
                </c:forEach>
              </table>
            </div>
          </div>
        </div>
      </c:if>
  </div>


</div>
<div class="clear"></div>

</div>            <%--A weird thing to avoid problems--%>
<c:import url="footer.jsp"/>