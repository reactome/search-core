<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="mytag" uri="/WEB-INF/tags/customTag.tld"%>

<c:import url="header.jsp"/>
<div class="ebi-content" >

    <div class="grid_23 padding">
        <h3>
            <c:if test="${not empty entry.exactType}">
                <c:choose>
                    <c:when test="${not empty entry.type}">
                        <img src="../resources/images/${entry.exactType}.png" title="${entry.type}" height="20" />
                    </c:when>
                    <c:otherwise>
                        <img src="../resources/images/${entry.exactType}.png" title="${entry.exactType}" height="20" />
                    </c:otherwise>
                </c:choose>

            </c:if>
            <c:if test="${entry.isDisease}">
                <img src="../resources/images/isDisease.png" title="Disease related entry" height="20" />
            </c:if>
            ${entry.name}
            <c:if test="${not empty entry.stId}">
                <span> (${entry.stId})</span>
            </c:if>
            <c:if test="${not empty entry.species && entry.species != 'Entries without species'}">
                <span>[${entry.species}]</span>
            </c:if>
        </h3>
        <c:if test="${not empty entry.type}">
            <span style="color: #1F419A; padding-left: 6px; font-size: 20px" title="${entry.instanceTypeExplanation}">${entry.type}</span>
        </c:if>
    </div>

    <c:if test="${not empty entry.summations}">
        <div class="grid_23  padding">
            <h5>Summation</h5>
            <div class="paddingleft">
                <c:forEach var="summation" items="${entry.summations}">
                    <p>${summation}</p>
                </c:forEach>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty entry.locationsPathwayBrowser}">
        <div class="grid_23  padding">
            <h5>Locations in the PathwayBrowser</h5>
            <c:if test="${fn:length(entry.availableSpecies) gt 1}">
                <div class="padding">
                    <select name="availableSpecies" id="availableSpeciesSel" style="height: 1.5em;">
                        <c:forEach items="${entry.availableSpecies}" var="species">
                            <option value="${fn:replace(species, ' ', '_')}" ${species == 'Homo_sapiens' ? 'selected' : ''}>${species}</option>
                        </c:forEach>
                    </select>
                </div>
            </c:if>

            <%--Insert $treeContent = $plus.nextAll().eq(1); into reactome.search.service.js --%>

            <div class="paddingleft">
                <c:forEach var="topLvl" items="${entry.locationsPathwayBrowser}">
                    <c:choose>
                        <c:when test="${empty topLvl.children}">
                            <span><img src="../resources/images/${topLvl.type}.png" title="${topLvl.type}" width="12" height="11" /> <a href="${topLvl.url}" class=""   title="goto Reactome Pathway Browser" rel="nofollow">${topLvl.name} (${topLvl.species})</a></span>
                        </c:when>
                        <c:otherwise>
                            <%--
                                The class attribute is used as a jQuery selector. This class is not present in the css.
                                Specially for chemical, it is present in all species, instead of showing a big list we just show Human as the default
                                and let the user select the desired species in a dropdown list.
                             --%>
                            <div class="tplSpe_${fn:replace(topLvl.species, ' ', '_')}" style="display: none">
                            <span class="plus" title="click here to expand or collapse the tree">
                                <img class="image" src="../resources/images/plus.png" title="${entry.exactType}" width="14" height="13" alt=""/>
                            </span>
                                <span style="font-size:14px"><img src="../resources/images/${topLvl.type}.png" title="${topLvl.type}" width="12" height="11" /> <a href="${topLvl.url}" class=""   title="goto Reactome Pathway Browser" rel="nofollow">${topLvl.name} (${topLvl.species})</a></span>
                                <div class="treeContent">
                                    <ul class="tree">
                                        <c:set var="node" value="${topLvl}" scope="request"/>
                                        <li> <c:import url="/WEB-INF/jsp/node.jsp"/></li>
                                    </ul>
                                </div>
                            </div>

                        </c:otherwise>
                    </c:choose>

                </c:forEach>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty entry.referenceEntity.derivedEwas}">
        <div class="grid_23  padding  margin">
            <h5>Other forms of this molecule</h5>

            <div style="height: auto; max-height: 120px; overflow:auto;" class="paddingleft">
                <table border="0" width="100%" style="border: 0px;">
                    <tbody>
                    <tr>
                    <c:forEach var="derivedEwas" items="${entry.referenceEntity.derivedEwas}" varStatus="loop">
                        <c:if test="${not loop.first and loop.index % 3 == 0}">
                            </tr><tr>
                        </c:if>

                         <td class="overme_3c">
                            <a href="../detail/${derivedEwas.stId}" title="Open ${derivedEwas.name} (${derivedEwas.compartment})" rel="nofollow">${derivedEwas.name} (${derivedEwas.compartment})</a>
                         </td>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty entry.referenceEntity || not empty entry.compartments || not empty entry.synonyms || not empty entry.reverseReaction || not empty entry.goBiologicalProcess || not empty entry.goMolecularComponent}">
        <div class="grid_23  padding  margin">
            <h5>Additional Information</h5>
            <table class="fixedTable">
                <thead>
                <tr class="tableHead">
                    <td></td>
                    <td></td>
                </tr>
                </thead>
                <tbody>
                <c:if test="${not empty entry.referenceEntity}">
                    <c:if test="${not empty entry.referenceEntity.referenceName}">
                        <tr>
                            <td><strong>External reference name</strong></td>
                            <td><a href="${entry.referenceEntity.database.url}" class="" title="Show Details" rel="show ${entry.referenceEntity.database.url}"> ${entry.referenceEntity.referenceName}</a></td>
                        </tr>
                    </c:if>
                    <c:if test="${not empty entry.referenceEntity.referenceIdentifier}">
                        <tr>
                            <td><strong>External reference id</strong></td>
                            <td><a href="${entry.referenceEntity.database.url}" class="" title="Show Details" rel="show ${entry.referenceEntity.database.url}"> ${entry.referenceEntity.referenceIdentifier}</a></td>
                        </tr>
                    </c:if>
                    <c:if test="${not empty entry.referenceEntity.referenceSynonyms}">
                        <tr>
                            <td><strong>external Synonyms</strong></td>
                            <td class="block">
                                <c:forEach var="synonym" items="${entry.referenceEntity.referenceSynonyms}" varStatus="loop">${synonym}<c:if test="${!loop.last}">, </c:if></c:forEach>
                            </td>
                        </tr>
                    </c:if>
                </c:if>
                <c:if test="${not empty entry.synonyms}">
                    <tr>
                        <td><strong>Synonyms</strong></td>
                        <td class="block">
                            <c:forEach var="synonym" items="${entry.synonyms}" varStatus="loop">${synonym}<c:if test="${!loop.last}">, </c:if></c:forEach>
                        </td>
                    </tr>
                </c:if>
                <c:if test="${not empty entry.compartments}">
                    <tr>
                        <td><strong>Compartment</strong></td>
                        <td>
                            <c:forEach var="compartment" items="${entry.compartments}" varStatus="loop">
                                <span><a href="${compartment.database.url}" title="show ${compartment.database.name}" rel="nofollow">${compartment.name}</a></span>
                                <c:if test="${!loop.last}">, </c:if>
                            </c:forEach>
                        </td>
                    </tr>
                </c:if>
                <c:if test="${not empty entry.reverseReaction}">
                    <tr>
                        <td><strong>Reverse Reaction</strong></td>
                        <td>
                            <a href="../detail/${entry.reverseReaction.stId}" class="" title="show Reactome ${entry.reverseReaction.stId}" rel="nofollow">${entry.reverseReaction.name}</a>
                        </td>
                    </tr>
                </c:if>
                <c:if test="${not empty entry.referenceEntity}">
                    <c:if test="${not empty entry.referenceEntity.otherIdentifier}">
                        <tr>
                            <td><strong>Other Identifiers</strong></td>
                            <td style="padding: 0px;">
                                <div style="height: auto; max-height: 120px; overflow: auto; padding-top: 1px; padding-left: 2px;">
                                    <table border="0" width="100%" style="border: 0px;">
                                        <tr>
                                            <c:forEach var="otherIdentifier" items="${entry.referenceEntity.otherIdentifier}" varStatus="loop">
                                            <c:if test="${not loop.first and loop.index % 5 == 0}">
                                        </tr><tr>
                                        </c:if>

                                        <td class="overme_5c">
                                                <span title="${otherIdentifier}">&nbsp;${otherIdentifier}</span>
                                        </td>
                                        </c:forEach>
                                    </table>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${not empty entry.referenceEntity.secondaryIdentifier}">
                        <tr>
                            <td><strong>Secondary Identifiers</strong></td>
                            <td>
                                <c:forEach var="secondaryIdentifier" items="${entry.referenceEntity.secondaryIdentifier}" varStatus="loop">${secondaryIdentifier}<c:if test="${!loop.last}">, </c:if>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${not empty entry.referenceEntity.geneNames}">
                        <tr>
                            <td><strong>Gene Names</strong></td>
                            <td>
                                <c:forEach var="geneNames" items="${entry.referenceEntity.geneNames}" varStatus="loop">${geneNames}<c:if test="${!loop.last}">, </c:if>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${not empty entry.referenceEntity.chain}">
                        <tr>
                            <td><strong>Chain</strong></td>
                            <td>
                                <c:forEach var="chain" items="${entry.referenceEntity.chain}" varStatus="loop">${chain}<c:if test="${!loop.last}">, </c:if>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:if>
                </c:if>
                <c:if test="${not empty entry.goMolecularComponent}">
                    <tr>
                        <td><strong>GO Molecular Component</strong></td>
                        <td>
                            <ul class="list overflowList">
                                <c:forEach var="goMolecularComponent" items="${entry.goMolecularComponent}">
                                    <li><a href="${goMolecularComponent.database.url}" class=""  title="show ${goMolecularComponent.database.name}" rel="nofollow">${goMolecularComponent.name}</a>( ${goMolecularComponent.accession})</li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:if>
                <c:if test="${not empty entry.goBiologicalProcess}">
                    <tr>
                        <td><strong>GO Biological Process</strong></td>
                        <td><a href="${entry.goBiologicalProcess.database.url}" class=""  title="go to ${entry.goBiologicalProcess.database.name}" rel="nofollow">${entry.goBiologicalProcess.name} (${entry.goBiologicalProcess.accession})</a></td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.input || not empty entry.output || not empty entry.components || not empty entry.candidates || not empty entry.member || not empty entry.repeatedUnits || not empty entry.entityOnOtherCell}">
        <div class="grid_23  padding  margin">
            <h5>Components of this entry</h5>
            <table class="fixedTable">
                <thead>
                <tr class="tableHead">
                    <td></td>
                    <td></td>
                </tr>
                </thead>
                <tbody>
                <c:if test="${not empty entry.input}">
                <tr>
                    <td><strong>Input entries</strong></td>
                    <td>
                        <ul class="list overflowAuto">
                            <c:forEach var="input" items="${entry.input}">
                                <li><a href="../detail/${input.stId}" class="" title="Show Details" rel="nofollow">${input.name} <c:if test="${not empty input.species}">(${input.species})</c:if></a></li>
                            </c:forEach>
                        </ul>
                    </td>
                </tr>
                </c:if>
                <c:if test="${not empty entry.output}">
                <tr>
                    <td><strong>Output entries</strong></td>
                    <td><ul class="list overflowList">
                        <c:forEach var="output" items="${entry.output}">
                            <li><a href="../detail/${output.stId}" class="" title="Show Details" rel="nofollow">${output.name}<c:if test="${not empty output.species}">(${output.species})</c:if></a></li>
                        </c:forEach>
                    </ul></td>
                </tr>
                </c:if>
                <c:if test="${not empty entry.components}">
                <tr>
                    <td><strong>Components entries</strong></td>
                    <td><ul class="list overflowList">
                        <c:forEach var="component" items="${entry.components}">
                            <li><a href="../detail/${component.stId}" class="" title="show Reactome" rel="nofollow">${component.name} <c:if test="${not empty component.species}">(${component.species})</c:if></a></li>
                        </c:forEach>
                    </ul></td>
                </tr>
                </c:if>
                <c:if test="${not empty entry.candidates}">
                <tr>
                    <td><strong>CandidateSet entries</strong></td>
                    <td><ul  class="list overflowList">
                        <c:forEach var="candidates" items="${entry.candidates}">
                            <li><a href="../detail/${candidates.stId}" class="" title="show Reactome ${candidates.stId}" rel="nofollow">${candidates.name} <c:if test="${not empty candidates.species}">(${candidates.species})</c:if></a></li>
                        </c:forEach>
                    </ul></td>
                </tr>
                </c:if>
                <c:if test="${not empty entry.member}">
                <tr>
                    <td><strong>Member</strong></td>
                    <td><ul class="list overflowList">
                        <c:forEach var="member" items="${entry.member}">
                            <li><a href="../detail/${member.stId}" class="" title="show Reactome ${member.stId}" rel="nofollow">${member.name} <c:if test="${not empty member.species}">(${member.species})</c:if></a></li>
                        </c:forEach>
                    </ul></td>
                </tr>
                </c:if>
                <c:if test="${not empty entry.repeatedUnits}">
                <tr>
                    <td><strong>repeatedUnits</strong></td>
                    <td><ul class="list overflowList">
                        <c:forEach var="repeatedUnit" items="${entry.repeatedUnits}">
                            <li><a href="../detail/${repeatedUnit.stId}" class="" title="show Reactome ${repeatedUnit.stId}" rel="nofollow">${repeatedUnit.name}</a></li>
                        </c:forEach>
                    </ul></td>
                </tr>
                </c:if>
                <c:if test="${not empty entry.entityOnOtherCell}">
                <tr>
                    <td><strong>EntityOnOtherCell</strong></td>
                    <td><ul class="list overflowList">
                        <c:forEach var="entityOnOtherCell" items="${entry.entityOnOtherCell}">
                            <li><a href="../detail/${entityOnOtherCell.stId}" class="" title="show Reactome ${entityOnOtherCell.stId}" rel="nofollow">${entityOnOtherCell.name}</a></li>
                        </c:forEach>
                    </ul></td>
                </tr>
                </c:if>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.referedEntities}">
        <div class="grid_23  padding  margin">
            <h5>This entry is a component of:</h5>
            <table class="fixedTable">
                <thead>
                <tr class="tableHead">
                    <td></td>
                    <td></td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="referrers" items="${entry.referedEntities}">
                    <tr>
                        <td><strong>${referrers.key}</strong></td>
                        <td>
                            <ul class="list overflowList">
                                <c:forEach var="entityReferenceList" items="${referrers.value}">
                                    <li><c:if test="${not empty entityReferenceList.name}"><a href="../detail/${entityReferenceList.stId}" class="" title="Show Details" rel="nofollow">${entityReferenceList.name}</a></c:if></li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.catalystActivities}">
        <div class="grid_23  padding  margin">
            <h5>Catalyst Activity</h5>
            <table>
                <thead>
                <tr class="tableHead">
                    <td>PhysicalEntity</td>
                    <td>Activity</td>
                    <td>Active Units</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="catalystActivity" items="${entry.catalystActivities}">
                    <tr>
                        <c:if test="${not empty catalystActivity.physicalEntity}">
                            <td><a href="../detail/${catalystActivity.physicalEntity.stId}" class="" title="show Reactome ${catalystActivity.physicalEntity.stId}" rel="nofollow">${catalystActivity.physicalEntity.name}</a></td>
                        </c:if>
                        <c:if test="${not empty catalystActivity.activity}">
                            <td><a href="${catalystActivity.activity.database.url}" class=""  title="show ${catalystActivity.activity.database.name}" rel="nofollow">${catalystActivity.activity.name} (${catalystActivity.activity.accession})</a></td>
                        </c:if>

                        <c:choose>
                            <c:when test="${not empty catalystActivity.activeUnit}">
                                <td>
                                    <ul class="list overflowList">
                                        <c:forEach var="activeUnit" items="${catalystActivity.activeUnit}">
                                            <li><a href="../detail/${activeUnit.stId}" class="" title="show Reactome ${activeUnit.stId}" rel="nofollow">${activeUnit.name}</a></li>
                                        </c:forEach>
                                    </ul>
                                </td>
                            </c:when>
                            <c:otherwise>
                                <td>&nbsp;</td>
                            </c:otherwise>
                        </c:choose>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.referenceEntity}">

        <c:if test="${not empty entry.referenceEntity.referenceGenes}">
            <div class="grid_23  padding  margin">
                <h5>Reference Genes</h5>
                <table class="fixedTable">
                    <thead>
                    <tr class="tableHead">
                        <td>Database</td>
                        <td>Identifier</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="referenceGenes" items="${entry.referenceEntity.referenceGenes}">
                        <tr>
                            <td><strong>${referenceGenes.key}</strong></td>
                            <td>
                                <c:forEach var="value" items="${referenceGenes.value}" varStatus="loop">
                                    <a href="${value.database.url}" title="show ${value.database.name}" rel="nofollow">${value.identifier}</a><c:if test="${!loop.last}">, </c:if>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>

        <c:if test="${not empty entry.referenceEntity.referenceTranscript}">
            <div class="grid_23  padding  margin">
                <h5>Reference Transcripts</h5>
                <table  class="fixedTable">
                    <thead>
                    <tr class="tableHead">
                        <td>Database</td>
                        <td>Identifier</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="referenceTranscript" items="${entry.referenceEntity.referenceTranscript}">
                        <tr>
                            <td><strong>${referenceTranscript.key}</strong></td>
                            <td>
                                <c:forEach var="value" items="${referenceTranscript.value}" varStatus="loop">
                                    <a href="${value.database.url}" title="show ${value.database.name}" rel="nofollow">${value.identifier}</a><c:if test="${!loop.last}">, </c:if>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>
    </c:if>

    <c:if test="${not empty entry.crossReferences}">
        <div class="grid_23  padding  margin">
            <h5>Cross References</h5>
            <table class="fixedTable">
                <thead>
                <tr class="tableHead">
                    <td>Database</td>
                    <td>Identifier</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="crossReference" items="${entry.crossReferences}">
                    <tr>
                        <td><strong>${crossReference.key}</strong></td>
                        <td>
                            <c:forEach var="value" items="${crossReference.value}" varStatus="loop">
                                <a href="${value.database.url}" title="show ${value.database.name}" rel="nofollow">${value.identifier}</a><c:if test="${!loop.last}">, </c:if>
                            </c:forEach>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.diseases}">

        <div class="grid_23  padding  margin">
            <h5>Diseases</h5>
            <table>
                <thead>
                <tr class="tableHead">
                    <td>Name</td>
                    <td>Identifier</td>
                    <td>Synonyms</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="diseases" items="${entry.diseases}">
                    <c:if test="${not empty diseases.name}">
                        <tr>
                            <td><a href="${diseases.database.url}" class=""  title="Show Details" rel="nofollow">${diseases.name} </a></td>
                            <td><c:if test="${not empty diseases.identifier}">${diseases.identifier}</c:if></td>
                            <td><c:if test="${not empty diseases.synonyms}">${diseases.synonyms}</c:if></td>
                        </tr>
                    </c:if>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.regulatedEvents}">
        <div class="grid_23  padding  margin">
            <h5>This entry is regulated by</h5>
            <table class="fixedTable">
                <thead>
                <tr class="tableHead">
                    <td>Regulation type</td>
                    <td>Name</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="regulation" items="${entry.regulatedEvents}">
                    <tr>
                        <td><strong>${regulation.key}</strong></td>
                        <td>
                            <ul class="list overflowList">
                                <c:forEach var="value" items="${regulation.value}" varStatus="loop">
                                    <li><c:if test="${not empty value.regulator.stId}"><a href="../detail/${value.regulator.stId}" class="" title="Show Details" rel="nofollow">${value.regulator.name}<c:if test="${not empty value.regulator.species}"> (${value.regulator.species})</c:if></a></c:if></li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.regulatingEntities}">
        <div class="grid_23  padding  margin">
            <h5>This entity regulates</h5>
            <table class="fixedTable">
                <thead>
                <tr class="tableHead">
                    <td>Regulation type</td>
                    <td>Name</td>

                </tr>
                </thead>
                <tbody>
                <c:forEach var="regulation" items="${entry.regulatingEntities}">
                    <tr>
                        <td><strong>${regulation.key}</strong></td>
                        <td>
                            <ul class="list overflowList">
                                <c:forEach var="value" items="${regulation.value}" varStatus="loop">
                                    <li><c:if test="${not empty value.regulatedEntity.stId}"><a href="../detail/${value.regulatedEntity.stId}" class="" title="Show Details" rel="nofollow">${value.regulatedEntity.name}<c:if test="${not empty value.regulatedEntity.species}"> (${value.regulatedEntity  .species})</c:if></a></c:if></li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>

            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.regulation}">
        <c:set var="regulation" value="${entry.regulation}"/>
        <div class="grid_23  padding  margin">
            <h5>Regulation participants</h5>
            <table>
                <thead>
                <tr class="tableHead">
                    <td></td>
                    <td></td>
                </tr>
                </thead>
                <tbody>
                <c:if test="${not empty regulation.regulationType}">
                    <tr>
                        <td><strong>regulation type</strong></td>
                        <td>${regulation.regulationType}</td>
                    </tr>
                </c:if>
                <c:if test="${not empty regulation.regulatedEntity}">
                    <tr>
                        <td><strong>Regulated entity</strong></td>
                        <td><a href="../detail/${regulation.regulatedEntity.stId}" class="" title="Show Details" rel="nofollow">${regulation.regulatedEntity.name}</a></td>
                    </tr>
                </c:if>
                <c:if test="${not empty regulation.regulator}">
                    <tr>
                        <td><strong>Regulator</strong></td>
                        <td><a href="../detail/${regulation.regulator.stId}" class="" title="Show Details" rel="nofollow">${regulation.regulator.name}</a></td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.modifiedResidues}">
        <div class="grid_23  padding  margin">
            <h5>ModifiedResidues</h5>
            <div class="paddingleft">
                <table>
                    <thead>
                    <tr class="tableHead">
                        <td>Name</td>
                        <td>Coordinate</td>
                        <td>Modification</td>
                        <td>PsiMod Name</td>
                        <td>PsiMod Identifier</td>
                        <td>PsiMod Definition</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="modifiedResidue" items="${entry.modifiedResidues}">
                    <tr>
                        <td><c:if test="${not empty modifiedResidue.name}">${modifiedResidue.name}</c:if></td>
                        <td><c:if test="${not empty modifiedResidue.coordinate}">${modifiedResidue.coordinate}</c:if></td>
                        <td><c:if test="${not empty modifiedResidue.modification.name}"><a href="../detail/${modifiedResidue.modification.stId}" class="" title="Show Details" rel="nofollow">${modifiedResidue.modification.name}</a></c:if></td>
                        <td><c:if test="${not empty modifiedResidue.psiMod.name}"><a href="${modifiedResidue.psiMod.database.url}" class=""  title="Show Details" rel="nofollow">${modifiedResidue.psiMod.name} </a></c:if></td>
                        <td><c:if test="${not empty modifiedResidue.psiMod.identifier}">${modifiedResidue.psiMod.identifier}</c:if></td>
                        <td><c:if test="${not empty modifiedResidue.psiMod.definition}">${modifiedResidue.psiMod.definition}</c:if></td>
                    </tr>
                    </c:forEach>
                </table>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty entry.literature}">
        <div class="grid_23  padding  margin">
            <h5>Literature References</h5>
            <table>
                <thead>
                <tr class="tableHead">
                    <td>pubMedId</td>
                    <td>Title</td>
                    <td>Journal</td>
                    <td>Year</td>
                </tr>
                </thead>
                <tbody class="tableBody">
                <c:forEach var="literature" items="${entry.literature}">
                    <tr>
                        <td><c:if test="${not empty literature.pubMedIdentifier}">${literature.pubMedIdentifier}</c:if></td>
                        <td><c:if test="${not empty literature.title}"><a href="${literature.url}" class=""  title="show Pubmed" rel="nofollow"> ${literature.title}</a></c:if></td>
                        <td><c:if test="${not empty literature.journal}">${literature.journal}</c:if></td>
                        <td><c:if test="${not empty literature.year}">${literature.year}</c:if></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${not empty entry.inferredFrom || not empty entry.inferredTo || not empty entry.orthologousEvents}">
        <div class="grid_23  padding  margin">
            <h5>Inferred Entries</h5>
            <table  class="fixedTable">
                <thead>
                <tr class="tableHead">
                    <td></td>
                    <td></td>
                </tr>
                </thead>
                <tbody>
                <c:if test="${not empty entry.inferredFrom}">
                    <tr>
                        <td><strong>Inferred From</strong></td>
                        <td>
                            <ul class="list overflowList">
                                <c:forEach var="inferredFrom" items="${entry.inferredFrom}">
                                    <li><a href="../detail/${inferredFrom.stId}" class="" title="Show Details" rel="nofollow">${inferredFrom.name} (${inferredFrom.species})</a></li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:if>
                <c:if test="${not empty entry.inferredTo}">
                    <tr>
                        <td><strong>Inferred to</strong></td>
                        <td>
                            <ul class="list overflowList">
                                <c:forEach var="inferredTo" items="${entry.inferredTo}">
                                    <li><a href="../detail/${inferredTo.stId}" class="" title="Show Details" rel="nofollow">${inferredTo.name} (${inferredTo.species})</a></li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:if>
                <c:if test="${not empty entry.orthologousEvents}">
                    <tr>
                        <td><strong>Orthologous events</strong></td>
                        <td>
                            <ul class="list overflowList">
                                <c:forEach var="orthologousEvents" items="${entry.orthologousEvents}">
                                    <li><a href="../detail/${orthologousEvents.stId}" class="" title="Show Details" rel="nofollow">${orthologousEvents.name} (${orthologousEvents.species})</a></li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </c:if>


    <!-- INTERACTORS TABLE -->
    <c:if test="${not empty entry.interactionList}">
        <div class="grid_23  padding  margin">
            <h5>Interactions</h5>
            <div class="wrap">
                <table class="fixedTable">
                    <thead>
                        <tr class="tableHead">
                            <td>Confidence Score</td>
                            <td>Interactor Accession</td>
                            <td>Interactor Name</td>
                            <td>Evidences</td>
                        </tr>
                    </thead>
                </table>
                <div class="inner_table_div">
                    <table>
                        <c:forEach var="interaction" items="${entry.interactionList}">
                            <tr>
                                <td>${interaction.intactScore}</td>
                                <td>
                                    <!-- Parse the Interactor URL -->
                                    <c:set var="interactorResource" value="${interactorResourceMap[interaction.interactorB.interactorResourceId]}" />
                                    <c:choose>
                                        <%-- Accessions do not have resource (even in intact portal) --%>
                                        <c:when test="${interactorResource.name == 'undefined'}">
                                            ${interaction.interactorB.acc}
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${fn:replace(interactorResource.url, '##ID##', interaction.interactorB.acc)}"
                                               title="Show ${interaction.interactorB.acc}"
                                               rel="nofollow">${interaction.interactorB.acc}</a>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${interaction.interactorB.alias}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${fn:length(interaction.interactionDetailsList) == 0}">
                                            ${fn:length(interaction.interactionDetailsList)}
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${evidencesUrlMap[interaction.interactorB.acc]}" title="Open evidences" rel="nofollow" target="_blank">${fn:length(interaction.interactionDetailsList)}</a>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </div>
        </div>
    </c:if>

</div>

<div class="clear"></div>

<%-- Adding some fixed spaces between last content panel and footer --%>
<div style="height: 40px;">&nbsp;</div>

</div>            <%--A weird thing to avoid problems--%>
<c:import url="footer.jsp"/>

