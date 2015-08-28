package com.mendeley.integration;


import android.test.suitebuilder.annotation.LargeTest;

import com.mendeley.api.callbacks.read_position.ReadPositionList;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.File;
import com.mendeley.api.model.ReadPosition;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ReadPositionBlockingNetworkProviderTest extends BlockingNetworkProviderTest{


    @LargeTest
    public void test_getRecentlyRead_receivesCorrectItems() throws Exception {
        // GIVEN some documents with files and recently read data existing in the server
        final List<ReadPosition> expected = new LinkedList<ReadPosition>();
        for (int i = 0; i < 5; i++) {
            // add them in reverse order, as that's how the server returns them
            expected.add(0, createRecentlyReadEntryAndDependancies());
        }

        // WHEN getting recently read positions
        final ReadPositionList response = getSdk().getRecentlyRead(null, null, 20);
        final List<ReadPosition> actual = response.readPositions;

        // THEN we have the expected recently read positions
        getAssertUtils().assertReadPositions(expected, actual);
    }

    @LargeTest
    public void test_postRecentlyRead_postsCorrectItems() throws Exception {
        // GIVEN a doc with a file
        final File file = createDocumentAndFile();

        // WHEN posting a recently read position for it
        final ReadPosition expected = new ReadPosition.Builder()
                .setFileId(file.id)
                .setPage(Math.abs(getRandom().nextInt(1000)))
                .setVerticalPosition(Math.abs(getRandom().nextInt(1000)))
                .setDate(new Date())
                .build();

        getSdk().postRecentlyRead(expected);

        // THEN we have successfully posted it
        final List<ReadPosition> actual = getTestAccountSetupUtils().getAllReadingPositions();
        getAssertUtils().assertReadPositions(Arrays.asList(expected), actual);
    }


    private File createDocumentAndFile() throws MendeleyException, IOException {
        // create doc
        final Document doc = new Document.Builder().
                setType("Book").
                setTitle("doc" + getRandom().nextInt()).
                build();

        final String docId = getTestAccountSetupUtils().createDocument(doc).id;

        // create file
        final InputStream inputStream = getContext().getAssets().open("android.pdf");
        return getTestAccountSetupUtils().createFile(docId, "file" + getRandom().nextInt(2000), inputStream);
    }

    private ReadPosition createRecentlyReadEntryAndDependancies() throws Exception {
        final String fileId = createDocumentAndFile().id;

        // create recently read position
        int page = getRandom().nextInt();
        int verticalPosition = getRandom().nextInt();

        return getTestAccountSetupUtils().createReadingPosition(fileId, page, verticalPosition, new Date());

    }


}
