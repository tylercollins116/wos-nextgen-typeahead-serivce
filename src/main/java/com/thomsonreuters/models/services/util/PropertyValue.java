package com.thomsonreuters.models.services.util;

public class PropertyValue implements Property {

	public static int FUZZTNESS_THRESHOLD = 10;

	private String value;
	private TYPE type = TYPE.NONE;

	private PropertyValue(String value, TYPE type) {
		this.value = value;
		this.type = type;
	}

	public static Property getProperty(String propertyName) {
		String $propertyName = "";

		if (propertyName != null
				&& ($propertyName = propertyName.trim().toLowerCase()).length() > 0) {

			if ($propertyName.startsWith(Property.DICTIONARY_PATH)) {
				return new PropertyValue(propertyName, TYPE.DICTIONARY_PATH);
			} else if ($propertyName.equals(Property.S3_BUCKET)) {
				return new PropertyValue(propertyName, TYPE.S3_BUCKET);
			}
		}

		return new PropertyValue("NONE", TYPE.NONE);
	}

	@Override
	public String getDictionayName() {
		if (isDictionaryPathRelated()) {
			return value.replace(Property.DICTIONARY_PATH, "");
		}

		return "";
	}

	@Override
	public boolean isDictionaryPathRelated() {
		if (this.type == TYPE.DICTIONARY_PATH) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isBucketName() {
		if (this.type == TYPE.S3_BUCKET) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.value;
	}

}
