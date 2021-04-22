package com.dejim;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class AWSSignatureV2 {
	private static final String CHARACTER_ENCODING = "UTF-8";
	final static String ALGORITHM = "HmacSHA256";

	public AWSSignatureV2() {

	}
	
	public String generateSignature(String awsId, String awsSecret, String MWSAuthToken, String sellerId, String marketPlaceId1, String timestamp) throws Exception {
		String secretKey = awsSecret;

		String serviceUrl = "https://mws.amazonservices.com/Orders/2013-09-01";

		HashMap<String, String> parameters = new HashMap<String, String>();

		parameters.put("AWSAccessKeyId", urlEncode(awsId));
		parameters.put("Action", urlEncode("ListOrders"));
		parameters.put("MWSAuthToken", urlEncode(MWSAuthToken));
		parameters.put("SellerId", urlEncode(sellerId));
		parameters.put("SignatureMethod", urlEncode(ALGORITHM));
		parameters.put("SignatureVersion", urlEncode("2"));
		parameters.put("CreatedAfter", urlEncode("2021-04-21T05:00:00Z"));
		parameters.put("Timestamp", urlEncode(timestamp));
		parameters.put("MarketplaceId.Id.1", urlEncode(marketPlaceId1));
		parameters.put("Version", urlEncode("2013-09-01"));

		String formattedParameters = calculateStringToSignV2(parameters, serviceUrl);
		String signature = sign(formattedParameters, secretKey);
		
		return signature;
	}

	private static String calculateStringToSignV2(Map<String, String> parameters, String serviceUrl)
			throws SignatureException, URISyntaxException {

		Map<String, String> sorted = new TreeMap<String, String>();
		sorted.putAll(parameters);

		URI endpoint = new URI(serviceUrl.toLowerCase());

		StringBuilder data = new StringBuilder();
		data.append("POST\n");
		data.append(endpoint.getHost());
		data.append("\n/Orders/2013-09-01");
		data.append("\n");

		Iterator<Entry<String, String>> pairs = sorted.entrySet().iterator();
		while (pairs.hasNext()) {
			Map.Entry<String, String> pair = pairs.next();
			if (pair.getValue() != null) {
				data.append(pair.getKey() + "=" + pair.getValue());
			} else {
				data.append(pair.getKey() + "=");
			}

			if (pairs.hasNext()) {
				data.append("&");
			}
		}

		return data.toString();
	}

	private static String sign(String data, String secretKey)
			throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
		Mac mac = Mac.getInstance(ALGORITHM);
		mac.init(new SecretKeySpec(secretKey.getBytes(CHARACTER_ENCODING), ALGORITHM));
		byte[] signature = mac.doFinal(data.getBytes(CHARACTER_ENCODING));
		String signatureBase64 = new String(Base64.encodeBase64(signature), CHARACTER_ENCODING);
		
		System.out.println(signatureBase64);
		
		return new String(signatureBase64);
	}

	private static String urlEncode(String rawValue) {
		String value = (rawValue == null) ? "" : rawValue;
		String encoded = null;

		try {
			encoded = URLEncoder.encode(value, CHARACTER_ENCODING).replace("+", "%20").replace("*", "%2A")
					.replace("%7E", "~");
		} catch (UnsupportedEncodingException e) {
			System.err.println("Unknown encoding: " + CHARACTER_ENCODING);
			e.printStackTrace();
		}

		return encoded;
	}
}