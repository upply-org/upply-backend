package com.upply.vector.index;

import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.upply.common.IndexName;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserSkillsIndexSchema implements IndexSchema {
    @Override
    public String indexName() {
        return IndexName.USER_SKILLS_INDEX;
    }

    @Override
    public String vectorProfile() {
        return "user-skills-vector-profile";
    }
    @Override
    public int vectorDimensions() {
        return 384;
    }

    @Override
    public String hnswConfig() {
        return "user-skills-hnsw-config";
    }
    @Override
    public String semanticConfig() {
        return "user-skills-semantic-config";
    }
    @Override
    public String semanticKeywordsField() {
        return "meta_userId";
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
                new SearchField("meta_userId", SearchFieldDataType.STRING).setFilterable(true)
        );
    }

}
