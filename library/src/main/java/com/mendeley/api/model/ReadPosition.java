package com.mendeley.api.model;

import java.util.Date;

/**
 * Model of the item that marks the last reading position of a {@link File}
 */
public class ReadPosition {

    public final String id;
    public final String fileId;
    public final int page;
    public final float verticalPosition;
    public final Date date;

    public ReadPosition(String id, String fileId, int page, float verticalPosition, Date date) {
        this.id = id;
        this.fileId = fileId;
        this.page = page;
        this.verticalPosition = verticalPosition;
        this.date = date;
    }


    /**
     * Builder for {@link ReadPosition}
     */
    public static class Builder {
        private String id;
        private String fileId;
        private int page;
        private float verticalPosition;
        private Date date;

        public Builder() {
        }

        public Builder(ReadPosition other) {
            this.id = other.id;
            this.fileId = other.fileId;
            this.page = other.page;
            this.verticalPosition = other.verticalPosition;
            this.date = other.date;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setFileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder setPage(int page) {
            this.page = page;
            return this;
        }

        public Builder setVerticalPosition(float verticalPosition) {
            this.verticalPosition = verticalPosition;
            return this;
        }

        public Builder setDate(Date date) {
            this.date = date;
            return this;
        }

        public ReadPosition build() {
            return new ReadPosition(id, fileId, page, verticalPosition, date);
        }
    }


}
