package com.slimgears.rxrpc.apt.util;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class OptionsUtils {
    public static ImmutableMap<String, String> toOptions(String... options) {
        return toOptions(Arrays.asList(options));
    }

    public static ImmutableMap<String, String> toOptions(Iterable<String> options) {
        Pattern optionPattern = Pattern.compile("(?<key>[^=]+)(=(?<value>[^=]*))?");
        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();

        StreamSupport.stream(options.spliterator(), false)
                .map(optionPattern::matcher)
                .filter(Matcher::matches)
                .forEach(matcher -> mapBuilder.put(
                        matcher.group("key"),
                        Optional
                                .ofNullable(matcher.group("value"))
                                .orElse("true")));
        return mapBuilder.build();
    }
}
