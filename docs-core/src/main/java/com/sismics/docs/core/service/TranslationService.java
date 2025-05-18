package com.sismics.docs.core.service;

import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.docs.core.util.DirectoryUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import okhttp3.*;
import java.io.*;

/**
 * Translation service.
 */
public class TranslationService extends AbstractScheduledService {
    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);
    private static final String Access_Token = "24.bd93e9503bb1c4c546005ae838eea477.2592000.1750094551.282335-118918223";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Override
    protected void startUp() {
        log.info("Translation service starting up");
    }

    @Override
    protected void shutDown() {
        log.info("Translation service shutting down");
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.SECONDS);
    }

    @Override
    protected void runOneIteration() throws Exception {
        log.info("Translation service running one iteration");
    }

    public TranslationService() {
    }
    
    /**
     * Translate text using Google Translate API.
     *
     * @param text Text to translate
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @return Translated text
     * @throws IOException If an I/O error occurs during translation
     */
    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        final String apiUrl = "https://aip.baidubce.com/rpc/2.0/mt/texttrans/v1?access_token=" + Access_Token;

        OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
        MediaType mediaType = MediaType.parse("application/json");

        String[] segments = text.split("\n");  // 按换行分段
        StringBuilder finalTranslation = new StringBuilder();

        for (String segment : segments) {
            String trimmedSegment = segment.trim();
            if (trimmedSegment.isEmpty()) {
                finalTranslation.append("\n"); // 保留空行
                continue;
            }

            JSONObject json = new JSONObject();
            json.put("from", sourceLanguage);
            json.put("to", targetLanguage);
            json.put("q", trimmedSegment);

            RequestBody body = RequestBody.create(mediaType, json.toString());

            Request request = new Request.Builder()
                .url(apiUrl)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        if (attempt == MAX_RETRIES) {
                            return "TranslateServiceError: Unexpected response: " + response + " -- segment: " + trimmedSegment;
                        }
                        Thread.sleep(RETRY_DELAY_MS);
                        continue;
                    }

                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.has("error_msg")) {
                        if (attempt == MAX_RETRIES) {
                            return "TranslateServiceError: Baidu Translation API error: " + jsonResponse.getString("error_msg") + " -- segment: " + trimmedSegment;
                        }
                        Thread.sleep(RETRY_DELAY_MS);
                        continue;
                    }

                    JSONArray transResultArray = jsonResponse.getJSONObject("result").getJSONArray("trans_result");
                    for (int i = 0; i < transResultArray.length(); i++) {
                        JSONObject item = transResultArray.getJSONObject(i);
                        finalTranslation.append(item.getString("dst")).append("\n");
                    }
                    break;
                } catch (Exception e) {
                    if (attempt == MAX_RETRIES) {
                        return "TranslateServiceError: Unknown error:" + e.getMessage() + " -- segment: " + trimmedSegment;
                    }
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "TranslateServiceError: Retry interrupted -- segment: " + trimmedSegment;
                    }
                }
            }
        }
        return finalTranslation.toString().trim();
    }
    
    /**
     * Check if a file is text-based based on its MIME type.
     */
    private boolean isTextFile(String mimeType) {
        return mimeType != null && (
            mimeType.startsWith("text/") ||
            mimeType.equals("application/json") ||
            mimeType.equals("application/xml") ||
            mimeType.equals("application/javascript") ||
            mimeType.equals("application/x-www-form-urlencoded")
        );
    }
    
    /**
     * Read file content.
     *
     * @param file File to read
     * @return File content as string
     * @throws IOException If an I/O error occurs
     */
    private String readFileContent(File file) throws IOException {
        Path filePath = getFilePath(file);
        return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
    }
    
    /**
     * Write file content.
     *
     * @param file File to write
     * @param content Content to write
     * @throws IOException If an I/O error occurs
     */
    private void writeFileContent(File file, String content) throws IOException {
        Path filePath = getFilePath(file);
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Copy file.
     *
     * @param source Source file
     * @param target Target file
     * @throws IOException If an I/O error occurs
     */
    private void copyFile(File source, File target) throws IOException {
        Path sourcePath = getFilePath(source);
        Path targetPath = getFilePath(target);
        Files.copy(sourcePath, targetPath);
    }
    
    /**
     * Get file path.
     */
    private Path getFilePath(File file) {
        // Assuming DirectoryUtil.getStorageDirectory() returns the base directory for file storage
        return DirectoryUtil.getStorageDirectory().resolve(file.getId());
    }
    
    // /**
    //  * Translate a document to another language.
    //  *
    //  * @param documentId Document ID
    //  * @param sourceLanguage Source language code
    //  * @param targetLanguage Target language code
    //  * @param principalId Principal ID
    //  * @return Translated document ID
    //  * @throws IOException If an I/O error occurs during translation
    //  */
    // public String translateDocument(String documentId, String sourceLanguage, String targetLanguage, String principalId) throws IOException {
    //     EntityManager em = ThreadLocalContext.get().getEntityManager();
        
    //     // Get the original document
    //     Document originalDoc = em.find(Document.class, documentId);
    //     if (originalDoc == null) {
    //         throw new RuntimeException("Document not found");
    //     }
        
    //     // Create a new document for the translation
    //     Document translatedDoc = new Document();
    //     translatedDoc.setTitle(translateText(originalDoc.getTitle(), sourceLanguage, targetLanguage));
    //     translatedDoc.setDescription(translateText(originalDoc.getDescription(), sourceLanguage, targetLanguage));
    //     translatedDoc.setLanguage(targetLanguage);
    //     translatedDoc.setCreateDate(new Date());
        
    //     // Save the translated document
    //     em.persist(translatedDoc);
        
    //     // Copy and translate files from original document
    //     Query q = em.createQuery("select f from File f where f.documentId = :documentId")
    //         .setParameter("documentId", documentId);
    //     List<File> files = q.getResultList();
        
    //     for (File originalFile : files) {
    //         File translatedFile = new File();
    //         translatedFile.setDocumentId(translatedDoc.getId());
    //         translatedFile.setName(translateText(originalFile.getName(), sourceLanguage, targetLanguage));
    //         translatedFile.setMimeType(originalFile.getMimeType());
    //         translatedFile.setSize(originalFile.getSize());
    //         translatedFile.setCreateDate(new Date());
    //         translatedFile.setVersion(1);
            
    //         // If the file is text-based, translate its content
    //         if (isTextFile(originalFile.getMimeType())) {
    //             String content = readFileContent(originalFile);
    //             String translatedContent = translateText(content, sourceLanguage, targetLanguage);
    //             writeFileContent(translatedFile, translatedContent);
    //         } else {
    //             // For non-text files, just copy the file
    //             // TODO: maybe extract the file content and translate it ?
    //             copyFile(originalFile, translatedFile);
    //         }
            
    //         em.persist(translatedFile);
    //     }
        
    //     return translatedDoc.getId();
    // }
} 