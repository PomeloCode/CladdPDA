package com.cladd.services;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cladd.entities.api.BaseConfigApi;
import com.cladd.entities.api.ColorApi;
import com.cladd.entities.api.ProductoApi;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class DataBaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "StockIT";
	private static final String BASECONFIG = "BaseConfig";
	private static final String COLORES = "Colores";
	private static final String PRODUCTOS = "Productos";

	private HashMap<String, String> BaseConfig = new HashMap<>();
	private HashMap<String, String> ColoresList = new HashMap<>();
	private HashMap<String, String> ProductosList = new HashMap<>();

	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createTable = new String("create table " + BASECONFIG +
				"(id INTEGER PRIMARY KEY, tipo TEXT, value TEXT)");
		String createTableColores = new String("create table " + COLORES +
				"(colorID INTEGER PRIMARY KEY, colorCod TEXT, descrip TEXT, fecha TEXT)");
		String createTableProductos = new String("create table " + PRODUCTOS +
				"(productoID INTEGER PRIMARY KEY, productoCod TEXT, descrip TEXT, fecha TEXT)");
		db.execSQL(createTable);
		db.execSQL(createTableColores);
		db.execSQL(createTableProductos);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(new String("DROP TABLE IF EXISTS " + BASECONFIG));
		db.execSQL(new String("DROP TABLE IF EXISTS " + COLORES));
		db.execSQL(new String("DROP TABLE IF EXISTS " + PRODUCTOS));
		onCreate(db);
	}

	public void addBaseConfigDefault() {

		/**
		 * Modo Debug:100 o Produccion:200
		 */
		addBaseConfig("modo", "200");
		addBaseConfig("UNIQUEIDPDA", "");
		addBaseConfig("_OperarioLog", "");
		addBaseConfig("_OperarioDesc", "");
		addBaseConfig("_OperarioUsoTracker", "");
		addBaseConfig("_OperarioPlanta", "");

		/**
		 * endpoint
		 */
		addBaseConfig("endpoint", "http://RFIDSERVER/INTEGRATIONSTOCKIT/");

	}

	public boolean addBaseConfig(String tipo, String value) {
		BaseConfig = getBaseConfig();

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("tipo", tipo);
		contentValues.put("value", value);


		if (BaseConfig.containsKey(tipo))
			db.update(BASECONFIG, contentValues, "tipo = ?", new String[]{tipo});
		else
			db.insert(BASECONFIG, null, contentValues);

		return true;

	}

	public boolean addColor(int id, String Cod, String desc, String fecha) {
		ColoresList = getColoresList();

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("colorID", id);
		contentValues.put("colorCod", Cod);
		contentValues.put("descrip", desc);
		contentValues.put("fecha", fecha);


		if (ColoresList.containsKey(String.valueOf(id)))
			db.update(COLORES, contentValues, "colorCod = ?", new String[]{Cod});
		else
			db.insert(COLORES, null, contentValues);

		return true;

	}

	public boolean addProducto (int id, String Cod, String desc, String fecha) {

		ProductosList = getProductosList();

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("productoID", id);
		contentValues.put("productoCod", Cod);
		contentValues.put("descrip", desc);
		contentValues.put("fecha", fecha);


		if (ProductosList.containsKey(String.valueOf(id)))
			db.update(PRODUCTOS, contentValues, "productoCod = ?", new String[]{Cod});
		else
			db.insert(PRODUCTOS, null, contentValues);

		return true;

	}

	public HashMap<String, String> getBaseConfig() {
		SQLiteDatabase database = this.getReadableDatabase();
		HashMap<String, String> baseConfigList = new HashMap<String, String>();

		Cursor cursor = database.rawQuery(new String("Select * from " + BASECONFIG), null);
		cursor.moveToFirst();
		int i = 0;
		while (cursor.getCount() > i) {

			String s1 = cursor.getString(cursor.getColumnIndexOrThrow("tipo"));
			String s2 = cursor.getString(cursor.getColumnIndexOrThrow("value"));
			baseConfigList.put(s1, s2);
			i++;
			cursor.moveToPosition(i);
		}

		return baseConfigList;
	}

	public HashMap<String, String> getColoresList() {
		SQLiteDatabase database = this.getReadableDatabase();
		HashMap<String, String> coloresList = new HashMap<String, String>();

		Cursor cursor = database.rawQuery(new String("Select * from " + COLORES), null);
		cursor.moveToFirst();
		int i = 0;
		while (cursor.getCount() > i) {

			String s1 = cursor.getString(cursor.getColumnIndexOrThrow("colorID"));
			String s2 = cursor.getString(cursor.getColumnIndexOrThrow("descrip"));
			coloresList.put(s1, s2);
			i++;
			cursor.moveToPosition(i);
		}

		return coloresList;
	}

	public HashMap<String, String> getProductosList() {
		SQLiteDatabase database = this.getReadableDatabase();
		HashMap<String, String> productosList = new HashMap<String, String>();

		Cursor cursor = database.rawQuery(new String("Select * from " + PRODUCTOS), null);
		cursor.moveToFirst();
		int i = 0;
		while (cursor.getCount() > i) {

			String s1 = cursor.getString(cursor.getColumnIndexOrThrow("productoID"));
			String s2 = cursor.getString(cursor.getColumnIndexOrThrow("descrip"));
			productosList.put(s1, "[" + s1 + "] " + s2);
			i++;
			cursor.moveToPosition(i);
		}

		return productosList;
	}

	public String getBaseConfigData(String tipo) {
		BaseConfig = getBaseConfig();
		String response;
		if (BaseConfig.containsKey(tipo))
			response = BaseConfig.get(tipo);
		else
			response = "error";

		return response;
	}

	public void setBaseConfigHardCoded() {
		/**
		 * Modo Produccion View
		 */
		addBaseConfig("verdetalleProd", "true");
		addBaseConfig("bctorfProd", "true");
		addBaseConfig("writecontProd", "true");
		addBaseConfig("finderProd", "true");
		addBaseConfig("trackeoProd", "true");
		addBaseConfig("verificacionProd", "true");
		addBaseConfig("ubicacionProd", "true");
		addBaseConfig("ajustesProd", "true");

		/**
		 * Potencias Por Activity
		 */
		addBaseConfig("AntPowerVerDetalle", "6");
		addBaseConfig("AntPowerBCtoRFRead", "6");
		addBaseConfig("AntPowerBCtoRFWrite", "33");
		addBaseConfig("AntPowerFinderRead", "6");
		addBaseConfig("AntPowerFinderSearch", "33");
		addBaseConfig("AntPowerTrackerRead", "6");
		addBaseConfig("AntPowerTrackerTrack", "12");
		addBaseConfig("AntPowerVerificarRead", "6");
		addBaseConfig("AntPowerVerificarVerificar", "12");
		addBaseConfig("AntPowerUbicacion", "10");
		addBaseConfig("AntPowerWriteContainerRead", "10");
	}

	public void setBaseConfig(List<BaseConfigApi> list) {

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getType().equals("BaseConfig")) {
				addBaseConfig(list.get(i).getName(), list.get(i).getValue());
			}
		}
	}

	public void setColores(List<ColorApi> list) {

		for (int i = 0; i < list.size(); i++) {

				addColor(list.get(i).getColorID(), list.get(i).getColorCod(),list.get(i).getDescrip(),list.get(i).getFecha());

		}
	}

	public void setProductos(List<ProductoApi> list) {

		for (int i = 0; i < list.size(); i++) {
			addProducto(list.get(i).getProductoID(), list.get(i).getProductoCod(),list.get(i).getDescrip(),list.get(i).getFecha());
		}
	}

	public String getDateLastColor() {
		SQLiteDatabase database = this.getReadableDatabase();
		String color = new String();

		Cursor cursor = database.rawQuery(new String("Select * from " + COLORES + " order by colorID desc  LIMIT 1"), null);
		cursor.moveToFirst();
		int i = 0;
		while (cursor.getCount() > i) {

			String s1 = cursor.getString(cursor.getColumnIndexOrThrow("fecha"));
			color = (s1);
			i++;
			cursor.moveToPosition(i);
		}

		return color;
	}

	public String getDateLastProducto() {
		SQLiteDatabase database = this.getReadableDatabase();
		String producto = new String();

		Cursor cursor = database.rawQuery(new String("Select * from " + PRODUCTOS + " order by productoID desc  LIMIT 1"), null);
		cursor.moveToFirst();
		int i = 0;
		while (cursor.getCount() > i) {

			String s1 = cursor.getString(cursor.getColumnIndexOrThrow("fecha"));
			producto = (s1);
			i++;
			cursor.moveToPosition(i);
		}

		return producto;
	}

}