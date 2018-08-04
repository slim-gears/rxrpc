package com.slimgears.rxrpc.core.data;

import com.google.auto.value.AutoValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AutoValue
public abstract class Path {
    private final static Pattern PATH_PATTERN = Pattern.compile("(?<head>\\w+)(?<tail>(/\\w+)+)");
    public abstract String head();
    public abstract String tail();

    public static Path of(String path) {
        Matcher matcher = PATH_PATTERN.matcher(path);
        if (!matcher.matches()) {
            throw new RuntimeException("Invalid path: " + path);
        }
        return new AutoValue_Path(matcher.group("head"), matcher.group("tail"));
    }
}
