package uk.co.mycomputerworld.docAssist4U.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.ContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class responsible for chunking text into smaller parts and creating documents from them.
 * Extends the TextSplitter class to utilize its text splitting capabilities.
 */
public class TextChunkerService extends TextSplitter {
    /**
     * Flag indicating whether to copy the content formatter from the original document to the chunks.
     */
    private boolean copyContentFormatter = true;

    /**
     * Logger instance for logging information and debugging messages.
     */
    private static final Logger logger = LoggerFactory.getLogger(TextChunkerService.class);

    /**
     * Splits the given text into a list of smaller text chunks.
     * This method is intended to be overridden by subclasses to provide specific text splitting logic.
     *
     * @param text The text to be split into chunks.
     * @return A list of text chunks.
     */
    @Override
    protected List<String> splitText(String text) {
        return List.of();
    }

    /**
     * Creates a list of Document objects from the provided texts, applying the specified content formatters
     * and metadata to each document. The text is split into chunks, and each chunk is used to create a new Document.
     *
     * @param texts      A list of strings, each representing the text content for a document.
     * @param formatters A list of ContentFormatter objects, each corresponding to a text in the texts list.
     * @param metadata   A map containing metadata key-value pairs to be associated with each document.
     * @return A list of Document objects created from the provided texts, with applied formatters and metadata.
     * @throws IllegalStateException if there is a duplicate key in the metadata map.
     */
    public List<Document> createDocuments(List<String> texts, List<ContentFormatter> formatters,
                                          Map<String, Object> metadata) {

        // Process the data in a column oriented way and recreate the Document
        List<Document> documents = new ArrayList<>();
        int textsSize = texts.size();
        for (int i = 0; i < textsSize; i++) {

            String text = texts.get(i);
            List<String> chunks = splitText(text);

            if (chunks.size() > 1) {
                logger.info(" TextChunkerService: Splitting up document into {} chunks. Text Size {} {}", chunks.size(), i, texts.size());
            } else {
                logger.info("TextChunkerService: Splitting up document into 1 chunk. Text Size {} {}", i, texts.size());
            }
            for (String chunk : chunks) {
                Map<String, Object> metadataCopy = new HashMap<>();
                for (Map.Entry<String, Object> txtValue : metadata.entrySet()) {

                    if (metadataCopy.put(txtValue.getKey(), txtValue.getValue()) != null) {
                        throw new IllegalStateException("Duplicate key");
                    }
                }
                Document newDoc = new Document(chunk, metadataCopy);

                if (this.copyContentFormatter) {
                    // Transfer the content-formatter of the parent to the chunked
                    // documents it was slit into.
                    newDoc.setContentFormatter(formatters.get(i));
                }

                // TODO copy over other properties.
                documents.add(newDoc);
            }
        }
        return documents;
    }
}