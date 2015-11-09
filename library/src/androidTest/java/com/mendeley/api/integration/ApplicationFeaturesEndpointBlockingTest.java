package com.mendeley.api.integration;


import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.mendeley.api.test.BuildConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ApplicationFeaturesEndpointBlockingTest extends EndpointBlockingTest {

    @SmallTest
    public void test_getApplicationFeatures_receivesCorrectItems() throws Exception {

        if (BuildConfig.FLAVOR.equals("production")) {
            Log.d("", "Application features test can only run on staging");
            return;
        }

        // GIVEN some application features on the server
        final String[] features = new String[]{
                "android.test-feature1-"+getRandom().nextInt(),
                "android.test-feature2-"+getRandom().nextInt(),
                "android.test-feature3-"+getRandom().nextInt()};
        final List<String> expected = new LinkedList<>();

        final List<String> featureIds = new ArrayList<>(features.length);
        try {
            for (String feature : features) {
                try {
                    String id = getTestAccountSetupUtils().setupApplicationFeature(feature);
                    featureIds.add(id);
                    expected.add(feature);
                } catch (Exception e) {
                    Log.d("", "failed to post application feature", e);
                }
            }

            assertTrue("no features posted", expected.size() > 0);

            // WHEN getting application features
            final List<String> actual = getSdk().getApplicationFeatures();

            // THEN we have the expected application features
            for (String feature : expected) {
                assertTrue(actual.indexOf(feature) != -1);
            }
        } finally {
            getTestAccountSetupUtils().deleteApplicationFeatures(featureIds);
        }
    }
}
