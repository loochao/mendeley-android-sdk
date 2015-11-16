package com.mendeley.api.request;


import android.test.suitebuilder.annotation.LargeTest;

import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.testUtils.AssertUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ReadPositionRequestTest extends RequestTest {


    @LargeTest
    public void test_getRecentlyRead_receivesCorrectItems() throws Exception {
        // GIVEN some documents with files and recently read data existing in the server
        final List<ReadPosition> expected = new LinkedList<ReadPosition>();
        for (int i = 0; i < 5; i++) {
            // add them in reverse order, as that's how the server returns them
            expected.add(0, setupRecentlyReadEntryAndDependancies());
        }

        // WHEN getting recently read positions
        final List<ReadPosition> actual = getSdk().getRecentlyRead(null, null, 20).run().resource;

        // THEN we have the expected recently read positions
        AssertUtils.assertReadPositions(expected, actual);
    }

    @LargeTest
    public void test_postRecentlyRead_postsCorrectItems() throws Exception {
        // GIVEN a doc with a file
        final File file = setupDocumentAndFile();

        // WHEN posting a recently read position for it
        final ReadPosition expected = new ReadPosition.Builder()
                .setFileId(file.id)
                .setPage(Math.abs(getRandom().nextInt(1000)))
                .setVerticalPosition(Math.abs(getRandom().nextInt(1000)))
                .setDate(new Date())
                .build();

        getSdk().postRecentlyRead(expected).run();

        // THEN we have successfully posted it
        final List<ReadPosition> actual = getTestAccountSetupUtils().getAllReadingPositions();
        AssertUtils.assertReadPositions(Arrays.asList(expected), actual);
    }

    @LargeTest
    public void test_postRecentlyRead_postsCorrectItems_whenReadPositionAlreadyExisted() throws Exception {
        // GIVEN a doc with a file
        final File file = setupDocumentAndFile();

        // and a recently read position for it
        final ReadPosition firstReadPostion = new ReadPosition.Builder()
                .setFileId(file.id)
                .setPage(Math.abs(getRandom().nextInt(1000)))
                .setVerticalPosition(Math.abs(getRandom().nextInt(1000)))
                .setDate(new Date())
                .build();

        getSdk().postRecentlyRead(firstReadPostion).run();

        // WHEN updating (posting for second time) the read position for the same file

        // and a recently read position for it
        final ReadPosition secondReadPostion = new ReadPosition.Builder()
                .setFileId(file.id)
                .setPage(Math.abs(getRandom().nextInt(1000)))
                .setVerticalPosition(Math.abs(getRandom().nextInt(1000)))
                .setDate(new Date())
                .build();

        getSdk().postRecentlyRead(secondReadPostion).run();

        // THEN we have successfully posted it
        final List<ReadPosition> actual = getTestAccountSetupUtils().getAllReadingPositions();
        AssertUtils.assertReadPositions(Arrays.asList(secondReadPostion), actual);
    }

    private ReadPosition setupRecentlyReadEntryAndDependancies() throws Exception {
        final String fileId = setupDocumentAndFile().id;

        // create recently read position
        int page = getRandom().nextInt();
        int verticalPosition = getRandom().nextInt();

        return getTestAccountSetupUtils().setupReadingPosition(fileId, page, verticalPosition, new Date());
    }

    private File setupDocumentAndFile() throws MendeleyException, IOException {
        // create doc
        final Document doc = new Document.Builder().
                setType("Book").
                setTitle("doc" + getRandom().nextInt()).
                setSource("source" + getRandom().nextInt()).
                build();

        final String docId = getTestAccountSetupUtils().setupDocument(doc).id;

        // create file
        final InputStream inputStream = getContext().getAssets().open("android.pdf");
        return getTestAccountSetupUtils().setupFile(docId, "file" + getRandom().nextInt(2000), inputStream);
    }


}
