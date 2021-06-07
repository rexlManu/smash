package de.rexlmanu.smash.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = AccessLevel.MODULE)
@Getter
@Accessors(fluent = true)
public class ConfigurationEntry<T> {

    private String name;
    private T value;

}
