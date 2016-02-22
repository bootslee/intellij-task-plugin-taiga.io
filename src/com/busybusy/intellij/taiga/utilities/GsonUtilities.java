package com.busybusy.intellij.taiga.utilities;

import com.google.gson.JsonObject;

public class GsonUtilities
{
	/**
	 * From the provided {@param object} get the string value from the {@param fieldName}.
	 * If the field doesn't exist or is not a json primitive. we return the {@param defaultValue}
	 *
	 * @param object       object to retrieve the string from
	 * @param fieldName    the name of the field on the json object
	 * @param defaultValue value to return if there is a problem
	 *
	 * @return object.get(fieldName) else defaultValue
	 */
	public static String getAsStringOr(JsonObject object, String fieldName, String defaultValue)
	{
		String result = defaultValue;
		if (object.has(fieldName) && object.get(fieldName).isJsonPrimitive())
		{
			result = object.get(fieldName).getAsJsonPrimitive().getAsString();
		}

		return result;
	}
}
