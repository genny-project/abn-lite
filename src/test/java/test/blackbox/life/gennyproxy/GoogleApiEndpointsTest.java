package test.blackbox.life.gennyproxy;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class GoogleApiEndpointsTest {

    private static String accessToken;

    @BeforeAll
    public static void beforeALL() throws IOException {
        String response =  given()
                .contentType(ContentType.URLENC)
                .port(8180)
                .log().all()
                .when()
                .body("username=alice&password=alice&grant_type=password&client_id=backend-service&client_secret=secret&scope=openid")
                .header("content-type", "application/x-www-form-urlencoded")
                .post("/auth/realms/internmatch/protocol/openid-connect/token")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .body()
                .asString();


        JSONObject json = new JSONObject(response);
        System.out.println(json.get("access_token"));
        accessToken = (String)json.get("access_token");

    }

    @Test
    public void retrieveGoogleMapApi_passNoParameter_return200() {
        //https://maps.googleapis.com/maps/api/js?key=XXXXX&libraries=places,drawing
        given()
          .log().all()
          .when()
                .header("Authorization","Bearer "+accessToken )
                .get("/googleapi/v1/map")
          .then()
                .log().all()
             .statusCode(200)
             .body(
               containsString("google.maps.Load = function(apiLoad)")
             );
    }

    @Test
    public void retrieveGoogleTimeZoneApi_passValidParameter_return200() {
        //http://localhost:8081/googleapi/v1/timezone?location=-37.913151%2C145.262253&timestamp=1458000000
        String response =  given()
                .log().all()
                .when()
                .header("Authorization","Bearer "+accessToken )
                .param("location", "-37.913151,145.262253")
                .param("timestamp", 1458000000)
                .get("/googleapi/v1/timezone")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .body()
                .asString();
         assertEquals("Australia/Melbourne",response);


    }

    @Test
    public void retrieveGoogleAddressApi_passValidParameter_return200() {
         String response =  given()
                .log().all()
                .when()
                .header("Authorization","Bearer "+accessToken )
                .param("address", "14 Durham Place, Clayton South")
                .get("/googleapi/v1/geocode")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .body()
                .asString();

    }

}