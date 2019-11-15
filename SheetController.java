package com.qcclub.booking.controller;


import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.qcclub.booking.constants.UrlMappings;
import com.qcclub.booking.service.GoogleService;
import com.qcclub.booking.util.ResponseDTO;
import com.qcclub.booking.util.ResponseGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping(UrlMappings.SHEET)
public class SheetController {


    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private LocalTime localTime = LocalTime.now();

    private LocalTime sessionTime;

    private String accessToken;

    @Autowired
    private GoogleService googleService;

    @GetMapping
    public RedirectView googleLogin() {
        String clientId = "610923772307-cqdgp25mtfhg7bdon2va2rdol24foi3m.apps.googleusercontent.com";
        String redirectUri = "http://localhost:8080/api/v1/sheet/googleprofiledata";
        String scope = "https://www.googleapis.com/auth/spreadsheets";
        String accessType = "offline";
        String state = "asdafwswdwefwsdg";
        String responseType = "code";
        String url = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + clientId + "&redirect_uri=" + redirectUri + "&scope=" + scope + "&access_type=" + accessType + "&state=" + state + "&response_type=" + responseType;
        RedirectView redirectView = new RedirectView(url);
        return redirectView;

    }


    @GetMapping("/googleprofiledata")
    public String accessToken(@RequestParam("code") String code) {
        String uri = "http://localhost:8080/api/v1/sheet/googleprofiledata";
        AccessGrant accessGrant = googleService.getAuthorizationCode(code, uri);
        sessionTime = localTime.plusSeconds(accessGrant.getExpireTime());
        accessToken = accessGrant.getAccessToken();
        return "Authenticated Successfully";

    }


    @GetMapping("/sheetData")
    public List<List<Object>> googleprofiledata() throws IOException {


        return googleService.getUserProfile(accessToken);


    }




    @ResponseBody
    @PostMapping("/addUserToSheet")
    public ResponseEntity<ResponseDTO<Void>> addUserToSheet() throws IOException {
        googleService.writeData(accessToken);
        return ResponseGenerator.generateSuccessResponse(null);
    }


    @GetMapping("/createSheet")
    public ResponseEntity<ResponseDTO<Spreadsheet>> createSheet() throws IOException {
       Spreadsheet spreadsheet= googleService.createSheet(accessToken);
       return ResponseGenerator.generateSuccessResponse(spreadsheet);
    }


}




