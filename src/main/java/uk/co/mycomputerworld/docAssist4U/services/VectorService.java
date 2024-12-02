package uk.co.mycomputerworld.docAssist4U.services;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VectorService {

    private static final Logger logger = LoggerFactory.getLogger(VectorService.class);


    @Getter
    private final VectorStore vectorStore;

    private final OllamaChatClient aiClient;
    private final String templateBasic = """
            DOCUMENTS:
            {documents}
            
            QUESTION:
            {question}
            
            INSTRUCTIONS:
            Answer the users question using the DOCUMENTS text above.
            
            """;
    @Value("${prompt.file.location}")
    private Resource systemPromptResource;
    @Value("classpath:prompt-template.txt")
    private Resource templateFile;

    public VectorService(VectorStore vectorStore, OllamaChatClient aiClient) {
        this.vectorStore = vectorStore;
        this.aiClient = aiClient;
    }

    /**
     * Processes a document resource by reading its content,
     * splitting the text, and storing it in a vector store.
     *
     * @param docResource the document resource to be processed
     */
    public void putDocument(Resource docResource) {

        DocumentReader pdfReader = new PagePdfDocumentReader(docResource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());


        var textSplitter = new TokenTextSplitter();

        this.vectorStore.accept(textSplitter.apply(pdfReader.get()));
    }

    public String getAnswer(String question) {
        logger.info("GetAnswer question=" + question);
// Combine system message retrieval and AI model call into a single operation
        ChatResponse aiResponse = aiClient.call(new Prompt(List.of(
                getRelevantDocs(question),
                new UserMessage(question))));
        logger.info("GetAnswer aiResponse size=" + aiResponse.getResults().size());
        // Log only necessary information, and use efficient string formatting
        logger.info("Asked AI model and received response.");
        return aiResponse.getResult().getOutput().getContent();

    }

    private Message getRelevantDocs(String question) {
        List<Document> similarDocuments = vectorStore.similaritySearch(question);

        // Log the document count efficiently
        if (logger.isInfoEnabled()) {
            logger.info("Found {} relevant documents.", similarDocuments.size());
        }

        // Streamline document content retrieval
        String documents = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));
        // Log the document count efficiently
        if (logger.isInfoEnabled()) {
            logger.info("documents size ==" + similarDocuments.size());
        }
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemPromptResource);

        // Log the document count efficiently
        if (logger.isInfoEnabled()) {
            logger.info("systemPromptTemplate ={}", systemPromptTemplate.getTemplate());
        }

        return systemPromptTemplate.createMessage(Map.of("documents", documents, "question", question));
    }

    public List<Document> getSimilarDocs(String message) {
        return this.vectorStore.similaritySearch(message);
    }
}
