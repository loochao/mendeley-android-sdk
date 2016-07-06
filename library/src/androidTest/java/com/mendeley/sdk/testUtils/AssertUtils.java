package com.mendeley.sdk.testUtils;

import android.text.TextUtils;

import com.mendeley.sdk.model.Annotation;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.model.Education;
import com.mendeley.sdk.model.Employment;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.model.Folder;
import com.mendeley.sdk.model.Group;
import com.mendeley.sdk.model.Institution;
import com.mendeley.sdk.model.Person;
import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.model.ReadPosition;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Class for asserting models in the tests
 */
public class AssertUtils {

    public static void assertDocuments(List<Document> expected, List<Document> actual) {
        Assert.assertEquals("Number of documents gotten", expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertDocument(expected.get(i), actual.get(i));
        }
    }

    public static void assertDocument(Document expected, Document actual) {
        Assert.assertEquals(expected.type, actual.type);
        Assert.assertEquals(expected.title, actual.title);
        Assert.assertEquals(expected.year, actual.year);
        Assert.assertEquals(expected.abstractString, actual.abstractString);
        Assert.assertEquals(expected.source, actual.source);
    }

    public static void assertPersons(List<Person> expected, List<Person> actual) {
        Assert.assertEquals("Number of persons gotten", expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertPerson(expected.get(i), actual.get(i));
        }
    }

    public static void assertPerson(Person expected, Person actual) {
        Assert.assertEquals(expected.firstName, actual.firstName);
        Assert.assertEquals(expected.lastName, actual.lastName);
    }

    public static void assertAnnotations(List<Annotation> expected, List<Annotation> actual) {
        Assert.assertEquals("Number of annotations gotten", expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertAnnotation(expected.get(i), actual.get(i));
        }
    }

    public static void assertAnnotation(Annotation expected, Annotation actual) {
        Assert.assertEquals(expected.type, actual.type);
        Assert.assertEquals(expected.text, actual.text);
        Assert.assertEquals(expected.documentId, actual.documentId);
        Assert.assertEquals(expected.fileHash, actual.fileHash);
    }

    public static void assertFiles(List<File> expected, List<File> actual) {
        Assert.assertEquals("Number of files gotten", expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertFile(expected.get(i), actual.get(i));
        }
    }

    public static void assertFile(File expected, File actual) {
        Assert.assertEquals(expected.documentId, actual.documentId);
        Assert.assertEquals(expected.mimeType, actual.mimeType);
    }

    public static void assertFolders(List<Folder> expected, List<Folder> actual) {
        Assert.assertEquals("Number of folders gotten", expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertFolder(expected.get(i), actual.get(i));
        }
    }

    public static void assertFolder(Folder expected, Folder actual) {
        Assert.assertEquals(expected.parentId, actual.parentId);
        Assert.assertEquals(expected.name, actual.name);
    }

    public static void assertGroup(Group expected, Group actual) {
        Assert.assertEquals(expected.id, actual.id);
        Assert.assertEquals(expected.name, actual.name);
    }


    public static void assertProfile(Profile expected, Profile actual) {
        if (!TextUtils.isEmpty(expected.id) && !TextUtils.isEmpty(actual.id)) {
            Assert.assertEquals(expected.id, actual.id);
        }
        Assert.assertEquals(expected.firstName, actual.firstName);
        Assert.assertEquals(expected.lastName, actual.lastName);
        Assert.assertEquals(expected.title, actual.title);
        Assert.assertEquals(expected.academicStatus, actual.academicStatus);
        assertInstitution(expected.institutionDetails, actual.institutionDetails);
        assertEmployments(expected.employment, actual.employment);
        assertEducations(expected.education, actual.education);
    }

    public static void assertReadPositions(List<ReadPosition> expected, List<ReadPosition> actual) {
        Assert.assertEquals("Number of read positions gotten", expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertReadPosition(expected.get(i), actual.get(i));
        }
    }

    private static void assertReadPosition(ReadPosition expected, ReadPosition actual) {
        Assert.assertEquals(expected.fileId, actual.fileId);
        Assert.assertEquals(expected.page, actual.page);
        Assert.assertEquals(expected.verticalPosition, actual.verticalPosition);
        //cannot assert date as server ignores the posted date
    }

    public static void assertEmployments(List<Employment> expected, List<Employment> actual) {
        Assert.assertEquals("Number of employments gotten", expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEmployment(expected.get(i), actual.get(i));
        }
    }

    public static void assertEducations(List<Education> expected, List<Education> actual) {
        Assert.assertEquals("Number of educations gotten", expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEducation(expected.get(i), actual.get(i));
        }
    }

    public static void assertEmployment(Employment actual, Employment expected) {
        if (!TextUtils.isEmpty(expected.id) && !TextUtils.isEmpty(actual.id)) {
            Assert.assertEquals("Employment id", actual.id, expected.id);
        }
        Assert.assertEquals("Employment start date", actual.startDate, expected.startDate);
        Assert.assertEquals("Employment end date", actual.endDate, expected.endDate);
        Assert.assertEquals("Employment position", actual.position, expected.position);
        Assert.assertEquals("Employment website", actual.website, expected.website);
        Assert.assertEquals("Employment isMainEmployment", actual.isMainEmployment, expected.isMainEmployment);
        assertInstitution(actual.institution, expected.institution);
    }

    public static void assertEducation(Education actual, Education expected) {
        if (!TextUtils.isEmpty(expected.id) && !TextUtils.isEmpty(actual.id)) {
            Assert.assertEquals("Education id", actual.id, expected.id);
        }
        Assert.assertEquals("Education start date", actual.startDate, expected.startDate);
        Assert.assertEquals("Education end date", actual.endDate, expected.endDate);
        Assert.assertEquals("Education degree", actual.degree, expected.degree);
        Assert.assertEquals("Education website", actual.website, expected.website);
        assertInstitution(actual.institution, expected.institution);
    }

    public static void assertInstitution(Institution actual, Institution expected) {
        Assert.assertEquals("Institution id", actual.id, expected.id);
        Assert.assertEquals("Institution name", actual.name, expected.name);

        if (!TextUtils.isEmpty(expected.parentId) && !TextUtils.isEmpty(actual.parentId)) {
            Assert.assertEquals("Institution parentId", actual.parentId, expected.parentId);
        }
        if (!TextUtils.isEmpty(expected.city) && !TextUtils.isEmpty(actual.city)) {
            Assert.assertEquals("Institution end city", actual.city, expected.city);
        }
        if (!TextUtils.isEmpty(expected.country) && !TextUtils.isEmpty(actual.country)) {
            Assert.assertEquals("Institution country", actual.country, expected.country);
        }
        if (!TextUtils.isEmpty(expected.state) && !TextUtils.isEmpty(actual.state)) {
            Assert.assertEquals("Institution state", actual.state, expected.state);
        }
        if (!TextUtils.isEmpty(expected.profilerUrl) && !TextUtils.isEmpty(actual.profilerUrl)) {
            Assert.assertEquals("Institution profilerUrl", actual.profilerUrl, expected.profilerUrl);
        }
    }

    public static <T extends Object> void assertSameElementsInCollection(Collection<T> col1, Collection<T> col2, Comparator<T> comparator) {
        Assert.assertTrue(areSameElementsInCollection(col1, col2, comparator));
    }

    private static <T extends Object> boolean areSameElementsInCollection(Collection<T> col1, Collection<T> col2, Comparator<T> comparator) {
        if (col1.size() != col2.size()) {
            return false;
        }

        List<T> list1 = new ArrayList<T>(col1);
        Collections.sort(list1, comparator);

        List<T> list2 = new ArrayList<T>(col2);
        Collections.sort(list2, comparator);

        Iterator<T> i1 = list1.iterator(), i2 = list2.iterator();

        while (i1.hasNext() && i2.hasNext()) {
            if (comparator.compare(i1.next(), i2.next()) != 0) {
                return false;
            }
        }

        return true;
    }
}
