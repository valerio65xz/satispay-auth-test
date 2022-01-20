package com.satispay.auth.satispayauthtest.controller;

import com.satispay.auth.satispayauthtest.exception.ErrorCode;
import com.satispay.auth.satispayauthtest.exception.SatispayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

/**
 * The MainController class is used to create a Signed HTTP message (GET or POST) to providing a signature
 * for the Satispay Signature Test API.
 */
@RestController
@RequestMapping(value = "/satispay", produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
public class SatispayController {

    @Value("${satispay.private_key_path}")
    private String privateKeyPath;

    @Value("${satispay.test_signature_url}")
    private String testSignatureUrl;

    @Value("${satispay.key_id}")
    private String keyId;

    @Value("${satispay.algorithm}")
    private String algorithm;

    /**
     * It signs a GET or POST HTTP message and tests Satispay Signature API.
     * @param type The type of the HTTP request (GET or POST)
     * @param body An optional body. In case of GET, the body will be ignored
     * @return The API signature test response
     * @throws SatispayException An exception from the signature process or Http request
     */
    @GetMapping(value = "/authenticate")
    public ResponseEntity<String> authenticate(@NotNull @RequestParam(value = "type") String type, @Nullable @RequestBody String body) throws SatispayException {

        if ((!type.equals("GET")) && (!type.equals("POST"))){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'type' parameter MUST be GET or POST");
        }

       if (type.equals("GET") || body == null){
            body = "";
        }

        String digest;
        try {
            digest = new String(
                    Base64.getEncoder().encode(
                            MessageDigest.getInstance("SHA-256").digest(
                                    parseBody(body)
                            )
                    )
            );
        } catch (NoSuchAlgorithmException e) {
            throw new SatispayException(e.getMessage(), e, ErrorCode.DIGEST_EXCEPTION);
        }

        String dateFormatted = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        String signature = createSignatureString(dateFormatted, digest, type.toLowerCase());
        String encodedSignature = encodeSignature(signature);
        return ok(executeHttpRequest(dateFormatted, digest, encodedSignature, type, parseBody(body)));
    }

    private byte[] parseBody(String body){
        if (System.getProperty("os.name").contains("Windows")){
            return body.replaceAll("\r", "").getBytes(StandardCharsets.UTF_8);
        }
        else if (System.getProperty("os.name").contains("Mac")){
            return body.replaceAll("\r", "\n").getBytes(StandardCharsets.UTF_8);
        }
        else return body.getBytes(StandardCharsets.UTF_8);
    }

    private String createSignatureString(String dateFormatted, String digest, String type){
        return "(request-target): "+type+" /wally-services/protocol/tests/signature\n"+
                "host: staging.authservices.satispay.com\n"+
                "date: "+dateFormatted+"\n"+
                "digest: SHA-256="+digest;
    }

    private String encodeSignature(String signatureString) throws SatispayException {
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Signature signature = Signature.getInstance("SHA256withRSA");

            String privateKeyString = new String(Files.readAllBytes(new File(privateKeyPath).toPath()), Charset.defaultCharset());
            String parsedPrivateKey = privateKeyString.replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("\n", "")
                    .replaceAll("\r", "");

            byte[] decodedPrivateKey = Base64.getDecoder().decode(parsedPrivateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedPrivateKey);

            signature.initSign(keyFactory.generatePrivate(spec));
            signature.update(signatureString.getBytes(StandardCharsets.UTF_8));

            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            throw new SatispayException(e.getMessage(), e, ErrorCode.SIGNATURE_EXCEPTION);
        }
    }

    private String executeHttpRequest(String dateFormatted, String digest, String encodedSignature, String method, byte[] byteBody) throws SatispayException {
        try{
            URL url = new URL(testSignatureUrl);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            http.setRequestProperty("content-type", "application/json");
            http.setRequestProperty("host", "staging.authservices.satispay.com");
            http.setRequestProperty("date", dateFormatted);
            http.setRequestProperty("digest", "SHA-256=" + digest);
            http.setRequestProperty("Authorization", "Signature keyId=\""+keyId+"\", algorithm=\""+algorithm+"\", headers=\"(request-target) host date digest\", signature=\""+encodedSignature+"\"");

            if(byteBody.length>0 && method.equals("POST")){
                http.getOutputStream().write(byteBody);
            }

            String response = readHttpResponse(http);

            http.disconnect();

            return response;
        } catch (IOException e) {
            throw new SatispayException(e.getMessage(), e, ErrorCode.HTTP_EXCEPTION);
        }

    }

    private String readHttpResponse(HttpURLConnection http) throws IOException {
        return new BufferedReader(new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

}