package com.cladd;


import static com.cladd.services.DataBaseHelper.DYNAMICCONFIG;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import com.cladd.entities.api.DynamicConfig;
import com.cladd.entities.api.Colores;
import com.cladd.entities.api.Productos;
import com.cladd.modulos.Estado4Activity;
import com.cladd.modulos.GrabarContenedorActivity;
import com.cladd.modulos.FinderBaseActivity;
import com.cladd.modulos.GrabarPiezaActivity;
import com.cladd.modulos.InventarioActivity;
import com.cladd.modulos.TrackeoActivity;
import com.cladd.modulos.VerDetallePiezaActivity;
import com.cladd.premodulo.VerDetalleContenedorActivity;
import com.cladd.modulos.VerificacionActivity;
import com.cladd.services.ApiInterface;
import com.cladd.services.DataBaseHelper;
import com.cladd.services.UpdateAppService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.hopeland.pda.example.R;
import com.port.Adapt;
import com.util.BaseActivity;

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

	private static final int MSG_UPDATEAPP_OK = 1212;
	Boolean Kevin = Boolean.FALSE;
	boolean activeActivity = false;
	private long lastClickTime = 0;

	GridView gridView;

	ArrayList<HashMap<String, Object>> listItemArrayList = new ArrayList<HashMap<String, Object>>();

	private static final int REQUEST_READ_PHONE_STATE = 1;


	DataBaseHelper dataBaseHelper;

	String BaseUrlApi = null;

	boolean Config = false; // Ajustes Publicos
	boolean MasterConfig = false; // Ajustes De Admin
	boolean VerDetalle = false; // Ver Detalle Pieza
	boolean VerDetalleContenedor = false; // Ver Detalle contenedor
	boolean GrabarPieza = false; // Grabacion de etiquetas de piezas
	boolean GrabarContenedor = false; // Grabacion de etiquetas de contenedor
	boolean Finder = false; // Buscador de piezas por algun criterio
	boolean Trackeo = false; // Precarga de piezas al contenedor
	boolean Verificacion = false; // Carga de pieza al contenedor
	boolean Estado4 = false; // Carga de pieza sin asignar al contenedor
	boolean Inventario = false; // Control de cantidad de piezas

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
		Adapt.init(this);
		Adapt.enablePauseInBackGround(this);
		listItemArrayList.clear();

		if (Config) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.config);
			map.put(getString(R.string.grid_itemText), getString(R.string.Config));
			map.put(getString(R.string.grid_itemActivity), ConfigActivity.class);
			listItemArrayList.add(map);
		} // Config
		if (MasterConfig) { // version
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.masterconfig);
			map.put(getString(R.string.grid_itemText), getString(R.string.MasterConfig));
			map.put(getString(R.string.grid_itemActivity), MasterConfigActivity.class);
			listItemArrayList.add(map);
		} // MasterConfig
		if (VerDetalle) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.verdetalle);
			map.put(getString(R.string.grid_itemText), getString(R.string.VerDetallePieza));
			map.put(getString(R.string.grid_itemActivity), VerDetallePiezaActivity.class);
			listItemArrayList.add(map);
		} // VerDetalle
		if (VerDetalleContenedor) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.verdetallecontenedor);
			map.put(getString(R.string.grid_itemText), getString(R.string.VerDetalleContenedor));
			map.put(getString(R.string.grid_itemActivity), VerDetalleContenedorActivity.class);
			listItemArrayList.add(map);
		} // VerDetalleContenedor
		if (GrabarPieza) { // version
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.grabarpieza);
			map.put(getString(R.string.grid_itemText), getString(R.string.GrabarPieza));
			map.put(getString(R.string.grid_itemActivity), GrabarPiezaActivity.class);
			listItemArrayList.add(map);
		} // GrabarPieza
		if (GrabarContenedor) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.grabarcontenedor);
			map.put(getString(R.string.grid_itemText), getString(R.string.GrabarContenedor));
			map.put(getString(R.string.grid_itemActivity), GrabarContenedorActivity.class);
			listItemArrayList.add(map);
		} // GrabarContenedor
		if (Finder) { // version
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.finder);
			map.put(getString(R.string.grid_itemText), getString(R.string.Finder));
			map.put(getString(R.string.grid_itemActivity), FinderBaseActivity.class);
			listItemArrayList.add(map);
		} // Finder
		if (Trackeo) { // version
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.trackeo);
			map.put(getString(R.string.grid_itemText), getString(R.string.Trackeo));
			map.put(getString(R.string.grid_itemActivity), TrackeoActivity.class);
			listItemArrayList.add(map);
		} // Trackeo
		if (Verificacion) { // version
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.verificar);
			map.put(getString(R.string.grid_itemText), getString(R.string.Verificacion));
			map.put(getString(R.string.grid_itemActivity), VerificacionActivity.class);
			listItemArrayList.add(map);
		} // Verificacion
		if (Estado4) { // version
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.estado4);
			map.put(getString(R.string.grid_itemText), getString(R.string.Estado4));
			map.put(getString(R.string.grid_itemActivity), Estado4Activity.class);
			listItemArrayList.add(map);
		} // Estado4
		if (Inventario) { // version
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.inventario);
			map.put(getString(R.string.grid_itemText), getString(R.string.Inventario));
			map.put(getString(R.string.grid_itemActivity), InventarioActivity.class);
			listItemArrayList.add(map);
		} // Inventario
		if (true) { // version
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(getString(R.string.grid_itemImage), R.drawable.update);
			map.put(getString(R.string.grid_itemText), "Actualizar");
			map.put(getString(R.string.grid_itemActivity), null);
			listItemArrayList.add(map);
		} // Actualizar
		//Generate an adapter ImageItem corresponding to the elements of a dynamic array
		SimpleAdapter saImageItems = new SimpleAdapter(this,
				listItemArrayList,//Data source
				R.layout.grid_item,//XML of the item

				//The child of the dynamic array corresponding to ImageItem
				new String[]{getString(R.string.grid_itemImage), getString(R.string.grid_itemText)},

				//An ImageView,TextView ID in the XML file of the ImageItem
				new int[]{R.id.grid_item_image, R.id.grid_item_txt});
		//Add and display
		gridView.setAdapter(saImageItems);

		//Add Message Handling
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!activeActivity) {

					activeActivity = true;
					HashMap<String, Object> map = listItemArrayList.get(position);
					if ("Actualizar".equals(map.get("itemText"))) {
						UpdateAppService u = new UpdateAppService();

						u.DoUpdate(ItemMainActivity.this, "https://raw.githubusercontent.com/PomeloCode/CladdPDA/main/StockITCladd/");
						return;
					}
					Intent intent = new Intent();
					intent.setClass(ItemMainActivity.this, (Class<?>) map.get(getString(R.string.grid_itemActivity)));

					startActivity(intent);
				}
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

		hideWait();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// create
		super.onCreate(savedInstanceState);


		this.setContentView(R.layout.item_main);
		gridView = (GridView) findViewById(R.id.main_item_grid);
		dataBaseHelper = new DataBaseHelper(ItemMainActivity.this);
		dataBaseHelper.addDynamicConfigsDefault();
		keepScreenOn(true);
		checkPermission();
		ChangeLayout(getResources().getConfiguration());

		BaseUrlApi = getString(R.string.GENERICENDPOINT);

		ShowPopUpOperario();

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
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		activeActivity = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Exit application
	 */
	public void Exit(View v) {
		ItemMainActivity.this.finish();
		System.exit(0);
	}

	/**
	 * Release all objects
	 */

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_READ_PHONE_STATE) {

			initView();
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	protected void ShowPopUpOperario() {
		if(!Kevin) {
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_TEXT);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.tv_title_Login));
			builder.setCancelable(false);
			builder.setView(input);
			builder.setPositiveButton(getString(R.string.GENERICPOSITIVEBUTTON), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (!input.getText().toString().equals(new String())) {
						if (input.getText().toString().toLowerCase(Locale.ROOT).equals("a"/*"admin#stockit"*/))
							MasterLogin();
						else if (input.getText().toString().equals("admin#cladd")) {
							CladdLogin("admin#cladd");
						} else {
							OperarioLogin(input.getText().toString());
						}
					} else {
						ShowPopUpOperario();
					}
				}
			});
			builder.show();
		}else
			MasterLogin();

	}

	protected void MasterLogin(){
		MasterConfig = true;
		showWait(getString(R.string.waiting));
		GetDynamicConfigsApi();
		InsertLoginToDatabaseAdmin();
	}

	protected void CladdLogin(String oper){
		Config = true;

		OperarioLogin("admin#cladd");

	}

	protected void OperarioLogin(String oper){
		showWait(getString(R.string.waiting));
		GetOperarioApi(oper);

	}

	/**
	 * API
	 */

	/** Obtiene la informacion del operario */
	public void GetOperarioApi(String codigoOperario) {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BaseUrlApi)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiInterface json = retrofit.create(ApiInterface.class);

		Map<String, Object> jsonParams = new LinkedHashMap<>();

		jsonParams.put("CodigoOperario", codigoOperario);

		RequestBody body = RequestBody.create(okhttp3.MediaType.parse(getString(R.string.API_CONTENTTYPE)), (new JSONObject(jsonParams)).toString());
		Call<JsonElement> call = json.getOperario(body);
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					if (response.isSuccessful()) {
						JSONObject j = new JSONObject(response.body().toString());

						JSONObject responseState = new JSONObject(j.getString(getString(R.string.API_RESPONSESTATE)));

						if (responseState.getBoolean(getString(R.string.API_RESPONSESTATE_ISERROR))) {

							showMsg(responseState.getString(getString(R.string.API_RESPONSESTATE_ERRORMESSAGE)) + getString(R.string.str_br) + responseState.getString(getString(R.string.API_RESPONSESTATE_OBSERVACION)));
							ShowPopUpOperario();

						} else {

							JSONObject dataSource = new JSONObject(j.getString(getString(R.string.API_DATASOURCE)));

							dataBaseHelper.RestartTable(DYNAMICCONFIG);
							GetDynamicConfigsApi();
							GetColoresApi();
							GetProductosApi();
							InsertLoginToDatabase(codigoOperario,dataSource);

						}
					}
				} catch (Exception ex) {

					showMsg(ex.getMessage());
					ShowPopUpOperario();

				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				showMsg(t.getMessage());
				ShowPopUpOperario();
			}
		});

	}

	/** Rellena nuestra tabla de configuraciones  */
	public void GetDynamicConfigsApi() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BaseUrlApi)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiInterface json = retrofit.create(ApiInterface.class);

		Call<JsonElement> call = json.getDynamicConfigs();
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					if (response.isSuccessful()) {
						JSONObject j = new JSONObject(String.valueOf(response.body()));

						JSONObject responseState = new JSONObject(String.valueOf(j.getJSONObject(getString(R.string.API_RESPONSESTATE))));

						if (responseState.getBoolean(getString(R.string.API_RESPONSESTATE_ISERROR))) {
						} else {

							ArrayList<DynamicConfig> baseconfig = new Gson().fromJson(j.getJSONArray(getString(R.string.API_DATASOURCE))
									.toString(), new TypeToken<ArrayList<DynamicConfig>>() {
							}.getType());

							synchronized (dataBaseHelper.setDynamicConfigs(baseconfig))
							{
								BaseUrlApi = dataBaseHelper.getDynamicConfigsData(getString(R.string.API_ENDPOINT));

								VerDetalle = Boolean.parseBoolean( dataBaseHelper.getDynamicConfigsData(getString(R.string.VerDetallePieza)));
								VerDetalleContenedor = Boolean.parseBoolean( dataBaseHelper.getDynamicConfigsData(getString(R.string.VerDetalleContenedor)));
								GrabarPieza = Boolean.parseBoolean( dataBaseHelper.getDynamicConfigsData(getString(R.string.GrabarPieza)));
								GrabarContenedor = Boolean.parseBoolean( dataBaseHelper.getDynamicConfigsData(getString(R.string.GrabarContenedor)));
								Finder = Boolean.parseBoolean( dataBaseHelper.getDynamicConfigsData(getString(R.string.Finder)));
								Trackeo = Boolean.parseBoolean( dataBaseHelper.getDynamicConfigsData(getString(R.string.Trackeo)));
								Verificacion = Boolean.parseBoolean( dataBaseHelper.getDynamicConfigsData(getString(R.string.Verificacion)));
								Estado4 = Boolean.parseBoolean( dataBaseHelper.getDynamicConfigsData(getString(R.string.Estado4)));
								Inventario = Boolean.parseBoolean( dataBaseHelper.getDynamicConfigsData(getString(R.string.Inventario)));

								initView();
							};

						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}


			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				showMsg(getString(R.string.str_faild));
			}
		});

	}

	/** Rellena nuestras tablas en la base de datos de Colores */
	public void GetColoresApi() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BaseUrlApi)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiInterface json = retrofit.create(ApiInterface.class);

		Map<String, Object> jsonParams = new LinkedHashMap<>();
		jsonParams.put(getString(R.string.API_FECHA), dataBaseHelper.getDateLastColor());

		RequestBody body = RequestBody.create(okhttp3.MediaType.parse(getString(R.string.API_CONTENTTYPE)), (new JSONObject(jsonParams)).toString());

		Call<JsonElement> call = json.getColores(body);

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					if (response.isSuccessful()) {
						JSONObject j = new JSONObject(String.valueOf(response.body()));

						JSONObject responseState = new JSONObject(String.valueOf(j.getJSONObject(getString(R.string.API_RESPONSESTATE))));

						if (responseState.getBoolean(getString(R.string.API_RESPONSESTATE_ISERROR))) {

						} else {

							ArrayList<Colores> colores = new Gson().fromJson(j.getJSONArray(getString(R.string.API_DATASOURCE))
									.toString(), new TypeToken<ArrayList<Colores>>() {
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

	/** Rellena nuestras tablas en la base de datos de Productos */
	public void GetProductosApi() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BaseUrlApi)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		ApiInterface json = retrofit.create(ApiInterface.class);

		Map<String, Object> jsonParams = new LinkedHashMap<>();
		jsonParams.put("fecha", dataBaseHelper.getDateLastProducto());

		RequestBody body = RequestBody.create(okhttp3.MediaType.parse(getString(R.string.API_CONTENTTYPE)), (new JSONObject(jsonParams)).toString());

		Call<JsonElement> call = json.getProductos(body);
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					if (response.isSuccessful()) {
						JSONObject j = new JSONObject(String.valueOf(response.body()));

						JSONObject responseState = new JSONObject(String.valueOf(j.getJSONObject(getString(R.string.API_RESPONSESTATE))));

						if (responseState.getBoolean(getString(R.string.API_RESPONSESTATE_ISERROR))) {
						} else {

							ArrayList<Productos> productos = new Gson().fromJson(j.getJSONArray(getString(R.string.API_DATASOURCE))
									.toString(), new TypeToken<ArrayList<Productos>>() {
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
				showMsg(getString(R.string.str_faild));
			}
		});

	}

	/**
	 * End API
	 * @param codigoOperario
	 * @param dataSource
	 */ // API

	/** Inicializa el operario  para poder operar en la app */
	public void InsertLoginToDatabase(String codigoOperario, JSONObject dataSource) {

		String descripcion = null;
		String planta = null;
		String habilitaUsoTracker = null;

		try {
			descripcion = dataSource.getString(getString(R.string.DESCRIPCION));
			planta = dataSource.getString(getString(R.string.PLANTA));
			habilitaUsoTracker = dataSource.getString(getString(R.string.HABILITAUSOTRACKER));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		dataBaseHelper.addDynamicConfigs(getString(R.string.DB_DynamicConfig_Type_BaseConfig),getString(R.string.OPERARIOLOG), codigoOperario);
		dataBaseHelper.addDynamicConfigs(getString(R.string.DB_DynamicConfig_Type_BaseConfig),getString(R.string.OPERARIODESC), descripcion);
		dataBaseHelper.addDynamicConfigs(getString(R.string.DB_DynamicConfig_Type_BaseConfig),getString(R.string.OPERARIOPLANTA), planta);
		dataBaseHelper.addDynamicConfigs(getString(R.string.DB_DynamicConfig_Type_BaseConfig),getString(R.string.OPERARIOUSOTRACKER), habilitaUsoTracker);
	}

	/** Inicializa el operario admin para poder operar en la app */
	public void InsertLoginToDatabaseAdmin() {

		String descripcion = getString(R.string.OPERARIODEFAULTDESC);
		String planta = getString(R.string.OPERARIODEFAULTPLANTA);
		String habilitaUsoTracker = getString(R.string.OPERARIODEFAULTUSOTRACKER);

		dataBaseHelper.addDynamicConfigs(getString(R.string.DB_DynamicConfig_Type_BaseConfig),getString(R.string.OPERARIOLOG),getString(R.string.OPERARIODEFAULTLOG));
		dataBaseHelper.addDynamicConfigs(getString(R.string.DB_DynamicConfig_Type_BaseConfig),getString(R.string.OPERARIODESC),descripcion);
		dataBaseHelper.addDynamicConfigs(getString(R.string.DB_DynamicConfig_Type_BaseConfig),getString(R.string.OPERARIOPLANTA), planta);
		dataBaseHelper.addDynamicConfigs(getString(R.string.DB_DynamicConfig_Type_BaseConfig),getString(R.string.OPERARIOUSOTRACKER), habilitaUsoTracker);



	}
}
