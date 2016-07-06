package com.mendeley.sdk.model;

import java.util.Date;

/**
 * Model class representing employment json object.
 *
 */
public class Employment {

	public final String id;
	public final Institution institution;
	public final String position;
	public final String website;
	public final Date startDate;
	public final Date endDate;
	public final Boolean isMainEmployment;

	private Employment(
			String id,
			Institution institution,
			String position,
			String website,
			Date startDate,
			Date endDate,
			Boolean isMainEmployment) {

		this.id = id;
		this.institution = institution;
		this.position = position;
		this.startDate = startDate;
		this.endDate = endDate;
		this.website = website;
		this.isMainEmployment = isMainEmployment;
	}

	public static class Builder {
		private String id;
		private Institution institution;
		private String website;
		private String position;
		private Date startDate;
		private Date endDate;
		private Boolean isMainEmployment;

		public Builder() {}

		public Builder(Employment from) {
			this.id = from.id;
			this.institution = from.institution;
			this.position = from.position;
			this.startDate = from.startDate;
			this.endDate = from.endDate;
			this.website = from.website;
			this.isMainEmployment = from.isMainEmployment;
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setInstitution(Institution institution) {
			this.institution = institution;
			return this;
		}

		public Builder setPosition(String position) {
			this.position = position;
			return this;
		}

		public Builder setStartDate(Date startDate) {
			this.startDate = startDate;
			return this;
		}

		public Builder setEndDate(Date endDate) {
			this.endDate = endDate;
			return this;
		}

		public Builder setWebsite(String website) {
			this.website = website;
			return this;
		}

		public Builder setIsMainEmployment(Boolean isMainEmployment) {
			this.isMainEmployment = isMainEmployment;
			return this;
		}

		public Employment build() {
			return new Employment(
					id,
					institution,
					position,
					website,
					startDate,
					endDate,
					isMainEmployment);
		}
	}
}
