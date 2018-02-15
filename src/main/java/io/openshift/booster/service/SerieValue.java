package io.openshift.booster.service;

import org.immutables.value.Value;

@Value.Immutable
@Value.Modifiable
public interface SerieValue {
    long getTime();
    long getValue();
}
