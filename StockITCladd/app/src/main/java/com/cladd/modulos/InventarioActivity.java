package com.cladd.modulos;

import static java.lang.Thread.sleep;

import android.content.DialogInterface;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cladd.entities.api.Operario;
import com.cladd.entities.model.ComplexEPC;
import com.cladd.entities.model.Inventario;
import com.cladd.entities.model.InventarioDetalle;
import com.cladd.entities.model.PDASettings;
import com.cladd.services.DataBaseHelper;
import com.cladd.uhf.UHFBaseActivity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.hopeland.pda.example.R;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.rfid.uhf.UHFReader;
import com.util.Helper.Helper_ThreadPool;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class InventarioActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	/**
	 * INICIO Definicion de elementos de la vista
	 **/



	private ListView listView = null;
	private EditText et_Inventario_Titulo = null;
	private TextView tv_UsuarioLogueado = null;
	private TextView lb_TagCount = null;
	private Button btn_Inventario = null;
	private Button btn_Inventario_Operacion = null;

	/** FIN Definicion de elementos de la vista **/

	/**
	 * Inicio Definicion Variables Controller
	 **/

	private HashMap<String, ComplexEPC> hmList = new HashMap<String, ComplexEPC>();
	private HashMap<String, Integer> hmListProd = new HashMap<String, Integer>();
	private HashMap<String, Integer> hmListProdCol = new HashMap<String, Integer>();

	private HashMap<String, String> ColoresEnBase = new HashMap<>();
	private HashMap<String, String> ProductosEnBase = new HashMap<>();
	private String FechaInicio = null;

	/* Variable Global */


	/* MESSAGGE Handling */

	private final int MSG_RESULT_READ = MSG_USER_BEG + 1; //constant

	/* Logging */

	private int cambioPotAntena = 0;
	private long timerBtnOperacion = 0;
	private long timerBtnLeer = 0;

	/** FIN Definicion Variables Controller **/

	/**
	 * Controller
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// create
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		InitRFIDModule();

	}

	@Override
	protected void onResume() {
		super.onResume();
		ReInitRFIDModule();
		super.UHF_GetReaderProperty();
	}

	protected void ReInitRFIDModule() {
		log = this;
		if (!UHF_Init(log)) { // Failed to power on the module
			showMsg(getString(R.string.RFID_ErrorConexion),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							InventarioActivity.this.finish();
						}
					});
		}
	}

	protected void InitRFIDModule() {
		log = this;
		if (!UHF_Init(log)) { // Failed to power on the module
			showMsg(getString(R.string.RFID_ErrorConexion),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							InventarioActivity.this.finish();
						}
					});
		} else {
			InitSoundModule();

		}
	}

	protected void ConfigureRFIDModule() {

		try {

			PDASettings pda = FillPDASettings(GetPDAConfigDB(getString(R.string.Inventario)),getString(R.string.Inventario));
			SetPDAConfigurations(pda);

		} catch (Exception ee) {
			Log.d("as",ee.getMessage());
		}


	}

	protected void InitSoundModule() {
		IsFlushList = true;

		Helper_ThreadPool.ThreadPool_StartSingle(new Runnable() { // The buzzer sounds
			@Override
			public void run() {
				while (IsFlushList) {
					synchronized (beep_Lock) {
						try {
							beep_Lock.wait();
						} catch (InterruptedException e) {
						}
					}
					if (IsFlushList) {
						toneGenerator
								.startTone(ToneGenerator.TONE_PROP_BEEP);
					}

				}
			}
		});

		InitDBConexion();

	}

	protected void InitDBConexion() {

		dataBaseHelper = new DataBaseHelper(InventarioActivity.this);

		BaseUrlApi = dataBaseHelper.getDynamicConfigsData(getString(R.string.API_ENDPOINT));

		GetOperarioDB();
		GetDataDB();
		ConfigureRFIDModule();

		InitView();


	}

	public void GetOperarioDB() {

		_Operario = new Operario();

		_Operario.setLogeo(dataBaseHelper.getDynamicConfigsData(getString(R.string.OPERARIOLOG)));
		_Operario.setDescripcion(dataBaseHelper.getDynamicConfigsData(getString(R.string.OPERARIODESC)));
		;
		_Operario.setHabilitaUsoTracker(dataBaseHelper.getDynamicConfigsData(getString(R.string.OPERARIOUSOTRACKER)));
		_Operario.setPlanta(dataBaseHelper.getDynamicConfigsData(getString(R.string.OPERARIOPLANTA)));

	}

	public void GetDataDB() {

		ColoresEnBase = dataBaseHelper.getColoresList();
		ProductosEnBase = dataBaseHelper.getProductosList();
	}

	public void SetAntennaConfigurations(String funcion) {

		SetBaseBand("4", "4", "1", "0", "false", "false");
		cambioPotAntena = UHFReader._Config.SetANTPowerParam(_NowAntennaNo, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_AntPowerTrackerTrack))));

		if (cambioPotAntena != 0)
			showMsg(getString(R.string.RFID_ErrorCambioPotencia));


	}

	protected void InitView() {

		this.setContentView(R.layout.inventario);

		showCustomBar(getString(R.string.tv_Inventario_Title),
				getString(R.string.str_back), null,
				R.drawable.left, 0,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Back();
					}
				},
				null
		);

		BindViews();
	}

	protected void BindViews() {

		listView = (ListView) this.findViewById(R.id.lv_Main);
		tv_UsuarioLogueado = (TextView) findViewById(R.id.tv_UsuarioLogueado);
		lb_TagCount = (TextView) findViewById(R.id.lb_TagCount);
		btn_Inventario = (Button) findViewById(R.id.btn_Inventario);
		et_Inventario_Titulo = (EditText) findViewById(R.id.et_Inventario_Titulo);
		btn_Inventario_Operacion = (Button) findViewById(R.id.btn_Inventario_Operacion);

		InitListeners();

		DisplayData();

		InitListViewUpdateThread();
	}

	protected void DisplayData() {

		tv_UsuarioLogueado.setText(_Operario.getDescripcion());

		btn_Inventario.setText(getString(R.string.btn_Inventario));

		Date date = new Date();
		FechaInicio = (dateFormat.format(date));

	}

	protected void InitListeners() {

		btn_Inventario_Operacion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// preventing double, using threshold of 1000 ms
				if (SystemClock.elapsedRealtime() - timerBtnOperacion < 1000){
					return;
				}

				timerBtnOperacion = SystemClock.elapsedRealtime();

				if (!et_Inventario_Titulo.getText().toString().equals("")) {
					if (hmList.size() > 0)
						MostrarPopUpOperar();
					else
						showMsg("Lea al menos 1 pieza");
				} else
					showMsg("Complete el Titulo del inventario");

			}
		});

		btn_Inventario.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// preventing double, using threshold of 1000 ms
				if (SystemClock.elapsedRealtime() - timerBtnOperacion < 1000){
					return;
				}

				timerBtnOperacion = SystemClock.elapsedRealtime();

				VirtualBtnKeyDown(view);
			}
		});

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@RequiresApi(api = Build.VERSION_CODES.N)
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String selectedItem = ((HashMap) parent.getItemAtPosition(position)).get("EPC").toString();
				String idprod = getKey(ProductosEnBase, selectedItem);

				Map<String, Integer> e = filterMap(hmListProdCol, idprod);

				String vista = "";
				for (Map.Entry<String, Integer> row : e.entrySet()) {

					String prodHex = row.getKey().substring(0, 4);
					String colorHex = row.getKey().substring(4, 8);

					String ColorID = String.valueOf(getDecimal(colorHex));
					String ProductoID = String.valueOf(getDecimal(prodHex));


					String p = ProductosEnBase.get(ProductoID);
					String c = ColoresEnBase.get(ColorID);

					String nvista = vista + c + " : " + row.getValue().toString() + "\n";
					vista = nvista;
				}
				;
				showMsg(vista);
			}
		});

	}

	protected void InitListViewUpdateThread() {

		Helper_ThreadPool.ThreadPool_StartSingle(new Runnable() {
			@Override
			public void run() {
				while (IsFlushList) {
					try {
						sendMessage(MSG_RESULT_READ, null);
						Thread.sleep(20); // Refresh every second
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (TriggerPressed(keyCode)) { // Press the handle button
			StartReading();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void VirtualBtnKeyDown(View v) {
		if (SystemClock.elapsedRealtime() - timerBtnLeer < 1000) {
			return;
		}

		timerBtnLeer = SystemClock.elapsedRealtime();

		Button btnInventario = (Button) v;

		String controlText = btnInventario.getText().toString();

		if (controlText.equals(getString(R.string.btn_Inventario))) {
			StartReading();
		} else
			StopReading();

	}

	public void StartReading() {

		if (!isKeyDown) {
			isKeyDown = true; //
			if (!isStartPingPong) {
				btn_Inventario.setText(getText(R.string.btn_read_stop));
				CLReader.Stop(); // stop
				isStartPingPong = true;
				GetEPC_6C();
			}
		}

	}

	public void RestartInventario() {

		et_Inventario_Titulo.setFocusable(true);
		et_Inventario_Titulo.setFocusableInTouchMode(true);
		et_Inventario_Titulo.setClickable(true);
		et_Inventario_Titulo.setTextColor(Color.BLACK);
		et_Inventario_Titulo.setText(new String());
		hmList.clear();
		et_Inventario_Titulo.setText(new String());
		btn_Inventario.setText(getString(R.string.btn_Inventario));

	}

	private int GetEPC_6C() {

		int ret = -1;
		in_reading = true;

		ret = UHFReader._Tag6C.GetEPC(_NowAntennaNo, _RFIDInventoryRead);

		return ret;

	}

	@Override
	public void OutPutEPC(EPCModel model) {
		if (!isStartPingPong)
			return;
		try {
			synchronized (hmList_Lock) {
				if (model._EPC.substring(2, 4).equals(getString(R.string.StockITBusinessID)) && !hmList.containsKey(model._EPC + model._TID)) {

					synchronized (beep_Lock) {
						beep_Lock.notify();
					}
					ComplexEPC complexEPC = new ComplexEPC();

					complexEPC.setReadedDate(new Date());
					complexEPC.setEpcModel(model);

					hmList.put(model._EPC + model._TID, complexEPC);

					if (hmListProd.containsKey(model._EPC.substring(16, 20))) {

						int cantidadProd = hmListProd.get(model._EPC.substring(16, 20));
						cantidadProd++;
						hmListProd.remove(model._EPC.substring(16, 20));
						hmListProd.put(model._EPC.substring(16, 20), cantidadProd);

						if (hmListProdCol.containsKey(model._EPC.substring(16, 24))) {

							int cantidad = hmListProdCol.get(model._EPC.substring(16, 24));
							cantidad++;
							hmListProdCol.remove(model._EPC.substring(16, 24));
							hmListProdCol.put(model._EPC.substring(16, 24), cantidad);

						} else {

							hmListProdCol.put(model._EPC.substring(16, 24), 1);

						}

					} else {

						hmListProd.put(model._EPC.substring(16, 20), 1);
						hmListProdCol.put(model._EPC.substring(16, 24), 1);
					}
				}
			}
		} catch (Exception ex) {
			Log.d("Debug", "Tags output exceptions:" + ex.getMessage());
		}


	}

	/**
	 * API
	 *
	 * @return
	 */

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void GrabarInventario (String titulo, String oper, String inicio) {
		showWait(getString(R.string.waiting));

		Call<JsonElement> call = GetApiService(BaseUrlApi).SaveInventario(CreateReqBodyAndLogToServerInventario(FillInventario( titulo,  oper, inicio) ));

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					JSONObject resp = new JSONObject(response.body().toString());

					LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_Trackeo_TrackearNombre), 2, Calendar.getInstance().toString(), resp.toString());

					if (IsAPIResponseStateOK(resp, getString(R.string.API_Trackeo_Nombre))) {

						showMsg("Exito!", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Back();
							}
						});
						sendMessage(MSG_HIDE_WAIT, null);

					}

				} catch (Exception ex) {
					sendMessage(MSG_HIDE_WAIT, null);

					showMsg(getString(R.string.GENERICERROR));
				}
			}


			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				sendMessage(MSG_HIDE_WAIT, null);

				showMsg(t.getMessage());
			}
		});

	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public Inventario FillInventario(String titulo, String oper, String inicio) {

		Inventario inventario = new Inventario();

		inventario.setTituloInventario(titulo);
		inventario.setFechaInicio(inicio);
		inventario.setOperario(oper);

		List<InventarioDetalle> piezas = new ArrayList<>();

		Date date = new Date();

		hmList.forEach((key, value) -> {
			InventarioDetalle aux = new InventarioDetalle();

			aux.setF(dateFormat.format(value.readedDate));
			aux.setH(value.epcModel._EPC);

			piezas.add(aux);
		});

		inventario.setPiezas(piezas);

		String fechaCierre = (dateFormat.format(date));
		inventario.setFechaCierre(fechaCierre);

		return inventario;
	}

	public RequestBody CreateReqBodyAndLogToServerInventario(Inventario inventario) {
		RequestBody i = null;
		Gson gson = new Gson();
		try {
			i = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(gson.toJson(inventario))).toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return i;
	}

	/**
	 * End API
	 */

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	protected void msgProcess(Message msg) {
		switch (msg.what) {
			case MSG_RESULT_READ:
				ShowList(); // Show the list
				break;
			default:
				super.msgProcess(msg);
				break;
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	protected void ShowList() {
		if (!isStartPingPong)
			return;
		sa = new SimpleAdapter(this, GetData(), R.layout.epclist_item,
				new String[]{"EPC", "ReadCount"}, new int[]{
				R.id.EPCList_TagID, R.id.EPCList_ReadCount});
		listView.setAdapter(sa);
		listView.invalidate();


		if (lb_TagCount != null) { // Refresh tag count
			lb_TagCount.setText("Total:" + hmList.size());
		}
	}

	protected List<Map<String, Object>> GetData() {
		List<Map<String, Object>> rt = new ArrayList<Map<String, Object>>();

		synchronized (hmList_Lock) {
			Iterator iter = hmListProd.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				int val = (int) entry.getValue();
				Map<String, Object> map = new HashMap<String, Object>();


				String prodHex = key.substring(0, 4);
				//String colorHex = key.substring(4,8);

				//String ColorID = String.valueOf(getDecimal(colorHex));
				String ProductoID = String.valueOf(getDecimal(prodHex));


				String p = ProductosEnBase.get(ProductoID);
				//String c = ColoresEnBase.get(ColorID);

				map.put("EPC", p);

				map.put("ReadCount", val);


				rt.add(map);


			}
		}

		return rt;
	}

	public void StopReading() {
		CLReader.Stop();
		isStartPingPong = false;
		isKeyDown = false;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				btn_Inventario.setText(getString(R.string.btn_Inventario));
				btn_Inventario.setClickable(true);
			}
		});
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (TriggerPressed(keyCode) && isStartPingPong) {
			StopReading();
		}
		return super.onKeyUp(keyCode, event);
	}

	public void MostrarPopUpOperar() {
		showConfim("Desea cerrar el inventario con las " + hmList.size() + " piezas?", "Si", "No"
				, new DialogInterface.OnClickListener() {
					@RequiresApi(api = Build.VERSION_CODES.N)
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ExecuteOperacion();
					}
				}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void ExecuteOperacion() {

		String tituloInventario = et_Inventario_Titulo.getText().toString();
		String operario = _Operario.getLogeo();
		String fechaInicio = FechaInicio;

		GrabarInventario(tituloInventario, operario, fechaInicio);
	}

	public void Clear(View v) {
		hmList.clear();
	}

	@Override
	public void onBackPressed() {
		Back();
	}

	public void Back() {

		if (btn_Inventario.getText().toString()
				.equals(getString(R.string.btn_read_stop))) {
			showMsg(getString(R.string.uhf_please_stop), null);
			return;
		}

		DisposeAll();

		finish();

		InventarioActivity.this.finish();
	}

	@Override
	protected void onDestroy() {
		DisposeAll();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//DisposeAll();
	}

	/**
	 * End Controller
	 */ // Controller



}