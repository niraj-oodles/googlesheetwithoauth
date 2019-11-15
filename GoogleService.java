package com.qcclub.booking.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.qcclub.booking.domain.User;
import com.qcclub.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleService {

    @Autowired
    UserRepository userRepository;

    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    @Value("${spring.social.google.app-id}")
    private String googleId;
    @Value("${spring.social.google.app-secret}")
    private String googleSecret;

    @Value("${client.file}")
    Resource resource;

    private GoogleConnectionFactory createGoogleConnection() {
        return new GoogleConnectionFactory(googleId, googleSecret);
    }


    public AccessGrant getAuthorizationCode(String code, String URI) {
        return createGoogleConnection().getOAuthOperations().exchangeForAccess(code, URI, null
        );
    }


    public void writeData(String authorizationCode) throws IOException {

        Google google = new GoogleTemplate(authorizationCode);

        String name = google.getAccessToken();

        List<User> users = userRepository.findAll();
        GoogleCredential credential = new GoogleCredential().setAccessToken(name).createScoped(SCOPES);
        GoogleClientRequestInitializer keyInitializer = new CommonGoogleClientRequestInitializer();
        ValueRange requestBody = new ValueRange();

        List<String>[] listArray = new ArrayList[users.size()];
        int i = 0;
        for (User user : users) {
            List userDetails = new ArrayList<>();
            userDetails.add(user.getUsername());
            userDetails.add(user.getFirstName());
            userDetails.add(user.getLastName());
            listArray[i++] = userDetails;
        }
        List<List<Object>> arrayList= new ArrayList<>();
        for(List list:listArray)
        {
            arrayList.add(list);
        }
        requestBody.setValues(arrayList);



        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("qccsheet").build();
        Sheets.Spreadsheets.Values.Append request =
                sheets.spreadsheets().values().append("1AxvdsITY5WwR_8uCh1eCwXtiUScjQTPFtceVHiuxwKY", "A2:E", requestBody);
        request.setValueInputOption("USER_ENTERED");
        request.setInsertDataOption("INSERT_ROWS");

        AppendValuesResponse response = request.execute();
    }

    public List<List<Object>> getUserProfile(String authorizationCode) throws IOException {

        Google google = new GoogleTemplate(authorizationCode);

        String name = google.getAccessToken();
        // GoogleClientSecrets secrets= GoogleClientSecrets.load(JSON_FACTORY,new InputStreamReader(resource.getInputStream()));
        //Credential credential = GoogleCredential.fromStream(resource.getInputStream()).createScoped(SCOPES);
        GoogleCredential credential = new GoogleCredential().setAccessToken(name).createScoped(SCOPES);
        GoogleClientRequestInitializer keyInitializer = new CommonGoogleClientRequestInitializer();
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("qccsheet").build();
        ValueRange response = sheets.spreadsheets().values().get("1AxvdsITY5WwR_8uCh1eCwXtiUScjQTPFtceVHiuxwKY", "A2:E").execute();
        return response.getValues();


    }



    public Spreadsheet createSheet(String authorizationCode) throws IOException {

        Google google= new GoogleTemplate(authorizationCode);
        String name=google.getAccessToken();
        GoogleCredential credential = new GoogleCredential().setAccessToken(name).createScoped(SCOPES);
        GoogleClientRequestInitializer keyInitializer = new CommonGoogleClientRequestInitializer();
        Spreadsheet requestBody = new Spreadsheet();
        SpreadsheetProperties spreadsheetProperties= new SpreadsheetProperties();
        spreadsheetProperties.setTitle("EventTest1111");
        requestBody.setProperties(spreadsheetProperties);

        Sheets sheetsService =new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("qccsheet").build();
        Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody);

        Spreadsheet response = request.execute();
        return response;


    }


}
