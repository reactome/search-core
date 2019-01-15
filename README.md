[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# Search Core

#### What is Reactome Search ?
Reactome Search is a project that optimizes the queries in Reactome Website. Based on Apache Lucene, Reactome Graph Database is fully indexed by Apache SolR. SolR is versatile, it's configured and parametrized to face Reactome needs and requirements, delivering a high performance and accurate result list. The Search Project is split into 'Indexer' and 'Search':

* Indexer: querying Reactome Graph Database and index PhysicalEntities and Event into SolR documents
* Search: Spring MVC Application which queries SolR documents in order to optimize the searching for Reactome Pathway Browser.

#### Search Core
The Search Core project provides access to the data in SolR based on the search term, faceting, clustered, etc.

#### Key Classes

* SearchService.java: facade between the DataContent and SolR.

* SolrCore.java: perform queries directly in SolR and retrieve the QueryResponse.

* SolrConverter.java: get the QueryResponse and `convert` into our domain.
