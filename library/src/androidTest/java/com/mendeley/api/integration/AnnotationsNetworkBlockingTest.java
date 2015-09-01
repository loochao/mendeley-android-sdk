package com.mendeley.api.integration;

import android.graphics.Color;
import android.test.AndroidTestCase;

import com.mendeley.api.BlockingSdk;
import com.mendeley.api.callbacks.annotations.AnnotationList;
import com.mendeley.api.callbacks.document.DocumentList;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Box;
import com.mendeley.api.model.Point;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.Sort;
import com.mendeley.api.testUtils.SignInException;
import com.mendeley.api.testUtils.TestUtils;

import java.util.Arrays;
import java.util.Comparator;

import static java.util.Collections.sort;

public class AnnotationsNetworkBlockingTest extends AndroidTestCase {
    private static final Box[] BOX_ARRAY = { new Box(new Point(0, 0), new Point(10, 10), null) };
    private static final Box[] POINT_ARRAY = { new Box(new Point(5, 5), null, 42) };

    private static final Annotation[] ANNOTATIONS = {
            new Annotation.Builder()
                    .setType(Annotation.Type.STICKY_NOTE)
                    .setText("Sticky")
                    .setPositions(Arrays.asList(POINT_ARRAY))
                    .setFileHash("abc")
                    .build(),
            new Annotation.Builder()
                    .setType(Annotation.Type.HIGHLIGHT)
                    .setPositions(Arrays.asList(BOX_ARRAY))
                    .setColor(Color.CYAN)
                    .setFileHash("def")
                    .build(),
            new Annotation.Builder()
                    .setType(Annotation.Type.DOCUMENT_NOTE)
                    .setText("Doc note")
                    .build(),
    };

    private static final Comparator<Annotation> COMPARATOR = new Comparator<Annotation>() {
        @Override
        public int compare(Annotation annotation1, Annotation annotation2) {
            if (annotation1.type != annotation2.type) {
                return annotation1.type.compareTo(annotation2.type);
            } else {
                return annotation1.text.compareTo(annotation2.text);
            }
        }
    };

    private BlockingSdk sdk;

    @Override
    protected void setUp() throws InterruptedException, SignInException, MendeleyException {
        sdk = TestUtils.signIn(getContext().getAssets());
    }

    public void testGetAnnotations() throws MendeleyException {
        ensureCorrectAnnotationsExist();

        AnnotationList annotationList = getSortedAnnotations();

        assertNotNull("annotations cannot be null", annotationList);
        assertNotNull("annotations list cannot be null", annotationList.annotations);
        int numAnnotations = annotationList.annotations.size();
        assertEquals("expected three annotations", 3, numAnnotations);

        // Check some properties:
        assertEquals("annotation type incorrect",
                Annotation.Type.STICKY_NOTE, annotationList.annotations.get(0).type);
        assertEquals("annotation color incorrect",
                 Color.CYAN, (int) annotationList.annotations.get(1).color);
        assertEquals("annotation text incorrect",
                "Doc note", annotationList.annotations.get(2).text);
    }

    public void testPostAndDeleteAnnotation() throws MendeleyException {
        String firstDocumentId = getFirstDocumentId();

        Annotation annotation = new Annotation.Builder()
                .setType(Annotation.Type.STICKY_NOTE)
                .setText("Extra")
                .setFileHash("123")
                .setDocumentId(firstDocumentId)
                .build();

        Annotation rcvd = sdk.postAnnotation(annotation);

        assertEquals("123", rcvd.fileHash);

        sdk.deleteAnnotation(rcvd.id);
    }

    public void testPatchAnnotation() throws MendeleyException {
        AnnotationList annotationList = getSortedAnnotations();
        if (annotationList.annotations.size() < 1) {
            fail("At least one annotation required");
        }
        Annotation annotation = annotationList.annotations.get(0);
        String id = annotation.id;

        assertEquals("Sticky", annotation.text);

        Annotation modified = new Annotation.Builder(annotation).setText("Tacky").build();
        Annotation patched = sdk.patchAnnotation(id, modified);
        assertEquals(patched.id, id);
        assertEquals(patched.text, "Tacky");

        modified = sdk.getAnnotation(id);
        assertEquals("Tacky", modified.text);

        modified = new Annotation.Builder(annotation).setText("Sticky").build();
        patched = sdk.patchAnnotation(id, modified);
        assertEquals(patched.id, id);
        assertEquals(patched.text, "Sticky");
    }

    private void ensureCorrectAnnotationsExist() throws MendeleyException {
        AnnotationList annotationList = sdk.getAnnotations();
        String firstDocumentId = getFirstDocumentId();

        // Delete existing annotations:
        for (Annotation annotation: annotationList.annotations) {
            sdk.deleteAnnotation(annotation.id);
        }

        // Post clean annotations:
        for (Annotation templateAnnotation : ANNOTATIONS) {
            sdk.postAnnotation(attachToDocument(templateAnnotation, firstDocumentId));
        }
    }

    private AnnotationList getSortedAnnotations() throws MendeleyException {
        AnnotationList annotationList = sdk.getAnnotations();
        sort(annotationList.annotations, COMPARATOR);
        return annotationList;
    }

    private String getFirstDocumentId() throws MendeleyException {
        DocumentRequestParameters params = new DocumentRequestParameters();
        params.sort = Sort.TITLE;
        DocumentList documentList = sdk.getDocuments(params);
        if (documentList.documents.size() < 1) {
            fail("At least one document required");
        }
        return documentList.documents.get(0).id;
    }

    private Annotation attachToDocument(Annotation annotation, String documentId) {
        return new Annotation.Builder(annotation).setDocumentId(documentId).build();
    }

    private boolean isInFixture(Annotation annotation) {
        String documentId = annotation.documentId;
        for (Annotation fixture : ANNOTATIONS) {
            if (annotationsEqual(annotation, attachToDocument(fixture, documentId))) {
                return true;
            }
        }
        return false;
    }

    private boolean wasReceived(Annotation annotation, AnnotationList annotationList) {
        for (Annotation rcvd : annotationList.annotations) {
            if (annotationsEqual(annotation, rcvd)) {
                return true;
            }
        }
        return false;
    }

    private boolean annotationsEqual(Annotation anno1, Annotation anno2) {
        return stringsEqual(anno1.documentId, anno2.documentId)
                && stringsEqual(anno1.text, anno2.text)
                && stringsEqual(anno1.fileHash, anno2.fileHash)
                && anno1.type.equals(anno2.type)
                && anno1.color == anno2.color;
    }

    private boolean stringsEqual(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }
}
