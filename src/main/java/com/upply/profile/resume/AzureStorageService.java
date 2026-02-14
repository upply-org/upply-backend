package com.upply.profile.resume;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AzureStorageService {

    @Value("${azure.storage.connection-String}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;

    @PostConstruct
    public void init(){
        try{
            this.blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
            containerClient.createIfNotExists();
            log.info("Azure Storage Service initialized Successfully");
        }catch (Exception e) {
            log.error("Failed to initialize Azure Storage Service", e);
            throw new RuntimeException("Failed to initialize Azure Storage Service", e);
        }
    }

    /**
     * Upload file to azure blob storage
     * @param userId User id used as dir name
     * @param fileData File content as an array of bytes
     * @return blob name of an uploaded file
     */
    public String uploadFile(Long userId, byte[] fileData){
        try{
            String blobName = fileBlobName(userId);

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(BinaryData.fromBytes(fileData),true);
            log.info("File Uploaded: {}", blobName);
            return blobName;
        }catch (Exception e) {
            log.error("Error uploading file for profile: {}", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * Download file to azure blob storage
     * @param blobName file UUID name
     * @return File data as bytes
     */
    public byte[] downloadFile(String blobName){
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            if(!blobClient.exists()){
                log.error("file doesn't exit: {}" + blobName);
                throw new RuntimeException("File Not Found:{} " + blobName);
            }
            BinaryData data = blobClient.downloadContent();
            return data.toBytes();
        } catch (Exception e) {
            log.error("Error downloading file for blobName: {}", blobName);
            throw new RuntimeException(e);
        }
    }

    private String fileBlobName(Long userId){
        String fileUUID = UUID.randomUUID().toString();
        String userIdString = String.valueOf(userId);
        return userIdString + "/" + fileUUID;
    }

}
