/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openshift.booster;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.jayway.restassured.RestAssured;
import io.openshift.booster.service.ImmutableSerieValue;
import io.openshift.booster.service.MetricController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BoosterApplicationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private MetricController metricController;

    @Before
    public void beforeTest() {
        RestAssured.baseURI = String.format("http://localhost:%d/api/metric", port);
    }

    @Test
    public void testGetSerie() throws InterruptedException {
        metricController.addSerieValue("serie-to-get", ImmutableSerieValue.builder().time(0).value(10).build());
        metricController.addSerieValue("serie-to-get", ImmutableSerieValue.builder().time(1).value(12).build());
        metricController.addSerieValue("other-serie", ImmutableSerieValue.builder().time(100).value(-12).build());
        when().get("serie-to-get")
                .then()
                .statusCode(200)
                .body("serie.time", contains(0, 1))
                .body("serie.value", contains(10, 12));
        metricController.deleteSerie("serie-to-get");
        metricController.deleteSerie("other-serie");

    }

    @Test
    public void testGetSerieList() throws InterruptedException {
        metricController.addSerieValue("serie-to-get", ImmutableSerieValue.builder().time(0).value(10).build());
        metricController.addSerieValue("serie-to-get", ImmutableSerieValue.builder().time(1).value(12).build());
        metricController.addSerieValue("other-serie", ImmutableSerieValue.builder().time(100).value(-12).build());
        when().get()
                .then()
                .statusCode(200)
                .body("serieList", containsInAnyOrder("serie-to-get", "other-serie"));
        metricController.deleteSerie("serie-to-get");
        metricController.deleteSerie("other-serie");
    }

    @Test
    public void testPutPoint() throws InterruptedException {
        given()
                .body("{\"time\":123,\"value\":10}")
                .contentType("application/json")
        .when()
                .put("serie-to-put")
        .then()
                .statusCode(200);

        when().get("serie-to-put")
                .then()
                .statusCode(200)
                .body("serie.time", contains(123))
                .body("serie.value", contains(10));
        metricController.deleteSerie("serie-to-put");
    }



}
