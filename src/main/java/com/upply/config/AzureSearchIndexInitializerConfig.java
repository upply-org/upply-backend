package com.upply.config;

import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.*;
import com.upply.vector.index.IndexSchema;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class AzureSearchIndexInitializerConfig {
    private final SearchIndexClient searchIndexClient;
    private final List<IndexSchema> schemas;


    @PostConstruct
    public void initializeIndexes() {
        log.info("Initializing {} Azure Search indexes...", schemas.size());
        schemas.forEach(this::ensureIndex);
        log.info("All indexes initialized successfully");
    }

    private void ensureIndex(IndexSchema schema) {
        String indexName = schema.indexName();

        try {
            searchIndexClient.getIndex(indexName);
            log.info("Index '{}' already exists, skipping creation", indexName);
            return;
        } catch (Exception e) {
            log.info("Index '{}' not found, creating...", indexName);
        }

        try {
            SearchIndex index = buildIndex(schema);
            searchIndexClient.createOrUpdateIndex(index);
            log.info("Index '{}' created successfully", indexName);
        } catch (Exception e) {
            log.error("Failed to create index '{}'", indexName, e);
            throw new RuntimeException("Failed to initialize Azure Search index: " + indexName, e);
        }
    }

    private SearchIndex buildIndex(IndexSchema schema) {
        return new SearchIndex(schema.indexName())
                .setFields(schema.fields())
                .setVectorSearch(buildVectorSearch(schema))
                .setSemanticSearch(buildSemanticSearch(schema));
    }

    private VectorSearch buildVectorSearch(IndexSchema schema) {
        return new VectorSearch()
                .setProfiles(List.of(
                        new VectorSearchProfile(
                                schema.vectorProfile(),
                                schema.hnswConfig()
                        )
                ))
                .setAlgorithms(List.of(
                        new HnswAlgorithmConfiguration(schema.hnswConfig())
                                .setParameters(new HnswParameters()
                                        .setMetric(VectorSearchAlgorithmMetric.COSINE)
                                        .setM(10)
                                        .setEfConstruction(400)
                                        .setEfSearch(500))
                ));
    }

    private SemanticSearch buildSemanticSearch(IndexSchema schema) {
        return new SemanticSearch()
                .setDefaultConfigurationName(schema.semanticConfig())
                .setConfigurations(List.of(
                        new SemanticConfiguration(
                                schema.semanticConfig(),
                                new SemanticPrioritizedFields()
                                        .setContentFields(List.of(
                                                new SemanticField("content")
                                        ))
                                        .setKeywordsFields(List.of(
                                                new SemanticField("meta_title")
                                        ))
                        )
                ));
    }


}