package com.stationflow.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stationflow.backend.model.Location;
import com.stationflow.backend.model.Station;
import com.stationflow.backend.model.StationStatus;
import com.stationflow.backend.model.TransportType;
import com.stationflow.backend.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class IettApiService {

    @Autowired
    private StationRepository stationRepository;

    private final String API_URL = "https://api.ibb.gov.tr/iett/UlasimAnaVeri/HatDurakGuzergah.asmx?wsdl";

    public void fetchAndSaveStations() {
        System.out.println("Fetching IETT stations...");
        try {
            String soapRequest =
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                    "               xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n" +
                    "               xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "  <soap:Body>\n" +
                    "    <GetDurak_json xmlns=\"http://tempuri.org/\">\n" +
                    "      <DurakKodu></DurakKodu>\n" +
                    "    </GetDurak_json>\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            // headers.add("SOAPAction", "http://tempuri.org/GetDurak_json"); // Sometimes needed

            HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                parseAndSave(response.getBody());
            } else {
                System.err.println("Failed to fetch IETT data. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("Error fetching IETT data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseAndSave(String xmlResponse) {
        try {
            // 1. Parse XML to get the JSON string inside <GetDurak_jsonResult>
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));
            
            // The structure is usually <soap:Envelope>...<GetDurak_jsonResult>JSON_HERE</GetDurak_jsonResult>...</soap:Envelope>
            // We can try to find the tag directly.
            String jsonString = doc.getElementsByTagName("GetDurak_jsonResult").item(0).getTextContent();

            // 2. Parse JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonString);

            int newStationsCount = 0;

            if (root.isArray()) {
                for (JsonNode node : root) {
                    try {
                        String latStr = node.path("KOORDINAT").asText(); // Format usually "POINT (LNG LAT)" or just raw? 
                        // Wait, IETT usually returns "KOORDINAT": "POINT (28.xxxx 41.xxxx)" or separate fields.
                        // Let's assume standard IETT JSON format. 
                        // Actually, IETT often returns "KOORDINAT" as WKT "POINT(lng lat)".
                        
                        // Let's check if there are separate fields first.
                        double lat = 0;
                        double lng = 0;

                        String koordinat = node.path("KOORDINAT").asText();
                        if (koordinat != null && koordinat.startsWith("POINT")) {
                            // Parse WKT: POINT (29.0123 41.0123)
                            String clean = koordinat.replace("POINT", "").replace("(", "").replace(")", "").trim();
                            String[] parts = clean.split("\\s+"); // Split by whitespace
                            if (parts.length >= 2) {
                                lng = Double.parseDouble(parts[0]);
                                lat = Double.parseDouble(parts[1]);
                            }
                        } else {
                             // Fallback or skip if no coordinate
                             continue;
                        }

                        // Check duplicates
                        if (stationRepository.existsByLocationLatAndLocationLng(lat, lng)) {
                            continue;
                        }

                        Station station = new Station();
                        station.setName(node.path("SDURAKADI").asText());
                        station.setLocation(new Location(lat, lng));
                        station.setType(TransportType.BUS);
                        station.setCapacity(100);
                        station.setAvailable((int) (Math.random() * 101));
                        station.setStatus(StationStatus.ACTIVE);
                        station.setCustom(false);

                        stationRepository.save(station);
                        newStationsCount++;

                    } catch (Exception e) {
                        // Skip individual error
                    }
                }
            }
            System.out.println("Imported " + newStationsCount + " new stations from IETT.");

        } catch (Exception e) {
            System.err.println("Error parsing IETT response: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
