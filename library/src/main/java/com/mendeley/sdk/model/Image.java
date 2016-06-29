package com.mendeley.sdk.model;

/**
 * Model class representing image json object.
 *
 */
public class Image {

    public final int width;
    public final int height;
    public final String url;
    public final boolean original;

    public Image(
            int width,
            int height,
            String url,
            boolean original) {

        this.width = width;
        this.height = height;
        this.url = url;
        this.original = original;
    }

    public static class Builder {
        private int width;
        private int height;
        private String url;
        private boolean original;

        public Builder() {}

        public Builder(Image from) {
            this.width = from.width;
            this.height = from.height;
            this.url = from.url;
            this.original = from.original;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setOriginal(boolean original) {
            this.original = original;
            return this;
        }

        public Image build() {
            return new Image(
                width,
                height,
                url,
                original);
        }
    }
}
