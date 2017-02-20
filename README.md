<img src=https://cloud.githubusercontent.com/assets/6883670/22938783/bbef4474-f2d4-11e6-92a5-07c1a6964491.png width=220 height=100 />

# Search Core

#### What is Reactome Search ?
Reactome Search is a project that optimizes the queries in Reactome Website. Based on Apache Lucene, Reactome Graph Database is fully indexed by Apache SolR. SolR is versatile, it's configured and parametrized to face Reactome needs and requirements, delivering a high performance and accurate result list. The Search Project is split into 'Indexer' and 'Search':

* Indexer: querying Reactome Graph Database and index PhysicalEntities, Event and Regulation into SolR documents
* Search: Spring MVC Application which queries SolR documents in order to optimize the searching for Reactome Pathway Browser.

#### Search Core
The Search Core project provides access to the data in SolR based on the search term, faceting, clustered, etc. It also takes into account interactors.

#### Key Classes

* SearchService.java: facade between the DataContent and SolR.

* SolrCore.java: perform queries directly in SolR and retrieve the QueryResponse.

* SolrConverter.java: get the QueryResponse and `convert` into our domain.