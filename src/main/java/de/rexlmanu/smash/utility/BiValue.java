package de.rexlmanu.smash.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class BiValue<A, B> {

    private A first;
    private B second;

}
