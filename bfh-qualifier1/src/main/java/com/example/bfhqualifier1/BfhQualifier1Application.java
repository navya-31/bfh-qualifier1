package com.example.bfhqualifier1;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BfhQualifier1Application {

    @Value("${app.name}")
    private String name;

    @Value("${app.regNo}")
    private String regNo;

    @Value("${app.email}")
    private String email;

    @Value("${app.finalQuery}")
    private String finalQuery;

    public static void main(String[] args) {
        SpringApplication.run(BfhQualifier1Application.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            RestTemplate rest = new RestTemplate();

            // 1. Generate webhook
            String genUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
            Map<String, String> genBody = Map.of(
                "name", name,
                "regNo", regNo,
                "email", email
            );
            HttpEntity<Map<String, String>> genRequest = new HttpEntity<>(genBody, headersJson());
            ResponseEntity<Map> genResp = rest.postForEntity(genUrl, genRequest, Map.class);

            String webhook = genResp.getBody().get("webhook").toString();
            String accessToken = genResp.getBody().get("accessToken").toString();

            System.out.println("Webhook URL: " + webhook);
            // Do NOT log token in shared logs; shown here for debugging only
            System.out.println("AccessToken (truncated): " + accessToken.substring(0, 20) + "...");

            // 2. Submit final SQL
            String submitUrl = webhook;
            Map<String, String> payload = Map.of("finalQuery", finalQuery);
            HttpHeaders headers2 = headersJson();
            headers2.set("Authorization", accessToken); // or "Bearer " + accessToken if required
            HttpEntity<Map<String, String>> submitRequest = new HttpEntity<>(payload, headers2);
            ResponseEntity<String> submitResp = rest.postForEntity(submitUrl, submitRequest, String.class);

            System.out.println("Submission Response: " + submitResp.getBody());
        };
    }

    private HttpHeaders headersJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}