package com.mendeley.api.testUtils;

import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.Folder;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.model.UserRole;

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
