package org.example;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.UnsupportedEncodingException;

public class CrptApi_2 {

    private final int requestLimit;
    private final TimeUnit timeUnit;
    private static int counter;

    private static final String URL = "http://<server-name>[:server-port]/api/v3/{extension}/ rollout?omsId={omsId}";
    private static final String CLIENT_TOKEN = "clientToken";
    private static final String USER_NAME = "userName";

    public CrptApi_2(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        if (requestLimit >= 0) {
            this.requestLimit = requestLimit;
            counter = requestLimit;
        } else {
            throw new IllegalArgumentException("Это отрицательное число -> неверные параметры!");
        }
    }

    public void runRequest(Document document, String signature) {
        if (document == null || signature == null) {
            throw new IllegalArgumentException("Документ или подпись не могут быть null");
        }
        String docJson = getDocJson(document, signature).toString();
        sendHttpRequest(docJson);
    }

    @SuppressWarnings("unchecked")
    private JSONObject getDocJson(Document document, String signature) {
        JSONObject doc = new JSONObject();

        if (isNull(document.getDescription())) {
            JSONObject inn = new JSONObject();
            inn.put("participantInn", document.getParticipantInn());
            doc.put("description", inn);
        }

        doc.put("doc_id", document.getDocId());
        doc.put("doc_status", document.getDocStatus());
        doc.put("doc_type", document.getDocType());

        if (isNull(document.getImportRequest())) {
            doc.put("importRequest", document.getImportRequest());
        }

        doc.put("owner_inn", document.getOwnerInn());
        doc.put("participant_inn", document.getParticipantInn());
        doc.put("producer_inn", document.getProducerInn());
        doc.put("production_date", document.getProducerInn());
        doc.put("production_type", document.getProductionType());

        org.example.CrptApi.Document.Products product = document.getProducts();

        if (product != null) {
            JSONArray productsList = new JSONArray();
            JSONObject products = new JSONObject();

            if (product.getCertificateDocument() != null) {
                products.put("certificate_document", product.getCertificateDocument());
            } else if (isNull(product.getCertificateDocumentDate())) {
                products.put("certificate_document_date", product.getCertificateDocumentDate());
            } else if (isNull(product.getCertificateDocumentNumber())) {
                products.put("certificate_document_number", product.getCertificateDocumentNumber());
            }

            products.put("owner_inn", document.getOwnerInn());
            products.put("producer_inn", document.getProducerInn());

            if (!document.getProductionDate().equals(product.getProductionDate())) {
                products.put("production_date", product.getProductionDate());
            } else {
                products.put("production_date", document.getProductionDate());
            }

            products.put("tnved_code", product.getTnvedCode());

            if (isNull(product.getUitCode())) {
                products.put("uit_code", product.getUitCode());
            } else if (isNull(product.getUituCode())) {
                products.put("uitu_code", product.getUituCode());
            } else {
                throw new IllegalArgumentException("Одно из полей uit_code/uitu_code " +
                        "является обязательным");
            }

            productsList.add(products);
            doc.put("products", productsList);
        }

        doc.put("signature", signature);
        doc.put("reg_date", document.getRegDate());
        doc.put("reg_number", document.getRegNumber());

        return doc;
    }

    private void sendHttpRequest(String json) {
        if (requestLimit != 0) {
            synchronized (this) {
                counter--;
            }
        }
        try {
            if (counter < 0) {
                Thread.sleep(getTime());
                counter = requestLimit;
            }
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                HttpPost post = createHttpPost();
                setRequestHeaders(post);
                setRequestBody(post, json);
                httpClient.execute(post);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HttpPost createHttpPost() {
        HttpPost post = new HttpPost(URL);
        post.addHeader("content-type", "application/json");
        post.addHeader("clientToken", CLIENT_TOKEN);
        post.addHeader("userName", USER_NAME);
        return post;
    }

    private void setRequestHeaders(HttpPost post) {
        post.addHeader("content-type", "application/json");
        post.addHeader("clientToken", CLIENT_TOKEN);
        post.addHeader("userName", USER_NAME);
    }

    private void setRequestBody(HttpPost post, String json) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(json);
        post.setEntity(entity);
    }

    private boolean isNull(String check) {
        return check != null;
    }

    public enum TimeUnit {
        SECOND, MINUTE, HOUR
    }

    private long getTime() {
        return switch (timeUnit) {
            case SECOND -> 1000;
            case MINUTE -> 1000 * 60;
            case HOUR -> 1000 * 60 * 60;
        };
    }

    public static class Document {
        @Getter
        @Setter
        private String description;
        @Getter
        private final String participantInn;
        @Getter
        private final String docId;
        @Getter
        private final String docStatus;
        @Getter
        private final String docType;
        @Getter
        @Setter
        private String importRequest;
        @Getter
        private final String ownerInn;
        @Getter
        private final String producerInn;
        @Getter
        private final String productionDate;
        @Getter
        private final String productionType;
        @Getter
        private final String regDate;
        @Getter
        private final String regNumber;
        @Getter
        @Setter
        private org.example.CrptApi.Document.Products products;

        public Document(String participantInn, String docId, String docStatus,
                        String docType, String ownerInn, String producerInn,
                        String productionDate, String productionType,
                        String regDate, String regNumber) {
            this.participantInn = participantInn;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.regDate = regDate;
            this.regNumber = regNumber;
        }

        public static class Products {
            @Getter
            @Setter
            private org.example.CrptApi.Document.Products.CertificateType certificateDocument;
            @Getter
            @Setter
            private String certificateDocumentDate;
            @Getter
            @Setter
            private String certificateDocumentNumber;
            @Getter
            @Setter
            private String productionDate;
            @Getter
            @Setter
            private String tnvedCode;
            @Getter
            @Setter
            private String uitCode;
            @Getter
            @Setter
            private String uituCode;

            public enum CertificateType {
                CONFORMITY_CERTIFICATE, CONFORMITY_DECLARATION
            }
        }
        public static void main(String[] args) {
            CrptApi_2 api = new CrptApi_2(TimeUnit.MINUTE, 0);
            System.out.println( api.getDocJson(new Document("123", "123", "123", "123", "123", "123", "123", "123", "123", "123"), "123") );
        }
    }
}