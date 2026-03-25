package com.upply.config;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.upply.common.IndexName;
import com.upply.profile.resume.AzureStorageService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.azure.AzureVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("!test")
public class VectorStoreConfig {

    @Value("${azure.search.endpoint}")
    private String searchEndpoint;
    @Value("${azure.search.key}")
    private String searchKey;

    @Bean
    public SearchIndexClient searchIndexClient(){
        return new SearchIndexClientBuilder()
                .endpoint(searchEndpoint)
                .credential(new AzureKeyCredential(searchKey))
                .buildClient();
    }
    @Bean
    public VectorStore jobsVectorStore(SearchIndexClient searchIndexClient,
                                   EmbeddingModel embeddingModel) {
        return AzureVectorStore.builder(searchIndexClient, embeddingModel)
                .indexName(IndexName.JOBS_INDEX)
                .initializeSchema(false)
                .filterMetadataFields(List.of(
                        AzureVectorStore.MetadataField.text("jobId"),
                        AzureVectorStore.MetadataField.text("title"),
                        AzureVectorStore.MetadataField.text("type"),
                        AzureVectorStore.MetadataField.text("seniority"),
                        AzureVectorStore.MetadataField.text("model"),
                        AzureVectorStore.MetadataField.text("location"),
                        AzureVectorStore.MetadataField.text("status")
                ))
                .build();
    }
    @Bean
    public VectorStore resumeVectorStore(SearchIndexClient searchIndexClient,
                                         EmbeddingModel embeddingModel){
        return AzureVectorStore.builder(searchIndexClient,embeddingModel)
                .indexName(IndexName.RESUME_INDEX)
                .initializeSchema(false)
                .filterMetadataFields(List.of(
                        AzureVectorStore.MetadataField.text("applicationId"),
                        AzureVectorStore.MetadataField.text("jobId"),
                        AzureVectorStore.MetadataField.text("chunkType"),
                        AzureVectorStore.MetadataField.text("userId")
                )).build();
    }
}
