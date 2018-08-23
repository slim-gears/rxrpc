/**
 *
 */
package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.data.TypeInfo;
import com.slimgears.rxrpc.apt.util.TypeFilters;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TypeFiltersTest {
    @Test
    public void testIncludedFilter() {
        Assert.assertTrue(TypeFilters
                .fromIncludedWildcard("java.*")
                .test(TypeInfo.of(List.class)));

        Assert.assertFalse(TypeFilters
                .fromIncludedWildcard("java.*")
                .test(TypeInfo.of(getClass())));
    }

    @Test
    public void testEmptyIncludedFilter() {
        Assert.assertTrue(TypeFilters
                .fromIncludedWildcard("")
                .test(TypeInfo.of(List.class)));
    }

    @Test
    public void testExcludedFilter() {
        Assert.assertFalse(TypeFilters
                .fromExcludedWildcard("java.*")
                .test(TypeInfo.of(List.class)));

        Assert.assertTrue(TypeFilters
                .fromExcludedWildcard("java.*")
                .test(TypeInfo.of(getClass())));
    }

    @Test
    public void testEmptyExcludedFilter() {
        Assert.assertTrue(TypeFilters
                .fromExcludedWildcard("")
                .test(TypeInfo.of(List.class)));
    }
}
