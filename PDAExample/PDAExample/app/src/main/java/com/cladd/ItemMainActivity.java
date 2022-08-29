package com.cladd;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.cladd.entities.api.BaseConfigApi;
import com.cladd.entities.api.ColorApi;
import com.cladd.entities.api.OperarioApi;
import com.cladd.entities.api.ProductoApi;
import com.cladd.services.ApiInterface;
import com.cladd.services.DataBaseHelper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.hopeland.pda.example.R;
import com.pda.mcu.MCUAdapter;
import com.port.Adapt;
import com.util.BaseActivity;
import com.pda.mcu.MCU;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ItemMainActivity extends BaseActivity {

	GridView gridView;

	ArrayList<HashMap<String ,Object>> listItemArrayList=new ArrayList<HashMap<String,Object>>();

	private static final int REQUEST_READ_PHONE_STATE = 1;


	DataBaseHelper dataBaseHelper;

	String BaseUrlApi = "http://mail.cladd.com.ar:8803/TestIntegrationStockIT/";
	Boolean AjusteProd = false;

	private void checkPermission() {
		//Check if the permission (NEED_PERMISSION) is authorized, PackageManager.PERMISSION_GRANTED means you agree with the authorization.
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
				!= PackageManager.PERMISSION_GRANTED) {
			//ask for permission
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
		} else {
			initView();

		}
	}

	void initView() {
		ShowPopUpOperario();
		Adapt.init(this);
		Adapt.enablePauseInBackGround(this);
		listItemArrayList.clear();

		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (AjusteProd) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}


		//Generate an adapter ImageItem corresponding to the elements of a dynamic array
		SimpleAdapter saImageItems = new SimpleAdapter(this,
				listItemArrayList,//Data source
				R.layout.grid_item,//XML of the item

				//The child of the dynamic array corresponding to ImageItem
				new String[]{"itemImage", "itemText"},

				//An ImageView,TextView ID in the XML file of the ImageItem
				new int[]{R.id.grid_item_image, R.id.grid_item_txt});
		//Add and display
		gridView.setAdapter(saImageItems);
		//Add Message Handling
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//Toast.makeText(getActivity(),name[position],Toast.LENGTH_LONG).show();
				if (isFastClick()) {
					return;
				}
				HashMap<String, Object> map = listItemArrayList.get(position);

				if (getString(R.string.btn_MainMenu_Version).equals(map.get("itemText"))) {
					GetVersion(view);
					return;
				} else if (map.get("itemText").equals(getString(R.string.btn_MainMenu_SerialNumber))) {
					GetSerialNumber(view);
					return;
				}

				Intent intent = new Intent();
				intent.setClass(ItemMainActivity.this, (Class<?>) map.get("itemActivity"));

				startActivity(intent);
			}
		});

		showCustomBar(getString(R.string.tv_MainMenu_Title),
				getString(R.string.str_exit), null,
				R.drawable.left, 0,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Exit(v);
					}
				},
				null
		);

	}

	void initViewTest() {
		Adapt.init(this);
		Adapt.enablePauseInBackGround(this);
		listItemArrayList.clear();

		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // version
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}
		if (true) { // serial number
			HashMap<String, Object> map=new HashMap<String,Object>();
			map.put("itemImage", R.drawable.serialno);
			map.put("itemText", getString(R.string.btn_MainMenu_SerialNumber));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}


		//Generate an adapter ImageItem corresponding to the elements of a dynamic array
		SimpleAdapter saImageItems = new SimpleAdapter(this,
				listItemArrayList,//Data source
				R.layout.grid_item,//XML of the item

				//The child of the dynamic array corresponding to ImageItem
				new String[]{"itemImage", "itemText"},

				//An ImageView,TextView ID in the XML file of the ImageItem
				new int[]{R.id.grid_item_image, R.id.grid_item_txt});
		//Add and display
		gridView.setAdapter(saImageItems);
		//Add Message Handling
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//Toast.makeText(getActivity(),name[position],Toast.LENGTH_LONG).show();
				if (isFastClick()) {
					return;
				}
				HashMap<String, Object> map = listItemArrayList.get(position);

				if (getString(R.string.btn_MainMenu_Version).equals(map.get("itemText"))) {
					GetVersion(view);
					return;
				} else if (map.get("itemText").equals(getString(R.string.btn_MainMenu_SerialNumber))) {
					GetSerialNumber(view);
					return;
				}

				Intent intent = new Intent();
				intent.setClass(ItemMainActivity.this, (Class<?>) map.get("itemActivity"));

				startActivity(intent);
			}
		});

		showCustomBar(getString(R.string.tv_MainMenu_Title),
				getString(R.string.str_exit), null,
				R.drawable.left, 0,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Exit(v);
					}
				},
				null
		);

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// create
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.item_main);
		keepScreenOn(true);
		gridView = (GridView) findViewById(R.id.main_item_grid);

		ChangeLayout(getResources().getConfiguration());
		checkPermission();
		dataBaseHelper=new DataBaseHelper(ItemMainActivity.this);

	}

	private void ChangeLayout(Configuration newConfig) {
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { //The current screen is landscape
			gridView.setNumColumns(4);

		} else { // The current screen is vertical
			gridView.setNumColumns(3);
		}
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		ChangeLayout(newConfig);
	}

	@Override
	protected void onPause() {
		DisposeAll();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		DisposeAll();
		super.onDestroy();
	}

	public void GetVersion(View v) {

		boolean mcuSupport = false;

		try {
			mcuSupport = Adapt.getPropertiesInstance().support("MCU");
		} catch (Exception e) {
		}

		String sdkVersion = Adapt.getVersion();

		if (!mcuSupport) {// The K3 and G3 do not support the MCU
			super.showTip("APP:" + getVersion() + "\n" + "SDK:"
					+ sdkVersion);
		} else {
			String mcuVersion = "";
			MCU mcu = MCUAdapter.getMCUInstance();
			if (mcu.OpenConnect()) {
				mcuVersion = mcu.GetInformation();
				mcu.CloseConnect();
			}

			super.showMsg("APP:" + getVersion() + "\n" + "SDK:"
					+ sdkVersion + "\n" + "MCU:" + mcuVersion, null);
		}
	}

	/**
	 * Get android device serial number
	 */
	public void GetSerialNumber(View v) {

		String serial = Adapt.getPropertiesInstance().getSN();
		showMsg(serial, null);
	}

	/**
	 * Exit application
	 */
	public void Exit(View v) {
		DisposeAll();
		ItemMainActivity.this.finish();
		System.exit(0);
	}

	/**
	 * Release all objects
	 */
	public void DisposeAll() {
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_READ_PHONE_STATE) {

			initView();
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	protected void ShowPopUpOperario(){
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT );

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.tv_title_Login));
		builder.setCancelable(false);
		builder.setView(input);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!input.getText().toString().equals("")) {
					if (input.getText().toString().toLowerCase(Locale.ROOT).equals("admin#stockit"))
					{
						initViewTest();
					}
					else if( input.getText().toString().equals("admin#cladd"))
					{
						AjusteProd = true;
						showWait(getString(R.string.waiting));
						GetOperarioApi(input.getText().toString());
					}
					else
					{
						showWait(getString(R.string.waiting));
						GetOperarioApi(input.getText().toString());
					}
				}else{
					ShowPopUpOperario();
				}
			}
		});
		builder.show();
	}

	/**
	 * API
	 */

	public void GetOperarioApi(String codigoOperario){
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BaseUrlApi)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiInterface json = retrofit.create(ApiInterface.class);

		Map<String, Object> jsonParams = new LinkedHashMap<>();

		jsonParams.put("CodigoOperario",codigoOperario);

		RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(jsonParams)).toString());
		Call<JsonElement> call = json.getOperario(body);
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					if (response.isSuccessful()) {
						JSONObject j = new JSONObject(response.body().toString());

						JSONObject responseState = new JSONObject(j.getString("responseState"));


						if (responseState.getBoolean("isError")) {

							showMsg(responseState.getString("errorMessage") + "\n" + responseState.getString("observacion"));
							ShowPopUpOperario();

						} else {

							JSONObject dataSource = new JSONObject(j.getString("dataSource"));

							String descripcion = dataSource.getString("descripcion");
							String habilitaUsoTracker = dataSource.getString("habilitaUsoTracker");
							String planta = dataSource.getString("planta");

							OperarioApi oper = new OperarioApi();
							oper.setDescripcion(descripcion);
							oper.setHabilitaUsoTracker(habilitaUsoTracker);
							oper.setPlanta(planta);
							oper.setLogeo(codigoOperario);

							dataBaseHelper.addBaseConfig("_OperarioLog",codigoOperario);
							dataBaseHelper.addBaseConfig("_OperarioDesc",oper.getDescripcion());
							dataBaseHelper.addBaseConfig("_OperarioUsoTracker",oper.getHabilitaUsoTracker());
							dataBaseHelper.addBaseConfig("_OperarioPlanta",oper.getPlanta());


							showCustomBar(getString(R.string.tv_MainMenu_Title),
									getString(R.string.str_exit), null,
									R.drawable.left, 0,
									new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											Exit(v);
										}
									},
									null
							);
							// GetColoresApi();
							// GetProductosApi();

							hideWait();
						}
					}
				} catch (Exception ex) {
					hideWait();
					showMsg(ex.getMessage());
					ShowPopUpOperario();

				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				hideWait();
				showMsg(t.getMessage());
				ShowPopUpOperario();
			}
		});

	}

	public void GetBaseConfigApi() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(dataBaseHelper.getBaseConfigData("endpoint"))
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiInterface json = retrofit.create(ApiInterface.class);

		Call<JsonElement> call = json.getBaseConfig();
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					if (response.isSuccessful()) {
						JSONObject j = new JSONObject(String.valueOf(response.body()));

						JSONObject responseState = new JSONObject(String.valueOf(j.getJSONObject("responseState")));

						if (responseState.getBoolean("isError")) {
							dataBaseHelper.setBaseConfigHardCoded();
						} else {

							ArrayList<BaseConfigApi> baseconfig = new Gson().fromJson(j.getJSONArray("dataSource")
									.toString(), new TypeToken<ArrayList<BaseConfigApi>>() {
							}.getType());
							dataBaseHelper.setBaseConfig(baseconfig);
						}
					}
				} catch (JSONException e) {
					dataBaseHelper.setBaseConfigHardCoded();
					e.printStackTrace();
				}
			}


			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {

				dataBaseHelper.setBaseConfigHardCoded();
			}
		});

	}

	public void GetColoresApi() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(dataBaseHelper.getBaseConfigData("endpoint"))
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiInterface json = retrofit.create(ApiInterface.class);

		Map<String, Object> jsonParams = new LinkedHashMap<>();
		jsonParams.put("fecha", dataBaseHelper.getDateLastColor());

		RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(jsonParams)).toString());

		Call<JsonElement> call = json.getColores(body);

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					if (response.isSuccessful()) {
						JSONObject j = new JSONObject(String.valueOf(response.body()));

						JSONObject responseState = new JSONObject(String.valueOf(j.getJSONObject("responseState")));

						if (responseState.getBoolean("isError")) {

						} else {

							ArrayList<ColorApi> colores = new Gson().fromJson(j.getJSONArray("dataSource")
									.toString(), new TypeToken<ArrayList<ColorApi>>() {
							}.getType());
							dataBaseHelper.setColores(colores);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}


			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {

			}
		});

	}

	public void GetProductosApi() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(dataBaseHelper.getBaseConfigData("endpoint"))
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiInterface json = retrofit.create(ApiInterface.class);

		Map<String, Object> jsonParams = new LinkedHashMap<>();
		jsonParams.put("fecha", dataBaseHelper.getDateLastProducto());

		RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(jsonParams)).toString());

		Call<JsonElement> call = json.getProductos(body);
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					if (response.isSuccessful()) {
						JSONObject j = new JSONObject(String.valueOf(response.body()));

						JSONObject responseState = new JSONObject(String.valueOf(j.getJSONObject("responseState")));

						if (responseState.getBoolean("isError")) {
						} else {

							ArrayList<ProductoApi> productos = new Gson().fromJson(j.getJSONArray("dataSource")
									.toString(), new TypeToken<ArrayList<ProductoApi>>() {
							}.getType());
							dataBaseHelper.setProductos(productos);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}


			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				Log.d("","askljnalsnal");
			}
		});

	}

	/**
	 * End API
	 */ // API

}
