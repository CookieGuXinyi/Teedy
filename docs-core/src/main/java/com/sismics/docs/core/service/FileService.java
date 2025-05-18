package com.sismics.docs.core.service;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.Date;
import java.nio.charset.StandardCharsets;

/**
 * File service.
 *
 * @author bgamard
 */
public class FileService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    /**
     * Phantom references queue.
     */
    private final ReferenceQueue<Path> referenceQueue = new ReferenceQueue<>();
    private final Set<TemporaryPathReference> referenceSet = new HashSet<>();

    public FileService() {
    }

    @Override
    protected void startUp() {
        log.info("File service starting up");
    }

    @Override
    protected void shutDown() {
        log.info("File service shutting down");
    }
    
    @Override
    protected void runOneIteration() {
        try {
            deleteTemporaryFiles();
        } catch (Throwable e) {
            log.error("Exception during file service iteration", e);
        }
    }

    /**
     * Delete unreferenced temporary files.
     */
    private void deleteTemporaryFiles() throws Exception {
        TemporaryPathReference ref;
        while ((ref = (TemporaryPathReference) referenceQueue.poll()) != null) {
            Files.delete(Paths.get(ref.path));
            referenceSet.remove(ref);
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.SECONDS);
    }

    public Path createTemporaryFile() throws IOException {
        return createTemporaryFile(null);
    }

    /**
     * Create a temporary file.
     *
     * @param name Wanted file name
     * @return New temporary file
     */
    public Path createTemporaryFile(String name) throws IOException {
        Path path = Files.createTempFile("sismics_docs", name);
        referenceSet.add(new TemporaryPathReference(path, referenceQueue));
        return path;
    }

    /**
     * Phantom reference to a temporary file.
     *
     * @author bgamard
     */
    static class TemporaryPathReference extends PhantomReference<Path> {
        String path;
        TemporaryPathReference(Path referent, ReferenceQueue<? super Path> q) {
            super(referent, q);
            path = referent.toAbsolutePath().toString();
        }
    }

    /**
     * Translate a file.
     *
     * @param fileId File ID
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @return Translated file ID
     */
    public String translateFile(String fileId, String sourceLanguage, String targetLanguage) throws Exception {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the file
        File file = em.find(File.class, fileId);
        if (file == null) {
            throw new Exception("File not found");
        }

        // Create a new file for the translation
        String translatedFileId = UUID.randomUUID().toString();
        File translatedFile = new File();
        translatedFile.setId(translatedFileId);
        translatedFile.setName(file.getName() + " (" + targetLanguage + ")");
        translatedFile.setMimeType(file.getMimeType());
        translatedFile.setSize(file.getSize());
        translatedFile.setDocumentId(file.getDocumentId());
        translatedFile.setCreateDate(new Date());
        translatedFile.setVersion(file.getVersion());
        translatedFile.setVersionId(file.getVersionId());
        translatedFile.setLatestVersion(true);
        // Copy the file content
        Path sourcePath = getFilePath(file);
        Path targetPath = getFilePath(translatedFile);
        Files.copy(sourcePath, targetPath);

        // If it's a text file, translate its content
        if (file.getMimeType().startsWith("text/")) {
            String content = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
            TranslationService translationService = new TranslationService();
            String translatedContent = translationService.translateText(content, sourceLanguage, targetLanguage);
            Files.write(targetPath, translatedContent.getBytes(StandardCharsets.UTF_8));
        }

        // Save the translated file
        em.persist(translatedFile);

        return translatedFileId;
    }

    /**
     * Get file path.
     */
    private Path getFilePath(File file) {
        // Assuming DirectoryUtil.getStorageDirectory() returns the base directory for file storage
        return DirectoryUtil.getStorageDirectory().resolve(file.getId());
    }
}
