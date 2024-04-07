package angaman.cedrick.rag_openai;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageClient;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@PropertySource("classpath:Key/.env")
public class RagOpenAiApplication {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    public static void main(String[] args) {
        SpringApplication.run(RagOpenAiApplication.class, args);
    }

//        @Bean
    CommandLineRunner commandLineRunner (VectorStore vectorStore, JdbcTemplate jdbcTemplate,
                                         @Value("classpath:powerpoint/*") Resource[] resources) {
        return args -> {
//            textEmbedding(vectorStore, jdbcTemplate, resources);
//            String query = "dans les memoires donne moi toute les thematic utilisées";
//            String query = "donne les Marque";
//            String query = "un resumer";
//            askLlm(vectorStore, query);
            String prompt = "A light cream colored mini golden doodle";
            extracted(prompt);


        };
    }

    private void extracted(String prompt) {
        System.out.println("0");
        OpenAiImageApi api = new OpenAiImageApi(apiKey);
        OpenAiImageClient openaiImageClient = new OpenAiImageClient(api);
        System.out.println("00");

        ImageResponse response = openaiImageClient.call(
                new ImagePrompt(prompt,
                        OpenAiImageOptions.builder()
                                .withQuality("hd")
                                .withN(1)
                                .withHeight(1024)
                                .withWidth(1024).build())

        );
        System.out.println("1");
        System.out.println(response.getResult().getOutput().getUrl());
        System.out.println("2");
    }

    private void askLlm(VectorStore vectorStore,String query) {
//                    String query = "donne moi pour chaque memoire au format json, l'auteur du memoire, les membres du jury, un petit résumé du mémoire et les languages utilisées.";
//                String query = "dans les memoires donne moi toute les thematic utilisées";
        List<Document> documentList = vectorStore.similaritySearch(query);
        String systemMessageTemplate = """
                Répondez à la question, au format json mais n'ajoute pas ```json   ``` ,en vous basant uniquement sur le CONTEXTE fourni.
                Si la réponse n'est pas trouvée dans le contexte, répondez ' je ne sais pas '.
                CONTEXTE:
                     {CONTEXTE}
                """;
        Message systemMessage = new SystemPromptTemplate(systemMessageTemplate)
                .createMessage(Map.of("CONTEXTE",documentList));
        UserMessage userMessage = new UserMessage(query);
        Prompt prompt = new Prompt(List.of(systemMessage,userMessage));
        OpenAiApi aiApi = new OpenAiApi(apiKey);
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .withModel("gpt-4-turbo-preview")
                .withTemperature(0F)
                .withMaxTokens(800)
                .build();
        OpenAiChatClient openAiChatClient = new OpenAiChatClient(aiApi, openAiChatOptions);
        ChatResponse response = openAiChatClient.call(prompt);
        String responseContent = response.getResult().getOutput().getContent();
        System.out.println(responseContent);
    }

//    private static void textEmbedding(VectorStore vectorStore, JdbcTemplate jdbcTemplate, Resource[] pdfResources) {
//        jdbcTemplate.update("delete from vector_store");
//        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.defaultConfig();
//        String content = "";
//        for(Resource resource : pdfResources){
//            PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource,config);
//            List<Document> documentList = pagePdfDocumentReader.get();
//            content += documentList.stream().map(d -> d.getContent()).collect(Collectors.joining("\n"))+"\n";
//        }
//
//        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
//        List<String> chunks = tokenTextSplitter.split(content,1000);
//        List<Document> chunksDocs = chunks.stream().map(chunk -> new Document(chunk)).collect(Collectors.toList());
//        vectorStore.accept(chunksDocs);
//    }
//private static void textEmbedding(VectorStore vectorStore, JdbcTemplate jdbcTemplate, Resource[] pdfResources) {
//    jdbcTemplate.update("delete from vector_store");
//    String content = "";
//    for(Resource resource : pdfResources){
//        try (InputStream inputStream = resource.getInputStream()) {
//            XWPFDocument document = new XWPFDocument(inputStream);
//            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
//            content += extractor.getText();
//        } catch (IOException e) {
//            // Gérer l'erreur d'une manière appropriée
//            e.printStackTrace();
//        }
//    }
//
//    TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
//    List<String> chunks = tokenTextSplitter.split(content,1000);
//    List<Document> chunksDocs = chunks.stream().map(chunk -> new Document(chunk)).collect(Collectors.toList());
//    vectorStore.accept(chunksDocs);
//}

//    private static void textEmbedding(VectorStore vectorStore, JdbcTemplate jdbcTemplate, Resource[] pdfResources) {
//        jdbcTemplate.update("delete from vector_store");
//        String content = "";
//        for(Resource resource : pdfResources){
//            try (InputStream inputStream = resource.getInputStream()) {
//                Workbook workbook = WorkbookFactory.create(inputStream);
//                int numberOfSheets = workbook.getNumberOfSheets();
//                for (int i = 0; i < numberOfSheets; i++) {
//                    Sheet sheet = workbook.getSheetAt(i);
//                    Iterator<Row> rowIterator = sheet.iterator();
//                    while (((Iterator<?>) rowIterator).hasNext()) {
//                        Row row = rowIterator.next();
//                        Iterator<Cell> cellIterator = row.cellIterator();
//                        while (cellIterator.hasNext()) {
//                            Cell cell = cellIterator.next();
//                            content += cell.toString() + " ";
//                        }
//                        content += "\n";
//                    }
//                }
//            } catch (IOException e) {
//                // Gérer l'erreur d'une manière appropriée
//                e.printStackTrace();
//            }
//        }
//
//        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
//        List<String> chunks = tokenTextSplitter.split(content, 1000);
//        List<Document> chunksDocs = chunks.stream().map(Document::new).collect(Collectors.toList());
//        vectorStore.accept(chunksDocs);
//    }

    private static void textEmbedding(VectorStore vectorStore, JdbcTemplate jdbcTemplate, Resource[] pdfResources) {
        jdbcTemplate.update("delete from vector_store");
        String content = "";
        for (Resource resource : pdfResources) {
            try (InputStream inputStream = resource.getInputStream()) {
                XMLSlideShow ppt = new XMLSlideShow(inputStream);
                for (XSLFSlide slide : ppt.getSlides()) {
                    for (XSLFShape shape : slide.getShapes()) {
                        if (shape instanceof XSLFTextShape) {
                            XSLFTextShape textShape = (XSLFTextShape) shape;
                            for (XSLFTextParagraph paragraph : textShape) {
                                content += paragraph.getText() + "\n";
                            }
                        }
                    }
                }
            } catch (IOException e) {
                // Gérer l'erreur d'une manière appropriée
                e.printStackTrace();
            }
        }

        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<String> chunks = tokenTextSplitter.split(content, 1000);
        List<Document> chunksDocs = chunks.stream().map(Document::new).collect(Collectors.toList());
        vectorStore.accept(chunksDocs);
    }
}