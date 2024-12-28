package com.project.mini_ssf.service;

import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.project.mini_ssf.model.EntityDetails;
import com.project.mini_ssf.repo.AcraRepo;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class AcraService {

    @Value("${data.gov.sg.datasetId}")
    private String dataset;

    @Value("${data.gov.sg.url}")
    private String dataUrl;

    @Autowired
    private AcraRepo acraRepo;

    public EntityDetails getEntityByUen(String uen) {
        try {
            String filters = URLEncoder.encode("{\"uen\":{\"type\":\"ILIKE\",\"value\":\"" + uen + "\"}}",
                    StandardCharsets.UTF_8);
            String url = dataUrl + dataset + "&filters=" + filters + "&limit=10";
            RequestEntity<Void> request = RequestEntity
                    .get(URI.create(url))
                    .accept(MediaType.APPLICATION_JSON)
                    .build();

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(request, String.class);

            if (response.getBody() == null || response.getBody().isEmpty()) {
                return null;
            }

            try (JsonReader reader = Json.createReader(new StringReader(response.getBody()))) {
                JsonObject jsonResponse = reader.readObject();
                JsonObject result = jsonResponse.getJsonObject("result");
                JsonArray records = result.getJsonArray("records");

                if (records != null && !records.isEmpty()) {
                    JsonObject record = records.getJsonObject(0);

                    EntityDetails entityDetails = new EntityDetails();
                    entityDetails.setUen(record.getString("uen", null));
                    entityDetails.setIssuanceAgencyId(record.getString("issuance_agency_id", null));
                    entityDetails.setUenStatus(record.getString("uen_status", null));
                    entityDetails.setEntityName(record.getString("entity_name", null));
                    entityDetails.setEntityType(record.getString("entity_type", null));
                    entityDetails.setUenIssueDate(LocalDate.parse(record.getString("uen_issue_date", null)));
                    entityDetails.setRegStreetName(record.getString("reg_street_name", null));
                    entityDetails.setRegPostalCode(record.getString("reg_postal_code", null));

                    return entityDetails;
                }
            }

            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void saveAcraToSeller(String userUuid,EntityDetails ent) {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("uen", ent.getUen() != null ? ent.getUen() : "")
                .add("issuanceAgencyId", ent.getIssuanceAgencyId() != null ? ent.getIssuanceAgencyId() : "")
                .add("uenStatus", ent.getUenStatus() != null ? ent.getUenStatus() : "")
                .add("entityName", ent.getEntityName() != null ? ent.getEntityName() : "")
                .add("entityType", ent.getEntityType() != null ? ent.getEntityType() : "")
                .add("uenIssueDate", ent.getUenIssueDate() != null ? ent.getUenIssueDate().toString() : "")
                .add("regStreetName", ent.getRegStreetName() != null ? ent.getRegStreetName() : "")
                .add("regPostalCode", ent.getRegPostalCode() != null ? ent.getRegPostalCode() : "")
                .build();
                
        acraRepo.saveAcraToSeller(userUuid,jsonObject);
        acraRepo.addToSellerDB(userUuid, jsonObject);
    }

    public Boolean checkIfUserAddedUEN(String userId) {
        return acraRepo.checkIfUserAddedUEN(userId);
    }

    public Boolean checkIfUENregistered(String uen){
        return acraRepo.checkIfUserAddedUEN(uen);
    }
}
