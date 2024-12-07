package edu.example.wayfarer.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.example.wayfarer.exception.GeocodingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GeocodingServiceImpl implements GeocodingService {

    // Google API 키를 application.properties 에서 주입받음
    @Value("${google.api.key}")
    private String apiKey;

    // RestTemplate 객체를 생성해서 HTTP 요청 수행
    private final RestTemplate restTemplate;

    /**
     * 주소를 받아서 위도,경도로 반환하는 메서드
     * 
     * @param address 변환하려는 주소 문자열
     * @return String 위도 경도를 포함한 문자열, 또는 에러메시지
     */
    @Override
    public String geocoding(String address) {
        // Google Maps Geocoding API 요청 URL
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + apiKey;

        // API 응답 JSON 검증 및 가져오기
        JsonObject jsonObject = getResponse(url);

        // 응답 상태가 "OK" 일 경우
        // JSON 응답 데이터에서 geometry.location 정보 추출
        JsonObject location = jsonObject
                .getAsJsonArray("results")
                .get(0)
                .getAsJsonObject()
                .get("geometry")
                .getAsJsonObject()
                .get("location")
                .getAsJsonObject();

        // location 에서 위도 값 추출
        double lat = location.get("lat").getAsDouble();
        // location 에서 경도 값 추출
        double lng = location.get("lng").getAsDouble();
            
        // 결과값을 문자열로 반환
        return "Latitude: " + lat + ", Longitude: " + lng;
    }

    /**
     * 위도,경도를 받아서 주소로 반환하는 메서드
     *
     * @param lat 위도
     * @param lng 경도
     * @return String 주소, 또는 에러 메시지
     */
    @Override
    public String reverseGeocoding(double lat, double lng) {
        // Google Maps Geocoding API 요청 URL
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + lat + "," + lng + "&key=" + apiKey+ "&language=ko";

        // API 응답 JSON 검증 및 가져오기
        JsonObject jsonObject = getResponse(url);


        // 응답 상태가 "OK" 일 경우
        // JSON 응답 데이터에서 results 배열의 첫번째 객체 formatted_address 값을 반환
        //  - 반환할 주소 형식에 따라서 추후 수정 예정
        return jsonObject
                .getAsJsonArray("results")
                .get(0)
                .getAsJsonObject()
                .get("formatted_address")
                .getAsString();
    }

    /**
     * API 요청 결과를 검증하고 JSON 파싱 결과 반환 메서드
     * @param url API 요청 url
     * @return JsonObject API 응답 JSON 객체
     */
    private JsonObject getResponse(String url) {
        // RestTemplate 를 사용해 Google API 요청 후 JSON 응답 데이터 가져오기
        String response = restTemplate.getForObject(url, String.class);

        // 응답이 null 일 경우 예외
        if (response == null) {
            throw GeocodingException.NULL_RESPONSE.get();
        }

        // Json 응답을 파싱하여 JsonObject 로 변환
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        // 응답 상태가 "OK"가 아닐 경우 상태코드 반환
        String status = jsonObject.get("status").getAsString();
        if (!"OK".equals(status)) {
            switch (status) {
                case "INVALID_REQUEST":
                    throw GeocodingException.INVALID_REQUEST.get();
                case "REQUEST_DENIED":
                    throw GeocodingException.REQUEST_DENIED.get();
                case "OVER_QUERY_LIMIT":
                    throw GeocodingException.OVER_QUERY_LIMIT.get();
                case "ZERO_RESULTS":
                    throw GeocodingException.ZERO_RESULTS.get();
                default:
                    throw GeocodingException.UNKNOWN_ERROR.get();
            }
        }
        //  응답 상태가 "OK"일 경우 응답 JSON 객체 반환
        return jsonObject;
    }
}
