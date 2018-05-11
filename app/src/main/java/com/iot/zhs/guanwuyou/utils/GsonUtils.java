package com.iot.zhs.guanwuyou.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * gson操作
 * @author star
 *
 */
public class GsonUtils {
	/**
	 * string转成JsonObject
	 * @param str
	 * @return
	 */
	public static JsonObject structureGson(String str){
		Gson gson=new Gson();
		JsonParser jp=new JsonParser();
		JsonElement message=jp.parse(str);
		JsonObject jsObject=message.getAsJsonObject();
		return jsObject;
	}

	/**
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> ArrayList<T> jsonToArrayList(String json, Class<T> clazz) {
		Type type = new TypeToken<ArrayList<JsonObject>>() {}.getType();

		ArrayList<JsonObject> jsonObjects = new Gson().fromJson(json, type);

		ArrayList<T> arrayList = new ArrayList<>();
		for (JsonObject jsonObject : jsonObjects) {
			arrayList.add(new Gson().fromJson(jsonObject, clazz));
		}
		return arrayList;
	}



	/**
	 * json字符串转成对象
	 * @param str
	 * @param type
	 * @return
	 */
	public static <T> T stringToObject(String str, Type type) {
		Gson gson = new Gson();
		return gson.fromJson(str, type);
	}

	/**
	 * json字符串转成对象
	 * @param str
	 * @return
	 */
	public static JsonArray stringToJsonArray(String str) {
		JsonArray jsonArray=new JsonParser().parse(str).getAsJsonArray();

		return jsonArray;
	}

	/**
	 * 对象转换成json字符串
	 * @param obj
	 * @return
	 */
	public static String objectToString(Object obj) {
		Gson gson = new Gson();
		return gson.toJson(obj);
	}

	public static <T> ArrayList<T> jsonArrayToArrayList(JsonArray jsonArray, Class<T> clazz) {
		Type type = new TypeToken<ArrayList<T>>() {}.getType();
		ArrayList<T> arrayList = new ArrayList<>();
		arrayList = new Gson().fromJson(jsonArray, type);

		return arrayList;
	}



}
