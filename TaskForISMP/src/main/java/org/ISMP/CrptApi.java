package org.ISMP;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {

    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final int requestLimit;
    private final long timeIntervalMillis;

    private final ObjectMapper objectMapper = new ObjectMapper();


    private long lastRequestTimeMillis = 0;
    private int requestCount = 0;

    public CrptApi(int requestLimit, long timeIntervalMillis) {
        this.requestLimit = requestLimit;
        this.timeIntervalMillis = timeIntervalMillis;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Description {
        private String participantInn;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;
    }

    public Document getDefaultDocument(){
        Description description = CrptApi.Description.builder()
                .participantInn("1234567890")  // Установите реальные значения
                .build();

        Product product = CrptApi.Product.builder()
                .certificate_document("Certificate123")
                .certificate_document_date("2022-01-20")
                .certificate_document_number("12345")
                .owner_inn("0987654321")
                .producer_inn("9876543210")
                .production_date("2022-01-20")
                .tnved_code("TNVED123")
                .uit_code("UIT123")
                .uitu_code("UITU123")
                .build();

        List<Product> products = new ArrayList<>();
        products.add(product);

        return Document.builder()
                .description(description)
                .doc_id("Doc123")
                .doc_status("Pending")
                .doc_type("LP_INTRODUCE_GOODS")
                .importRequest(true)
                .owner_inn("9876543210")
                .participant_inn("1234567890")
                .producer_inn("9876543210")
                .production_date("2022-01-20")
                .production_type("Type123")
                .products(products)
                .reg_date("2022-01-20")
                .reg_number("Reg123")
                .build();
    }

    public void createDocument(Document document, String signature) {
        try {
            reentrantLock.lock();

            checkRequestLimit();

            performApiRequest(document, signature);

            requestCount++;
        } finally {
            reentrantLock.unlock();
        }
    }

    private void checkRequestLimit() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastRequestTimeMillis >= timeIntervalMillis) {
            requestCount = 0;
            lastRequestTimeMillis = currentTimeMillis;
        }

        if (requestCount >= requestLimit) {
            throw new IllegalStateException("Превышен лимит запросов");
        }
    }

    private void performApiRequest(Document document, String signature) {
        String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(convertObjectToJson(document)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            HttpHeaders headers = response.headers();
            String responseBody = response.body();

            System.out.println("Status Code: " + statusCode);
            System.out.println("Headers: " + headers);
            System.out.println("Response Body: " + responseBody);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String convertObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
