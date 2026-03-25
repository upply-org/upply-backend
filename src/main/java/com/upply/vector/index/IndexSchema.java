package com.upply.vector.index;

import com.azure.search.documents.indexes.models.SearchField;

import java.util.List;

public interface IndexSchema {
    String indexName();
    String vectorProfile();
    String hnswConfig();
    String semanticConfig();
    int vectorDimensions();
    List<SearchField> fields();

    default String semanticKeywordsField() {
        return null;
    }
}
