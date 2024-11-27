package uk.co.mycomputerworld.docAssist4U.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.ContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import uk.co.mycomputerworld.docAssist4U.model.JsonDualDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class VectorService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private volatile VectorStore vectorStore;

    private static final Logger logger = LoggerFactory.getLogger(VectorService.class);


    private final OllamaChatClient aiClient;

    /**
     *
     * @param aiClient
     */
    VectorService(OllamaChatClient aiClient) {

        this.aiClient = aiClient;
    }

    @Value("classpath:prompt-template.txt")
    private Resource templateFile;

    private final String templateBasic = """
            DOCUMENTS:
            {documents}
            
            QUESTION:
            {question}
            
            INSTRUCTIONS:
            Answer the users question using the DOCUMENTS text above.
            Keep your answer ground in the facts of the DOCUMENTS.
            If the DOCUMENTS doesn’t contain the facts to answer the QUESTION, return: 
            I'm sorry but I haven't enough information to answer.
            """;


    public VectorStore getVectorStore() {
        return vectorStore;
    }
    // Method to store a text document


    // Method to store a text document as documents in the vector store
    public void putTextDocument(Resource docResource) {
        try {
            // Read text from the resource
            String text = Files.readString(docResource.getFile().toPath());
            List<Document> documents = getDocuments(text);

            // Now you have a list of Document objects
            for (Document doc : documents) {
                System.out.println("Content: " + doc.getContent());
                System.out.println("Metadata: " + doc.getMetadata());
                // Optionally, if you need to access content formatters
                // System.out.println("Formatter: " + doc.getContentFormatter());
            }

        } catch (IOException e) {
            logger.error("Failed to read text document: {}", e.getMessage());
        }
    }

    private static List<Document> getDocuments(String text) {
        TextChunkerService textChunkerService = new TextChunkerService();
        List<String> textList = new ArrayList<>();
        textList.add(text);

        /* formatter parameters */
        List<ContentFormatter> formatters = Arrays.asList(
                (document, mode) -> "",
                (document, mode) -> "",
                (document, mode) -> ""
        );

        // Example metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("author", "John Doe");
        metadata.put("date", "2024-06-24");
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

        // Create documents
        List<Document> documents;
        documents = textChunkerService.createDocuments(textList, formatters, metadata);
        return documents;
    }

    //EXPERIMENTAL - NOT FULLY TESTED
    public Boolean putJsonView(String jsonView) {

        String sqlStatement = "select DATA from " + jsonView;

        RowMapper<JsonDualDTO> rowMapper = (rs, rowNum) -> {
            JsonDualDTO jsonElem = new JsonDualDTO();

            String data = rs.getString(1);
            jsonElem.setDATA(data);
            return jsonElem;
        };
        try {
            List<JsonDualDTO> ret = jdbcTemplate.query(sqlStatement, rowMapper);

            for (JsonDualDTO j : ret) {
                Resource resourceJsonDual = new ByteArrayResource(("[" + j.getDATA() + "]").getBytes());
                JsonReader jsonReader = new JsonReader(resourceJsonDual);
                var textSplitter = new TokenTextSplitter();
                this.vectorStore.accept(textSplitter.apply(jsonReader.get()));
            }
            logger.info("JSON view: {} stored as embedding", jsonView);
            return true;
        } catch (Exception ex) {
            logger.error(sqlStatement);
            return false;
        }
    }


    /**
     * Processes a document resource by reading its content,
     * splitting the text, and storing it in a vector store.
     *
     * @param docResource the document resource to be processed
     */
    public void putDocument(Resource docResource) {

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(docResource,
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


    public String rag(String question) {

        String START = "\n<article>\n";
        String STOP = "\n</article>\n";

        List<Document> similarDocuments = this.vectorStore.similaritySearch(
                SearchRequest.
                        query(question).
                        withTopK(4));
        List<Document> similarDocuments2 = vectorStore.similaritySearch(
                SearchRequest.defaults()
                        .withQuery(question)
                        .withTopK(4)
                        .withSimilarityThreshold(0));

        Iterator<Document> iterator = similarDocuments.iterator();
        StringBuilder context = new StringBuilder();
        while (iterator.hasNext()) {
            Document document = iterator.next();
            context.append(document.getId()).append(".");
            context.append(START).append(document.getFormattedContent()).append(STOP);
        }

        String template;
        try {
            template = Files.readString(templateFile.getFile().toPath());

        } catch (IOException e) {
            logger.error(e.getMessage());
            template = templateBasic;
        }


        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("documents", context, "question", question));
        logger.info(prompt.toString());
        ChatResponse aiResponse = aiClient.call(prompt);
        return aiResponse.getResult().getOutput().getContent();
    }

    public List<Document> getSimilarDocs(String message) {
        // Example implementation assuming vectorStore supports similarity search with a string message
        return this.vectorStore.similaritySearch(message);
    }
}
