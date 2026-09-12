package com.infinity.service;

import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinity.constants.CollectConstants;
import com.vTransact.upi.service.EncryptionMainClass;

@Service
public class CollectService {

	@Value("${collect.url}")
	private String collectUrl;

	public String processCollect(String virtualAddress) {

		EncryptionMainClass encdec = new EncryptionMainClass();

		String pspRefNo = UUID.randomUUID().toString().replace("-", "").substring(0, 20);

		// Random amount between 20.00 and 2000.00
		Random random = new Random();

		int amountInPaise = 2000 + random.nextInt(198001);

		String amount = String.format("%.2f", amountInPaise / 100.0);

		// ==========================================
		// Create original JSON
		// ==========================================

		String jsonRequest = "{" + "\"requestInfo\": {" + "\"pgMerchantId\": \"" + CollectConstants.PG_MERCHANT_ID
				+ "\"," + "\"pspRefNo\": \"" + pspRefNo + "\"" + "}," + "\"addInfo\": {" + "\"addInfo10\": \"NA\","
				+ "\"addInfo9\": \"NA\"" + "}," + "\"amount\": \"" + amount + "\"," + "\"transactionNote\": \"UPI\","
				+ "\"expiryTime\": \"1200\"," + "\"txnId\": \" \"," + "\"upiTransRefNo\": \"140005500606\","
				+ "\"payerType\": {" + "\"virtualAddress\": \"" + virtualAddress + "\"," + "\"name\": \"CollectSim\""
				+ "}" + "}";

		try {

			// ==========================================
			// Encrypt original JSON
			// ==========================================

			String encJson = encdec.encrypt(jsonRequest, CollectConstants.MERCHANT_KEY);

			System.out.println("=================================");
			System.out.println("PSP REF NO        : " + pspRefNo);
			System.out.println("AMOUNT            : " + amount);
			System.out.println("VPA               : " + virtualAddress);
			System.out.println("REQUEST JSON      : " + jsonRequest);
			System.out.println("ENCRYPTED REQUEST : " + encJson);

			// ==========================================
			// Create JSON expected by external URL
			// ==========================================

			String postJson = "{" + "\"requestMsg\": \"" + encJson + "\"," + "\"pgMerchantId\": \""
					+ CollectConstants.PG_MERCHANT_ID + "\"" + "}";

			System.out.println("POST JSON         : " + postJson);
			System.out.println("POST URL          : " + collectUrl);

			// ==========================================
			// POST request
			// ==========================================

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();

			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> requestEntity = new HttpEntity<>(postJson, headers);

			ResponseEntity<String> response = restTemplate.postForEntity(collectUrl, requestEntity, String.class);

			// ==========================================
			// Get response JSON
			// ==========================================

			String responseBody = response.getBody();

			System.out.println("HTTP STATUS       : " + response.getStatusCode());

			System.out.println("FULL RESPONSE     : " + responseBody);

			// ==========================================
			// Extract "resp" from response JSON
			// ==========================================

			ObjectMapper objectMapper = new ObjectMapper();

			JsonNode responseJson = objectMapper.readTree(responseBody);

			JsonNode respNode = responseJson.get("resp");

			if (respNode == null || respNode.isNull()) {

				System.out.println("RESP IS NULL OR NOT PRESENT");

				return responseBody;
			}

			String encryptedResp = respNode.asText();

			System.out.println("ENCRYPTED RESP    : " + encryptedResp);

			// ==========================================
			// Decrypt ONLY resp
			// ==========================================

			String decryptedResponse = encdec.decrypt(encryptedResp, CollectConstants.MERCHANT_KEY);

			System.out.println("DECRYPTED RESPONSE: " + decryptedResponse);

			System.out.println("=================================");

			// Return decrypted resp to Postman
			return decryptedResponse;

		} catch (Exception e) {

			e.printStackTrace();

			return "Error while processing request: " + e.getMessage();
		}
	}
}