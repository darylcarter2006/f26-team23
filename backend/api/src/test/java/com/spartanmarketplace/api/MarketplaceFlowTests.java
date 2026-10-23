package com.spartanmarketplace.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Walks the main use cases end to end against a real (embedded) Postgres.
@SpringBootTest(properties = {"app.admin-emails=boss@campus.edu"})
@AutoConfigureMockMvc
class MarketplaceFlowTests {

    private static final EmbeddedPostgres POSTGRES;

    static {
        try {
            POSTGRES = EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;

    private JsonNode send(MockHttpServletRequestBuilder req, String body, String adminId, int expected)
            throws Exception {
        if (body != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        if (adminId != null) {
            req.header("X-Admin-Id", adminId);
        }
        MvcResult result = mvc.perform(req).andExpect(status().is(expected)).andReturn();
        String text = result.getResponse().getContentAsString();
        return text.isBlank() ? null : json.readTree(text);
    }

    private String user(String email) throws Exception {
        JsonNode u = send(post("/api/users"), "{\"email\":\"" + email + "\",\"displayName\":\"" + email + "\"}",
                null, 201);
        String id = u.get("id").asText();
        send(post("/api/users/" + id + "/verify-edu"), null, null, 200);
        return id;
    }

    @Test
    void marketplaceEndToEnd() throws Exception {
        String admin = user("boss@campus.edu");
        assertEquals("ADMIN", send(get("/api/users/" + admin), null, null, 200).get("role").asText());
        String owner = user("owner@campus.edu");
        String buyer = user("buyer@campus.edu");

        // validation and error codes
        send(post("/api/users"), "{\"email\":\"\"}", null, 400);
        send(post("/api/users"), "{\"email\":\"x@gmail.com\"}", null, 400);
        send(post("/api/users"), "{\"email\":\"owner@campus.edu\"}", null, 409);
        send(get("/api/users/00000000-0000-0000-0000-000000000000"), null, null, 404);
        send(get("/api/users"), null, owner, 403);
        send(get("/api/users"), null, null, 400);

        // admin-managed catalog data
        JsonNode cat = send(post("/api/categories"), "{\"name\":\"Food\"}", admin, 201);
        String catId = cat.get("id").asText();
        JsonNode spot = send(post("/api/meetup-locations"), "{\"name\":\"Student Union\"}", admin, 201);

        // vendor application -> admin approval
        String app = send(post("/api/vendor-applications"), "{\"userId\":\"" + owner
                + "\",\"businessName\":\"Cookie Co\",\"whatSelling\":\"cookies\",\"studentId\":\"123\"}",
                null, 201).get("id").asText();
        send(post("/api/vendor-applications"), "{\"userId\":\"" + owner
                + "\",\"businessName\":\"Dup\",\"whatSelling\":\"x\",\"studentId\":\"1\"}", null, 409);
        send(put("/api/vendor-applications/" + app + "/approve"), null, owner, 403);
        send(put("/api/vendor-applications/" + app + "/approve"), null, admin, 200);
        String vendor = send(get("/api/vendors"), null, null, 200).get(0).get("id").asText();

        // product with stock 2
        send(post("/api/products"), "{\"actorId\":\"" + buyer + "\",\"vendorId\":\"" + vendor
                + "\",\"title\":\"Cookie\",\"price\":300,\"stockCount\":2}", null, 403);
        send(post("/api/products"), "{\"actorId\":\"" + owner + "\",\"vendorId\":\"" + vendor
                + "\",\"title\":\"\",\"price\":-1,\"stockCount\":2}", null, 400);
        String product = send(post("/api/products"), "{\"actorId\":\"" + owner + "\",\"vendorId\":\"" + vendor
                + "\",\"title\":\"Cookie\",\"description\":\"choc chip\",\"price\":300,\"stockCount\":2,"
                + "\"categoryId\":\"" + catId + "\",\"photoUrls\":[\"a.png\"]}", null, 201).get("id").asText();
        assertEquals(1, send(get("/api/products?categoryId=" + catId + "&maxPrice=500&sort=priceAsc"),
                null, null, 200).size());

        // request to buy holds stock immediately; reject releases it
        String hold = send(post("/api/products/" + product + "/buy-requests"),
                "{\"buyerId\":\"" + buyer + "\",\"quantity\":2}", null, 201).get("id").asText();
        assertEquals(0, send(get("/api/products/" + product), null, null, 200).get("stockCount").asInt());
        send(post("/api/products/" + product + "/buy-requests"), "{\"buyerId\":\"" + buyer + "\"}", null, 409);
        send(put("/api/buy-requests/" + hold + "/reject"), "{\"actorId\":\"" + owner + "\"}", null, 200);
        assertEquals(2, send(get("/api/products/" + product), null, null, 200).get("stockCount").asInt());

        // accept -> transaction -> meetup -> confirmations -> completed
        String req = send(post("/api/products/" + product + "/buy-requests"),
                "{\"buyerId\":\"" + buyer + "\"}", null, 201).get("id").asText();
        JsonNode accepted = send(put("/api/buy-requests/" + req + "/accept"),
                "{\"actorId\":\"" + owner + "\"}", null, 200);
        String tx = accepted.get("transactionId").asText();
        assertNotNull(tx);
        send(post("/api/transactions/" + tx + "/reviews"),
                "{\"reviewerId\":\"" + buyer + "\",\"rating\":5}", null, 409);
        send(post("/api/transactions/" + tx + "/confirm-intent"), "{\"actorId\":\"" + buyer + "\"}", null, 409);
        send(post("/api/transactions/" + tx + "/meetup"), "{\"actorId\":\"" + buyer + "\"}", null, 400);
        send(post("/api/transactions/" + tx + "/meetup"), "{\"actorId\":\"" + buyer + "\",\"meetupLocationId\":\""
                + spot.get("id").asText() + "\"}", null, 200);
        send(post("/api/transactions/" + tx + "/confirm-intent"), "{\"actorId\":\"" + buyer + "\"}", null, 200);
        send(post("/api/transactions/" + tx + "/confirm-intent"), "{\"actorId\":\"" + owner + "\"}", null, 200);
        send(post("/api/transactions/" + tx + "/confirm-sale"), "{\"actorId\":\"" + buyer + "\"}", null, 200);
        JsonNode done = send(post("/api/transactions/" + tx + "/confirm-sale"),
                "{\"actorId\":\"" + owner + "\"}", null, 200);
        assertEquals("COMPLETED", done.get("status").asText());

        // review, vendor reply, moderation
        String review = send(post("/api/transactions/" + tx + "/reviews"),
                "{\"reviewerId\":\"" + buyer + "\",\"rating\":2,\"comment\":\"stale\"}", null, 201)
                .get("id").asText();
        send(post("/api/transactions/" + tx + "/reviews"),
                "{\"reviewerId\":\"" + buyer + "\",\"rating\":2}", null, 409);
        send(post("/api/reviews/" + review + "/replies"),
                "{\"authorId\":\"" + buyer + "\",\"body\":\"hi\"}", null, 403);
        JsonNode replied = send(post("/api/reviews/" + review + "/replies"),
                "{\"authorId\":\"" + owner + "\",\"body\":\"sorry about that\"}", null, 201);
        assertEquals("sorry about that", replied.get("reply").get("body").asText());
        assertEquals(1, send(get("/api/reviews"), null, admin, 200).size());

        JsonNode stats = send(get("/api/vendors/" + vendor + "/statistics?actorId=" + owner), null, null, 200);
        assertEquals(1, stats.get("completedTransactions").asInt());
        assertEquals(1, stats.get("reviewCount").asInt());

        // listing: pending until approved, then sold
        String listing = send(post("/api/listings"), "{\"sellerId\":\"" + buyer
                + "\",\"title\":\"Textbook\",\"price\":2000}", null, 201).get("id").asText();
        assertEquals(0, send(get("/api/listings"), null, null, 200).size());
        send(put("/api/listings/" + listing + "/approve"), null, admin, 200);
        assertEquals(1, send(get("/api/listings"), null, null, 200).size());
        send(put("/api/listings/" + listing + "/sold?userId=" + buyer), null, null, 200);
        assertEquals(0, send(get("/api/listings"), null, null, 200).size());

        // messaging and reports
        String convo = send(post("/api/conversations"),
                "{\"userId\":\"" + buyer + "\",\"otherUserId\":\"" + owner + "\"}", null, 201).get("id").asText();
        send(post("/api/conversations/" + convo + "/messages"),
                "{\"senderId\":\"" + buyer + "\",\"body\":\"see you at 3\"}", null, 201);
        assertEquals(1, send(get("/api/conversations/" + convo + "/messages?userId=" + owner),
                null, null, 200).size());
        String report = send(post("/api/reports"), "{\"reporterId\":\"" + buyer + "\",\"conversationId\":\""
                + convo + "\",\"reason\":\"spam\"}", null, 201).get("id").asText();
        send(put("/api/reports/" + report), "{\"status\":\"ACTIONED\"}", admin, 200);
        assertEquals(1, send(get("/api/users/" + owner + "/reports"), null, admin, 200).size());

        // ban, hide, statistics, deletes
        send(put("/api/users/" + buyer + "/status"), "{\"status\":\"BANNED\"}", admin, 200);
        send(post("/api/listings"), "{\"sellerId\":\"" + buyer + "\",\"title\":\"x\",\"price\":1}", null, 403);
        send(put("/api/vendors/" + vendor + "/hide"), null, admin, 200);
        assertEquals(0, send(get("/api/products"), null, null, 200).size());
        assertEquals(3, send(get("/api/statistics"), null, admin, 200).get("totalUsers").asInt());
        send(delete("/api/reviews/" + review), null, admin, 204);
        send(delete("/api/reviews/" + review), null, admin, 404);
        send(delete("/api/categories/" + catId), null, admin, 409);
    }
}
