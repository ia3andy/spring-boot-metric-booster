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

package io.openshift.booster.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.influxdb.impl.TimeUtil.fromInfluxDBTimeFormat;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.openshift.booster.ApplicationConfig;
import io.openshift.booster.exception.NotFoundException;
import io.openshift.booster.exception.StorageException;
import io.openshift.booster.exception.UnprocessableEntityException;
import io.openshift.booster.exception.UnsupportedMediaTypeException;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/metric")
public class MetricController {

    private static final String VALUE_FIELD_NAME = "value";
    private static final Predicate<String> ID_PREDICATE = Pattern.compile("[^\"]+").asPredicate();
    private final InfluxDB influxDB;
    private final String databaseName;

    @Autowired
    public MetricController(final ApplicationConfig applicationConfig) {
        this(applicationConfig.getDatabase());
    }


    MetricController(final DatabaseConfig databaseConfig) {
        this.influxDB = InfluxDBFactory.connect(databaseConfig.getUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
        this.databaseName = databaseConfig.getDatabase();
        initializeDatabaseIfNotExists();
    }

    @GetMapping()
    public SerieListResult getSeries() {
        final QueryResult queryResult = influxDB.query(new Query("SHOW SERIES", databaseName));
        System.out.println(queryResult);
        if(queryResult.hasError()){
            throw new StorageException(queryResult.getError());
        }

        if(queryResult.getResults().isEmpty()){
            throw  new NotFoundException("No serie found.");
        }

        final QueryResult.Result result = queryResult.getResults().get(0);

        if(result.hasError()){
            throw new StorageException(result.getError());
        }

        if(result.getSeries() == null || result.getSeries().isEmpty()){
            throw  new NotFoundException("No serie found.");
        }

        final List<String> names = result.getSeries().get(0).getValues().stream().map(s -> ((String) s.get(0))).collect(toList());
        return ImmutableSerieListResult.builder().serieList(names).build();
    }

    @GetMapping("/{id}")
    public SerieResult getSerie(@PathVariable("id") final String id) {
        verifyCorrectId(id);
        Query query = new Query("SELECT " + VALUE_FIELD_NAME + " FROM \"" + id + "\"", databaseName);
        final QueryResult queryResult = influxDB.query(query);
        if(queryResult.hasError()){
            throw new StorageException(queryResult.getError());
        }

        if(queryResult.getResults().isEmpty()){
            throw  new NotFoundException("Serie not found.");
        }

        final QueryResult.Result result = queryResult.getResults().get(0);

        if(result.hasError()){
            throw new StorageException(result.getError());
        }

        if(result.getSeries() == null || result.getSeries().isEmpty()){
            throw  new NotFoundException("Serie not found.");
        }

        final QueryResult.Series series = result.getSeries().get(0);
        final List<ImmutableSerieValue> serie = series.getValues().stream()
                .map(v -> ImmutableSerieValue.builder().time(parseTime(v)).value(parseValue(v)).build())
                .collect(toList());

        return ImmutableSerieResult.builder().addAllSerie(serie).build();
    }


    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    public void addSerieValue(@PathVariable("id") final String id, @RequestBody final ModifiableSerieValue serieValue) {
        addSerieValue(id, (SerieValue) serieValue);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}")
    public void deleteSerie(@PathVariable("id") final String id) {
        verifyCorrectId(id);
        Query query = new Query(" DROP SERIES FROM \"" + id + "\"", databaseName);
        influxDB.query(query);
    }

    public void addSerieValue(String id, final SerieValue serieValue) {
        verifyCorrectId(id);
        final SerieValue verifiedSerieValue = verifyCorrectPayload(serieValue);
        final Point dbPoint = Point.measurement(id).time(verifiedSerieValue.getTime(), MILLISECONDS).addField(VALUE_FIELD_NAME, verifiedSerieValue.getValue()).build();
        influxDB.write(databaseName, "", dbPoint);
    }


    void deleteDatabase(){
        influxDB.deleteDatabase(databaseName);
    }

    private void verifyCorrectId(final String id) {
        if (Objects.isNull(id)) {
            throw new UnsupportedMediaTypeException("id cannot be null.");
        }
        if(!ID_PREDICATE.test(id)){
            throw new UnprocessableEntityException("id syntax is not valid.");
        }
    }

    private SerieValue verifyCorrectPayload(SerieValue serieValue) {
        if (Objects.isNull(serieValue)) {
            throw new UnsupportedMediaTypeException("SerieValue cannot be null.");
        }
        return ImmutableSerieValue.copyOf(serieValue);
    }

    private void initializeDatabaseIfNotExists() {
        if(!influxDB.databaseExists(databaseName)){
            influxDB.createDatabase(databaseName);
            addSerieValue("cpu", ImmutableSerieValue.builder().time(System.currentTimeMillis() - 5000).value(10).build());
            addSerieValue("cpu", ImmutableSerieValue.builder().time(System.currentTimeMillis()).value(12).build());
            addSerieValue("memory", ImmutableSerieValue.builder().time(System.currentTimeMillis()).value(5).build());
        }
    }

    private long parseValue(final List<Object> v) {
        return ((Double) v.get(1)).longValue();
    }

    private long parseTime(final List<Object> v) {
        return fromInfluxDBTimeFormat((String) v.get(0));
    }

}