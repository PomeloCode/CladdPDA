package com.cladd.services;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cladd.entities.api.Colores;
import com.cladd.entities.api.DynamicConfig;
import com.cladd.entities.api.Productos;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class DataBaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "StockIT";
	public static final String DYNAMICCONFIG = "DynamicConfig";
	private static final String COLORES = "Colores";
	private static final String PRODUCTOS = "Productos";

	private HashMap<String, String> DynamicConfigs = new HashMap<>();
	private HashMap<String, String> ColoresList = new HashMap<>();
	private HashMap<String, String> ProductosList = new HashMap<>();

	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 3);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createTable = new String("create table " + DYNAMICCONFIG +
				"(id INTEGER PRIMARY KEY, type TEXT, name TEXT, value TEXT)");
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
		db.execSQL(new String("DROP TABLE IF EXISTS " + DYNAMICCONFIG));
		db.execSQL(new String("DROP TABLE IF EXISTS " + COLORES));
		db.execSQL(new String("DROP TABLE IF EXISTS " + PRODUCTOS));
		onCreate(db);
	}

	public void addDynamicConfigsDefault() {

		addDynamicConfigs("BaseConfig", "endpoint", "http://mail.cladd.com.ar:8803/IntegrationStockIT/");

	}

	public void addDynamicConfigs(String type, String name, String value) {
		DynamicConfigs = getDynamicConfigs();

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("type", type);
		contentValues.put("name", name);
		contentValues.put("value", value);


		if (DynamicConfigs.containsKey(type))
			db.update(DYNAMICCONFIG, contentValues, "type = ?", new String[]{type});
		else
			db.insert(DYNAMICCONFIG, null, contentValues);

	}

	public void addColor(int id, String Cod, String desc, String fecha) {
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

	}

	public void addProducto(int id, String Cod, String desc, String fecha) {

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

	}

	public Object setDynamicConfigs(List<DynamicConfig> list) {

		addDynamicConfigsDefault();
		for (int i = 0; i < list.size(); i++) {

			addDynamicConfigs(list.get(i).getType(), list.get(i).getName(), list.get(i).getValue());

		}

		return new Object();
	}

	public void setColores(List<Colores> list) {

		for (int i = 0; i < list.size(); i++) {

			addColor(list.get(i).getColorID(), list.get(i).getColorCod(), list.get(i).getDescrip(), list.get(i).getFecha());

		}
	}

	public void setProductos(List<Productos> list) {

		for (int i = 0; i < list.size(); i++) {
			addProducto(list.get(i).getProductoID(), list.get(i).getProductoCod(), list.get(i).getDescrip(), list.get(i).getFecha());
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

	public HashMap<String, String> getDynamicConfigs() {
		SQLiteDatabase database = this.getReadableDatabase();
		HashMap<String, String> baseConfigList = new HashMap<String, String>();

		Cursor cursor = database.rawQuery(new String("Select * from " + DYNAMICCONFIG), null);
		cursor.moveToFirst();
		int i = 0;
		while (cursor.getCount() > i) {

			String s1 = cursor.getString(cursor.getColumnIndexOrThrow("name"));
			String s2 = cursor.getString(cursor.getColumnIndexOrThrow("value"));
			baseConfigList.put(s1, s2);
			i++;
			cursor.moveToPosition(i);
		}

		return baseConfigList;
	}

	public HashMap<String, String> getDynamicConfigsByType(String type) {
		SQLiteDatabase database = this.getReadableDatabase();
		HashMap<String, String> baseConfigList = new HashMap<String, String>();

		Cursor cursor = database.rawQuery(new String("Select * from " + DYNAMICCONFIG + "WHERE type = ?" + type), null);
		cursor.moveToFirst();
		int i = 0;
		while (cursor.getCount() > i) {

			String s1 = cursor.getString(cursor.getColumnIndexOrThrow("name"));
			String s2 = cursor.getString(cursor.getColumnIndexOrThrow("value"));
			baseConfigList.put(s1, s2);
			i++;
			cursor.moveToPosition(i);
		}

		return baseConfigList;
	}

	public HashMap<String, String> getDynamicConfigsByTypeAndName(String type, String name) {
		SQLiteDatabase database = this.getReadableDatabase();
		HashMap<String, String> baseConfigList = new HashMap<String, String>();

		Cursor cursor = database.rawQuery(new String("Select * from " + DYNAMICCONFIG + " WHERE type = '" + type + "' AND " + " name LIKE '" + name + "%'"), null);
		cursor.moveToFirst();
		int i = 0;
		while (cursor.getCount() > i) {

			String s1 = cursor.getString(cursor.getColumnIndexOrThrow("name")).toLowerCase(Locale.ROOT);
			String s2 = cursor.getString(cursor.getColumnIndexOrThrow("value"));
			baseConfigList.put(s1, s2);
			i++;
			cursor.moveToPosition(i);
		}

		return baseConfigList;
	}

	public String getDynamicConfigsData(String name) {
		DynamicConfigs = getDynamicConfigs();
		String response;
		if (DynamicConfigs.containsKey(name))
			response = DynamicConfigs.get(name);
		else
			response = "error";

		return response;
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

	public void RestartTable(String tablename) {

		String selectQuery = "DELETE FROM " + tablename;

		SQLiteDatabase db = this.getWritableDatabase();

		db.execSQL(selectQuery);
	}
}