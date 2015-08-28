package com.mendeley.testUtils;

import com.mendeley.api.model.ReadPosition;

import junit.framework.Assert;

import java.util.List;

/**
 * Class for asserting models in the tests
 */
public class AssertUtils {

    public void assertReadPositions(List<ReadPosition> expected, List<ReadPosition> actual) {
        Assert.assertEquals("Number of read positions gotten", expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertReadPosition(expected.get(i), actual.get(i));
        }
    }

    private void assertReadPosition(ReadPosition expected, ReadPosition actual) {
        Assert.assertEquals(expected.fileId, actual.fileId);
        Assert.assertEquals(expected.page, actual.page);
        Assert.assertEquals(expected.verticalPosition, actual.verticalPosition);
        Assert.assertEquals(expected.date, actual.date);
    }


}
