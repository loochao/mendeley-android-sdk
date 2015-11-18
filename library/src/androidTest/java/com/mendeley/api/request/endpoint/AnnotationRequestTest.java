package com.mendeley.api.request.endpoint;

import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.Point;
import com.mendeley.api.request.Request;
import com.mendeley.api.request.SignedInTest;
import com.mendeley.api.request.params.AnnotationRequestParameters;
import com.mendeley.api.testUtils.AssertUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class AnnotationRequestTest extends SignedInTest {

    public void test_getAnnotations_withoutParameters_receivesCorrectAnnotations() throws Exception {
        // GIVEN some annotations
        final List<Annotation> expected = new LinkedList<Annotation>();
        final Document postedDocument = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));

        for (int i = 0; i < 5; i++) {
            final Annotation annotation = createAnnotation(postedDocument.id);
            getTestAccountSetupUtils().setupAnnotation(annotation);
            expected.add(annotation);
        }

        // WHEN getting annotations
        final List<Annotation> actual = getRequestFactory().getAnnotations().run().resource;

        Comparator<Annotation> comparator = new Comparator<Annotation>() {
            @Override
            public int compare(Annotation a1, Annotation a2) {
                return a1.text.compareTo(a2.text);
            }
        };

        // THEN we have the expected annotations
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getAnnotations_withParameters_receivesCorrectAnnotations() throws Exception {
        // GIVEN some annotations
        final List<Annotation> expected = new LinkedList<Annotation>();
        final Document postedDocument = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));

        for (int i = 0; i < 5; i++) {
            final Annotation annotation = createAnnotation(postedDocument.id);
            getTestAccountSetupUtils().setupAnnotation(annotation);
            expected.add(annotation);
        }

        // WHEN getting annotations
        AnnotationRequestParameters params = new AnnotationRequestParameters();
        params.documentId = postedDocument.id;
        params.limit = 12;
        final List<Annotation> actual = getRequestFactory().getAnnotations(params).run().resource;

        Comparator<Annotation> comparator = new Comparator<Annotation>() {
            @Override
            public int compare(Annotation a1, Annotation a2) {
                return a1.text.compareTo(a2.text);
            }
        };

        // THEN we have the expected annotations
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getAnnotations_whenMoreThanOnePage_receivesCorrectAnnotations() throws Exception {
        final Document postedDocument = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));

        // GIVEN a number of annotations greater than the page size
        final int pageSize = 4;
        final int pageCount = 3;
        final int annotationsCount = pageSize * pageCount;

        final List<Annotation> expected = new LinkedList<Annotation>();
        for (int i = 0; i < annotationsCount; i++) {
            final Annotation annotation = createAnnotation(postedDocument.id);
            getTestAccountSetupUtils().setupAnnotation(annotation);
            expected.add(annotation);
        }

        // WHEN getting annotations
        final AnnotationRequestParameters params = new AnnotationRequestParameters();
        params.limit = pageSize;

        final List<Annotation> actual = new LinkedList<Annotation>();
        Request.Response<List<Annotation>> response = getRequestFactory().getAnnotations(params).run();

        // THEN we receive an annotations list...
        for (int page = 0; page < pageCount; page++) {
            actual.addAll(response.resource);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", response.next != null);
                response = getRequestFactory().getAnnotations(response.next).run();
            }
        }

        Comparator<Annotation> comparator = new Comparator<Annotation>() {
            @Override
            public int compare(Annotation a1, Annotation a2) {
                return a1.text.compareTo(a2.text);
            }
        };

        // THEN we have the expected annotations
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_postAnnotation_createsAnnotationInServer() throws Exception {

        // GIVEN an annotation
        final Document postedDocument = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
        final Annotation postingAnnotation = createAnnotation(postedDocument.id);

        // WHEN posting it
        final Annotation returnedAnnotation = getRequestFactory().postAnnotation(postingAnnotation).run().resource;

        // THEN we receive the same annotation back, with id filled
        AssertUtils.assertAnnotation(postingAnnotation, returnedAnnotation);
        assertNotNull(returnedAnnotation.id);

        // ...and the annotation exists in the server
        AssertUtils.assertAnnotations(getRequestFactory().getAnnotations().run().resource, Arrays.asList(postingAnnotation));
    }

    public void test_deleteAnnotation_removesTheAnnotationFromServer() throws Exception {
        // GIVEN some annotations
        final Document postedDocument = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
        final List<Annotation> serverAnnotationsBefore = setUpAnnotationsInServer(postedDocument.id, 5);

        // WHEN deleting one of them
        final String deletingAnnotationId = serverAnnotationsBefore.get(0).id;
        getRequestFactory().deleteAnnotation(deletingAnnotationId).run();

        // THEN the server does not have the deleted annotation any more
        final List<Annotation> serverAnnotationsAfter = getRequestFactory().getAnnotations().run().resource;
        assertEquals("Annotation deleted", serverAnnotationsBefore.size() - 1, serverAnnotationsAfter.size());

        for (Annotation annotation : serverAnnotationsAfter) {
            assertFalse("Right annotation deleted", deletingAnnotationId.equals(annotation.id));
        }
    }

    public void test_patchAnnotation_updatesTheAnnotationOnServer() throws Exception {
        // GIVEN annotation
        final Document postedDocument = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
        final Annotation annotation = setUpAnnotationsInServer(postedDocument.id, 1).get(0);

        // WHEN patched
        final Annotation annotationPatched = new Annotation.Builder(annotation)
                .setText(annotation.text + "updated")
                .build();

        final Annotation returnedAnnotation = getRequestFactory().patchAnnotation(annotation.id, annotationPatched).run().resource;

        // THEN we receive the patched annotation
        AssertUtils.assertAnnotation(annotationPatched, returnedAnnotation);

        // ...and the server has updated the annotation
        final Annotation annotationAfter = getRequestFactory().getAnnotation(annotationPatched.id).run().resource;
        AssertUtils.assertAnnotation(annotationPatched, annotationAfter);
    }

    private Annotation createAnnotation(String docId) {
        final Annotation annotation = new Annotation.Builder()
                .setDocumentId(docId)
                .setText("text " + getRandom().nextInt())
                .setType(Annotation.Type.STICKY_NOTE)
                .setFileHash("hash " + +getRandom().nextInt())
                .setPositions(Arrays.asList(new Annotation.Position(new Point(getRandom().nextInt(100), getRandom().nextInt(100)), null, getRandom().nextInt(40))))
                .build();
        return annotation;
    }

    private Document createDocument(String title) throws Exception {
        final Document doc = new Document.Builder().
                setType("book").
                setTitle(title).
                setYear(getRandom().nextInt(2000)).
                setAbstractString("abstract" + getRandom().nextInt()).
                setSource("source" + getRandom().nextInt()).
                build();

        return doc;
    }

    private List<Annotation> setUpAnnotationsInServer(String docId, int annotationCount) throws Exception {
        final List<Annotation> annotations = new LinkedList<Annotation>();
        for (int i = 0; i < annotationCount; i++) {
            final Annotation annotation = createAnnotation(docId);
            getTestAccountSetupUtils().setupAnnotation(annotation);
            annotations.add(annotation);
        }

        return getRequestFactory().getAnnotations().run().resource;
    }

}
