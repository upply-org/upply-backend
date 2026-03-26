package com.upply.vector.index;

import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.upply.common.IndexName;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResumeIndexSchema implements IndexSchema {

    @Override
    public String indexName() {
        return IndexName.RESUME_INDEX;
    }

    @Override
    public String vectorProfile() {
        return "Resume-vector-profile";
    }

    @Override
    public String hnswConfig() {
        return "resume-hnsw-config";
    }

    @Override
    public String semanticConfig() {
        return "resume-semantic-config";
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
                new SearchField("meta_applicationId", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("meta_jobId", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("meta_userId", SearchFieldDataType.STRING).setFilterable(true),
                new SearchField("meta_chunkType", SearchFieldDataType.STRING).setFilterable(true)
        );
    }
}
