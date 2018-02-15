package io.openshift.booster.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MetricControllerIT {

    private MetricController metricController;

    @Before
    public void setUp() throws Exception {
        metricController = new MetricController(new DatabaseConfig("influx-db-access-andy-booster.192.168.64.2.nip.io", "dev", "pass", "test-metric"));
    }

    @Test
    public void shouldCorrectlyAddPointToSerie() throws Exception {
        for (int i = 0; i < 100; i++) {
            metricController.addSerieValue("my-serie", ImmutableSerieValue.builder().time(System.currentTimeMillis()).value(RandomUtils.nextLong(0, 10000000L)).build());
        }
        final SerieListResult serieListResult = metricController.getSeries();
        assertThat(serieListResult.getSerieList()).containsExactly("my-serie");
        final SerieResult serie = metricController.getSerie("my-serie");
        assertThat(serie.getSerie())
                .hasSize(100);
        metricController.deleteSerie("my-serie");
    }

    @After
    public void tearDown() throws Exception {
        metricController.deleteDatabase();
    }
}