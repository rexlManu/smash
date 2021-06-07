package de.rexlmanu.smash.utility.replace;

public class ParameterModifier {

    public static ParameterModifier of(String input) {
        return new ParameterModifier(input);
    }

    private String input;

    private ParameterModifier(String input) {
        this.input = input;
    }

    public ParameterModifier replace(String modifier, String value) {
        this.input = this.input.replace(modifier, value);
        return this;
    }

    public String build() {
        return this.toString();
    }

    @Override
    public String toString() {
        return this.input;
    }
}
