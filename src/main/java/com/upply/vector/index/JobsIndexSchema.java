package com.upply.vector.index;

import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobsIndexSchema implements IndexSchema {
    @Override
    public String indexName() {
        return "jobs-index";
    }

    @Override
    public String vectorProfile() {
        return "jobs-vector-profile";
    }

    @Override
    public String hnswConfig() {
        return "jobs-hnsw-config";
    }

    @Override
    public String semanticConfig() {
        return "jobs-semantic-config";
    }

    @Override
    public int vectorDimensions() {
        return 768;
    }

    @Override
    public List<SearchField> fields() {
        return List.of(
                new SearchField("id", SearchFieldDataType.STRING).setKey(true).setFilterable(true),
                new SearchField("content", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("embedding", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                        .setSearchable(true)
                        .setVectorSearchDimensions(vectorDimensions())
                        .setVectorSearchProfileName(vectorProfile()),
                new SearchField("metadata", SearchFieldDataType.STRING),
                new SearchField("meta_jobId", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("meta_title", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true),
                new SearchField("meta_status", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("meta_type", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("meta_seniority", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("meta_model", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("meta_location", SearchFieldDataType.STRING).setFilterable(true)
        );
    }
}
