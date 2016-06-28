package com.mendeley.sdk.model;

import com.mendeley.sdk.util.NullableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing institution json object.
 *
 */
public class Institution {

    public final int scivalId;
    public final String id;
    public final String name;
    public final String city;
    public final String state;
    public final String country;
    public final String parentId;
    public final NullableList<String> urls;
    public final String profilerUrl;
    public final NullableList<AlternativeName> altNames;

    public Institution(
            int scivalId,
            String id,
            String name,
            String city,
            String state,
            String country,
            String parentId,
            List<String> urls,
            String profilerUrl,
            List<AlternativeName> altNames) {

        this.scivalId = scivalId;
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.country = country;
        this.parentId = parentId;
        this.urls = new NullableList<String>(urls);
        this.profilerUrl = profilerUrl;
        this.altNames = new NullableList<AlternativeName>(altNames);
    }

    public static class Builder {
        private int scivalId;
        private String id;
        private String name;
        private String city;
        private String state;
        private String country;
        private String parentId;
        private List<String> urls;
        private String profilerUrl;
        private List<AlternativeName> altNames;

        public Builder() {}

        public Builder(Institution from) {
            this.scivalId = from.scivalId;
            this.id = from.id;
            this.name = from.name;
            this.city = from.city;
            this.state = from.state;
            this.country = from.country;
            this.parentId = from.parentId;
            this.urls = from.urls==null?new ArrayList<String>():from.urls;
            this.profilerUrl = from.profilerUrl;
            this.altNames = from.altNames==null?new ArrayList<AlternativeName>():from.altNames;
        }

        public Builder setScivalId(int scivalId) {
            this.scivalId = scivalId;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCity(String city) {
            this.city = city;
            return this;
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setCountry(String country) {
            this.country = country;
            return this;
        }

        public Builder setParentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder setUrls(List<String> urls) {
            this.urls = urls;
            return this;
        }

        public Builder setProfilerUrl(String profilerUrl) {
            this.profilerUrl = profilerUrl;
            return this;
        }

        public Builder setAltNames(List<AlternativeName> altNames) {
            this.altNames = altNames;
            return this;
        }

        public Institution build() {
            return new Institution(
                scivalId,
                id,
                name,
                city,
                state,
                country,
                parentId,
                urls,
                profilerUrl,
                altNames);
        }
    }
}
