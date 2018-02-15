package io.openshift.booster.service;

import java.util.List;

import org.immutables.value.Value;

@Value.Immutable
public interface SerieResult {
    List<SerieValue> getSerie();
}
