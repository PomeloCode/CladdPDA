package com.cladd.modulos;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;

import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;



import com.cladd.entities.api.Contenedor;
import com.cladd.entities.api.Operario;
import com.cladd.entities.api.TrackeoPieza;
import com.cladd.entities.model.PDASettings;
import com.cladd.entities.model.Pieza;
import com.cladd.services.DataBaseHelper;
import com.cladd.uhf.UHFBaseActivity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.hopeland.pda.example.R;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.rfid.uhf.UHFReader;
import com.util.Helper.Helper_ThreadPool;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TrackeoActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	/**
	 * INICIO Definicion de elementos de la vista
	 **/

	private RadioGroup rg_Trackeo = null;
	private RadioButton rb_Trackeo_Carga = null;
	private RadioButton rb_Trackeo_Descarga = null;
	private RadioButton rb_Trackeo_Devolucion = null;
	private EditText et_Trackeo_Nro = null;
	private View v_editContainer = null;
	private ListView listView = null;
	private TextView tv_UsuarioLogueado = null;
	private TextView tv_Trackeo_Leidos = null;
	private TextView tv_Trackeo_Asignados = null;
	private TextView tv_Trackeo_Cargados = null;
	private TextView tv_Trackeo_Verificadas = null;
	private Button btn_Trackeo_Operacion = null;
	private Button btn_Trackeo = null;

	/** FIN Definicion de elementos de la vista **/

	/**
	 * Inicio Definicion Variables Controller
	 **/

	private String Numero = new String();

	private int Asignados = 0;
	private int Cargados = 0;
	private int Verificados = 0;
	private int Leidos = 0;
	private int _Operacion = 0;//0:Carga,1:Descarga,2:Devolucion

	private List<String> LeidosList = new ArrayList<String>();
	private List<Pieza> ProductosLeidosList = new ArrayList<>();

	/* Variable Global */

	private Contenedor containerGlobal = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());
	private Contenedor containerGlobalCargadas = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());

	/* MESSAGGE Handling */

	private final int MSG_RESULT_Trackeo = MSG_USER_BEG + 1;
	private final int MSG_MATCH_READ = MSG_USER_BEG + 2;

	/* Logging */

	private boolean llamo = false;
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



		InitDBConexion();
		InitRFIDModule(this,getString(R.string.Trackeo));
		InitBCModule();
		InitSoundModule();
		InitView();

	}

	@Override
	protected void onResume(){
		super.onResume();
		showWait("Configurando Antena");
		Helper_ThreadPool.ThreadPool_StartSingle(new Runnable() {
			@Override
			public void run() {
				ConfigureRFIDModule(getString(R.string.Trackeo));
			}
		});
	}


	public void SetAntennaConfigurations(String funcion) {

		SetBaseBand("4", "4", "1", "0", "false", "false");
		cambioPotAntena = UHFReader._Config.SetANTPowerParam(_NowAntennaNo, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_AntPowerTrackerTrack))));

		if (cambioPotAntena != 0)
			showMsg(getString(R.string.RFID_ErrorCambioPotencia));


	}

	protected void InitView() {

		this.setContentView(R.layout.trackeo);
		
		showCustomBar(getString(R.string.tv_Trackeo_Title),
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

		rg_Trackeo = findViewById(R.id.rg_Trackeo);
		rb_Trackeo_Carga = findViewById(R.id.rb_Trackeo_Carga);
		rb_Trackeo_Descarga = findViewById(R.id.rb_Trackeo_Descarga);
		rb_Trackeo_Devolucion = findViewById(R.id.rb_Trackeo_Devolucion);
		tv_UsuarioLogueado = findViewById(R.id.tv_UsuarioLogueado);
		v_editContainer = findViewById(R.id.v_editContainer);
		et_Trackeo_Nro = findViewById(R.id.et_Trackeo_Nro);
		listView = findViewById(R.id.lv_Main);
		tv_Trackeo_Leidos = findViewById(R.id.tv_Trackeo_Leidos);
		tv_Trackeo_Asignados = findViewById(R.id.tv_Trackeo_Asignados);
		tv_Trackeo_Cargados = findViewById(R.id.tv_Trackeo_Cargados);
		tv_Trackeo_Verificadas = findViewById(R.id.tv_Trackeo_Verificadas);
		btn_Trackeo = findViewById(R.id.btn_Trackeo);
		btn_Trackeo_Operacion = findViewById(R.id.btn_Trackeo_Operacion);

		InitListeners();

		DisplayData();

		InitListViewUpdateThread();
	}

	protected void DisplayData() {

		tv_UsuarioLogueado.setText(_Operario.getDescripcion());

		v_editContainer.setBackground(getDrawable(R.drawable.lupa));

	}

	protected void InitListeners() {

		et_Trackeo_Nro.addTextChangedListener(new TextWatcher() {

			@RequiresApi(api = Build.VERSION_CODES.O)
			@Override
			public void afterTextChanged(Editable s) {
				if (et_Trackeo_Nro.getText().length() == 24) {
					View view = getCurrentFocus();
					if (view != null) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
										  int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
									  int before, int count) {
				v_editContainer.setBackground(getDrawable(R.drawable.lupa));


			}
		});

		v_editContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Asignados != 0) {
					cambioPotAntena = UHFReader._Config.SetANTPowerParam(1, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_AntPowerTrackerRead))));
					RestartTrackeo();
				} else {
					View currentFocus = getCurrentFocus();
					if (currentFocus != null) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
					}
					sendMessage(MSG_MATCH_READ, et_Trackeo_Nro.getText().toString());
				}
			}
		});

		rg_Trackeo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// checkedId is the RadioButton selected
				RadioButton rb = findViewById(checkedId);
				cambioPotAntena = UHFReader._Config.SetANTPowerParam(1, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_AntPowerTrackerRead))));
				RestartTrackeo();

				btn_Trackeo_Operacion.setText(rb.getText().toString());
				if (rb.getText().toString().equals(getString(R.string.rb_Trackeo_Carga)))
					_Operacion = 0;
				if (rb.getText().toString().equals(getString(R.string.rb_Trackeo_Descarga)))
					_Operacion = 0; //1; Etapa 2
				if (rb.getText().toString().equals(getString(R.string.rb_Trackeo_Devolucion)))
					_Operacion = 0; //2; Etapa 2

			}
		});

		btn_Trackeo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				VirtualBtnKeyDown(view);

			}
		});

		btn_Trackeo_Operacion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// preventing double, using threshold of 1000 ms
				if (SystemClock.elapsedRealtime() - timerBtnOperacion < 1000){
					return;
				}

				timerBtnOperacion = SystemClock.elapsedRealtime();

				if (Leidos > 0)
					MostrarPopUpOperar();

			}
		});

	}

	protected void InitListViewUpdateThread() {

		Helper_ThreadPool.ThreadPool_StartSingle(new Runnable() {
			@Override
			public void run() {
				while (IsFlushList) {
					try {
						sendMessage(MSG_RESULT_Trackeo, null);
						Thread.sleep(20); // Refresh every second
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	public void VirtualBtnKeyDown(View v) {
		if (SystemClock.elapsedRealtime() - timerBtnLeer < 1000){
			return;
		}

		timerBtnLeer = SystemClock.elapsedRealtime();

		Button btnTrackeo = (Button) v;

		String controlText = btnTrackeo.getText().toString();

		if (controlText.equals(getString(R.string.btn_Trackeo_Trackear))) {
			StartReading();
		} else
			StopReading();

	}

	public void StartReading() {

		if (tv_Trackeo_Asignados.getText().toString().equals(getString(R.string.tv_Trackeo_Start))) {

			in_reading = true;
			int ret = -1;
			if (cambioPotAntena == 0) {
				ret = UHFReader._Tag6C.GetEPC(_NowAntennaNo, _RFIDSingleRead);
			}

			int retval = CLReader.GetReturnData(new String());
			if (!UHF_CheckReadResult(retval)) {
				CLReader.Stop();
				in_reading = false;
				return;
			}

		} else {
			if (!isKeyDown) {
				isKeyDown = true; //
				if (!isStartPingPong) {
					btn_Trackeo.setText(getText(R.string.btn_read_stop));
					CLReader.Stop(); // stop
					isStartPingPong = true;
					GetEPC_6C();
				}
			}
		}
	}

	public void RestartTrackeo() {

		et_Trackeo_Nro.setFocusable(true);
		et_Trackeo_Nro.setFocusableInTouchMode(true);
		et_Trackeo_Nro.setClickable(true);
		et_Trackeo_Nro.setTextColor(Color.BLACK);
		et_Trackeo_Nro.setText(new String());
		hmList.clear();
		Asignados = 0;
		Leidos = 0;
		Verificados = 0;
		Cargados = 0;
		et_Trackeo_Nro.setText(new String());
		tv_Trackeo_Verificadas.setText(String.valueOf(Verificados));
		tv_Trackeo_Cargados.setText(String.valueOf(Cargados));

		tv_Trackeo_Leidos.setText(String.valueOf(Asignados));
		tv_Trackeo_Asignados.setText(String.valueOf(Leidos));

		v_editContainer.setBackground(getDrawable(R.drawable.lupa));

		btn_Trackeo.setText(getString(R.string.btn_Trackeo_LeerContenedor));

		containerGlobal = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());
		containerGlobalCargadas = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());
	}

	private int GetEPC_6C() {

		int ret = -1;
		in_reading = true;

		ret = UHFReader._Tag6C.GetEPC(_NowAntennaNo, _RFIDInventoryRead);

		return ret;

	}

	@Override
	public void OutPutEPC(EPCModel model) {

		if (Asignados == 0) {
			hmList.clear();
			sendMessage(MSG_MATCH_READ, model._EPC);
			try {
				synchronized (beep_Lock) {
					beep_Lock.notify();
				}
			} catch (Exception ex) {
			}
		}
		else {
			if (containerGlobal.getTagsList().contains(model._EPC)) {
				// pieza asignada al contenedor
				if (in_reading) {
					in_reading = false;
				}
				if (!isStartPingPong)
					return;
				try {
					synchronized (hmList_Lock) {
						if (!hmList.containsKey(model._EPC + model._TID)) {
							synchronized (beep_Lock) {
								beep_Lock.notify();
							}
							hmList.put(model._EPC + model._TID, model);
						}
					}
				} catch (Exception ex) {
				}
			} else {
				if (containerGlobalCargadas.getTagsList().contains(model._EPC)) {
					// pieza que fue cargada previamente
				} else {
					// pieza que no esta asignada
					if (!model._EPC.substring(0, 4).equals(getString(R.string.RFID_VirginStartTag)))
						if (model._EPC.substring(2, 4).equals(getString(R.string.StockITBusinessID)))
							ShowPopupErrorTrackeo(model._EPC);
				}
			}
		}
	}

	/**
	 * API
	 *
	 * @return
	 */

	protected void GetPiezasATrackear(String num) {

		showWait(getString(R.string.waiting));

		List<String> reqKeys = GetReqKeysList();
		List<Object> reqValues = GetReqValuesList(num, 1);

		Call<JsonElement> call = GetApiService(BaseUrlApi).getContainer(CreateReqBodyAndLogToServer(reqKeys, reqValues, getString(R.string.API_Trackeo_Nombre)));

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				llamo = false;

				try {
					if (response.isSuccessful()) {

						JSONObject resp = new JSONObject(response.body().toString());

						LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_Trackeo_TrackearNombre), 2, Calendar.getInstance().toString(), resp.toString());

						if (IsAPIResponseStateOK(resp, getString(R.string.API_Trackeo_Nombre))) {

							JSONObject dataSource = new JSONObject(resp.getString(getString(R.string.API_DATASOURCE)));

							List<Pieza> piezas = new Gson().fromJson(dataSource.getJSONArray(getString(R.string.API_Trackeo_Res_Piezas)).toString(), new TypeToken<List<Pieza>>() {
							}.getType());

							if (piezas.size() > 0) {

								Numero = (dataSource.getString(getString(R.string.API_Trackeo_Res_ContenedorID)));
								Asignados = Integer.parseInt(dataSource.getString(getString(R.string.API_Trackeo_Res_Asignados)));
								Cargados = Integer.parseInt(dataSource.getString(getString(R.string.API_Trackeo_Res_Cargados)));
								Verificados = Integer.parseInt(dataSource.getString(getString(R.string.API_Trackeo_Res_Verificados)));

								List<String> hexList = new ArrayList<>();
								List<String> hexListCargados = new ArrayList<>();

								for (int i = 0; i < piezas.size(); i++) {
									if (!Boolean.parseBoolean(piezas.get(i).getPiezaCargada()))
										hexList.add(piezas.get(i).getCodigoHexa());
									else
										hexListCargados.add(piezas.get(i).getCodigoHexa());
								}

								FillContainerGlobal(Numero,piezas,hexList,hexListCargados);

								SetAntennaConfigurations(getString(R.string.tv_Trackeo_Title));

								MostrarDatosDeLaAPI();

								btn_Trackeo.setText(getString(R.string.btn_Trackeo_Trackear));
								btn_Trackeo.setClickable(true);

							}

						}

					} else {
						hideWait();
						showMsg(getString(R.string.error_FinderBase_ApiDevolvioRaro));
					}
				} catch (Exception ex) {
					hideWait();
					showMsg(getString(R.string.GENERICERROR) + getString(R.string.GENERICPUNTOS) + getString(R.string.str_br) + ex.getMessage().toString(), null);
				}
				Clear(null);
				hideWait();
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				Clear(null);
				hideWait();
				llamo = false;
				showMsg(t.getMessage(), null);
				LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_FinderBase_Nombre), 4, Calendar.getInstance().toString(), t.getMessage());
			}
		});
	}

	public void FillContainerGlobal(String numero, List<Pieza> piezas, List<String> hexList, List<String> hexListCargados) {

		containerGlobal.setId(Numero);
		containerGlobal.setProductos(piezas);
		containerGlobal.setTagsList(hexList);
		containerGlobalCargadas.setTagsList(hexListCargados);

	}

	public void MostrarDatosDeLaAPI() {

		tv_Trackeo_Cargados.setText(String.valueOf(Cargados));
		tv_Trackeo_Verificadas.setText(String.valueOf(Verificados));
		tv_Trackeo_Asignados.setText(String.valueOf(Asignados));

		et_Trackeo_Nro.setText(String.valueOf(Numero));
		et_Trackeo_Nro.setFocusable(false);
		et_Trackeo_Nro.setTextColor(Color.GRAY);
		et_Trackeo_Nro.setFocusableInTouchMode(false);
		et_Trackeo_Nro.setClickable(false);

		v_editContainer.setBackground(getDrawable(R.drawable.edit));

	}

	public void TrackearApi(String container, int op, List<HashMap<String,String>> piezas, String operario) {
		showWait(getString(R.string.waiting));

		Map<String, Object> jsonPiezas = new LinkedHashMap<>();
		jsonPiezas.put(getString(R.string.API_Trackeo_Req_TrackearPiezas), piezas);

		List<String> reqKeys = GetReqKeysListTrackeo();
		List<Object> reqValues = GetReqValuesListTrackeo(container, op, jsonPiezas,operario);

		Call<JsonElement> call = GetApiService(BaseUrlApi).Trackear(CreateReqBodyAndLogToServer(reqKeys, reqValues, getString(R.string.API_Trackeo_TrackearNombre)));
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				llamo = false;
				try {
					JSONObject resp = new JSONObject(response.body().toString());

					LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_Trackeo_TrackearNombre), 2, Calendar.getInstance().toString(), resp.toString());

					if (IsAPIResponseStateOK(resp, getString(R.string.API_Trackeo_Nombre))) {

							JSONObject dataSource = new JSONObject(resp.getString(getString(R.string.API_DATASOURCE)));

							List<TrackeoPieza> aux = new Gson()
									.fromJson(dataSource.getJSONObject(getString(R.string.API_Trackeo_Res_TrackearPiezas)).getJSONArray(getString(R.string.API_Trackeo_Res_TrackearItems)).toString(),
											new TypeToken<List<TrackeoPieza>>(){}.getType());

							String asignadas = dataSource.getString(getString(R.string.API_Trackeo_Res_TrackearAsignados));
							String cargadas = dataSource.getString(getString(R.string.API_Trackeo_Res_TrackearCargados));
							String verificadas = dataSource.getString(getString(R.string.API_Trackeo_Res_TrackearVerificados));

							showConfim(getString(R.string.str_success), getString(R.string.PopUpTrackeo_Ver), getString(R.string.str_exit)
									, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											MostrarPopUpTrackeo(aux, asignadas,cargadas,verificadas);
										}
									}, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											TrackeoActivity.this.finish();
										}
									});

						}

				} catch (Exception ex) {

					showMsg(getString(R.string.error_Trackeo_ApiNotFound));
				}
			}


			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				Clear(null);
				hideWait();
				showMsg(t.getMessage(), null);
				LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_FinderBase_Nombre), 4, Calendar.getInstance().toString(), t.getMessage());
			}
		});
	}

	protected List<String> GetReqKeysList() {

		List<String> list = new ArrayList<>();

		list.add(getString(R.string.API_Trackeo_Req_ID));
		list.add(getString(R.string.API_Trackeo_Req_Criterio));

		return list;

	}

	protected List<String> GetReqKeysListTrackeo() {

		List<String> list = new ArrayList<>();

		list.add(getString(R.string.API_Trackeo_Req_TrackearContainerID));
		list.add(getString(R.string.API_Trackeo_Req_TrackearOperacion));
		list.add(getString(R.string.API_Trackeo_Req_TrackearPiezas));
		list.add(getString(R.string.API_Trackeo_Req_TrackearOperario));

		return list;

	}

	protected List<Object> GetReqValuesList(String id, int crit) {

		List<Object> list = new ArrayList<>();

		list.add(id);
		list.add(crit);

		return list;

	}

	protected List<Object> GetReqValuesListTrackeo(String cont,int op,Object jsonPiezas, String operario) {

		List<Object> list = new ArrayList<>();

		list.add(cont);
		list.add(op);
		list.add(jsonPiezas);
		list.add(operario);

		return list;

	}

	/**
	 * End API
	 */

	@Override
	protected void msgProcess(Message msg) {
		switch (msg.what) {
			case MSG_RESULT_Trackeo:
				ShowList();
				break;
			case MSG_MATCH_READ:
				if (!msg.obj.toString().equals(new String()))
					DisplayContenedorName(msg.obj.toString()); // Refresh the list
				break;
			default:
				super.msgProcess(msg);
				break;
		}
	}

	public void DisplayContenedorName(String tid) {
		et_Trackeo_Nro.setText(tid);
		if (!llamo) {
			et_Trackeo_Nro.setText(tid);
			Numero = et_Trackeo_Nro.getText().toString();
			llamo = true;
			GetPiezasATrackear(Numero);
		}
	}

	protected void ShowList() {

		sa = new SimpleAdapter(this, GetData(), R.layout.epclist_item,
				new String[]{getString(R.string.GENERICEPC), getString(R.string.tv_Trackeo_TrackeoCount)}, new int[]{
				R.id.EPCList_TagID, R.id.EPCList_ReadCount});
		listView.setAdapter(sa);
		listView.invalidate();

	}

	protected List<Map<String, String>> GetData() {
		List<Map<String, String>> rt = new ArrayList<Map<String, String>>();
		synchronized (hmList_Lock) {
			Iterator iter = hmList.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				EPCModel val = (EPCModel) entry.getValue();
				Map<String, String> map = new HashMap<String, String>();
				if (containerGlobal != null)
					if (containerGlobal.getTagsList().contains(val._EPC)) {
						if (!LeidosList.contains(val._EPC)) {
							LeidosList.add(val._EPC);

							Pieza p = new Pieza();
							List<Pieza> lis = containerGlobal.getProductos();
							for (int i = 0; i < lis.size(); i++)
								if (lis.get(i).getCodigoHexa().equals(val._EPC)) {
									p = lis.get(i);
								}
							ProductosLeidosList.add(p);

						}
						Leidos = LeidosList.size();
						tv_Trackeo_Leidos.setText(String.valueOf(Leidos));

						List<Pieza> lp = containerGlobal.getProductos();
						int ind;
						Pieza aux;
						for (int i = 0; i < lp.size(); i++) {
							aux = lp.get(i);
							if (aux.getCodigoHexa().equals(val._EPC))
								map.put(getString(R.string.GENERICEPC), aux.getTipoProducto() + aux.getnSerie() + getString(R.string.GENERICGUION) + aux.getDescColor() + getString(R.string.GENERICBARRA) + aux.getDescArticulo());

						}

						int dis = val._RSSI;
						String RSSI = Integer.toString(dis);
						map.put(getString(R.string.tv_Trackeo_TrackeoCount), RSSI);

						rt.add(map);
					}
			}

			return rt;
		}
	}

	public void StopReading() {
		CLReader.Stop();
		isStartPingPong = false;
		isKeyDown = false;
		if (Asignados != 0)
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					btn_Trackeo.setText(getString(R.string.btn_Trackeo_Trackear));
					btn_Trackeo.setClickable(true);
				}
			});
	}

	public void ShowPopupErrorTrackeo(String tag) {
		Intent popup = new Intent(TrackeoActivity.this, PopUpActivity.class);
		popup.putExtra(getString(R.string.tv_PopUpError_pieza), tag);
		startActivity(popup);
	}

	public void MostrarPopUpOperar() {
		showConfim("Desea " + btn_Trackeo_Operacion.getText().toString() + " las " + Leidos + " piezas al contenedor " + et_Trackeo_Nro.getText().toString() + getString(R.string.str_signoPregunta), getString(R.string.GENERICSI), getString(R.string.GENERICNO)
				, new DialogInterface.OnClickListener() {
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

	public void ExecuteOperacion() {

		List<HashMap<String,String>> piezas = new ArrayList<>();
		String cont = containerGlobal.getId();
		String operario = _Operario.getLogeo();

		for (Pieza p : ProductosLeidosList) {

			HashMap x=new HashMap();
			x.put(getString(R.string.API_Trackeo_Req_TrackearCodigo),p.getTipoProducto()+p.getnSerie());
			x.put(getString(R.string.API_Trackeo_Req_TrackearCodigoHexa),p.getCodigoHexa());

			piezas.add(x);

		}

		TrackearApi(cont, _Operacion, piezas,operario);

	}

	public void MostrarPopUpTrackeo(List<TrackeoPieza> e, String asignadas, String cargadas, String verificadas){

		String vista = new String();
		for(TrackeoPieza row : e) {

			boolean error = (row.isError());
			String em = row.getErrorMensaje();
			String ns = row.getnSerie();
			String tp = row.getTipoProducto();
			String nvista;

			if(!error)
				nvista = vista + tp + ns + "   " + getString(R.string.GENERICPUNTOS) + "   " + getString(R.string.GENERICCORRECTO) + getString(R.string.str_br);
			else
				nvista = vista + tp + ns + "   " + getString(R.string.GENERICPUNTOS) + "   " + em + getString(R.string.str_br);


			vista = nvista;
		};
		vista = vista + getString(R.string.str_br) + getString(R.string.str_br) + getString(R.string.str_br);
		vista = vista + getString(R.string.PopUpTrackeo_Asignados) + asignadas + getString(R.string.str_br);
		vista = vista + getString(R.string.PopUpTrackeo_Cargados) + cargadas + getString(R.string.str_br);
		vista = vista + getString(R.string.PopUpTrackeo_Verificados) + verificadas + getString(R.string.str_br);
		hideWait();
		showMsg(vista ,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Back();
			}
		});

	}

	public void Clear(View v) {
		hmList.clear();
	}

	@Override
	public void onBackPressed() {
		Back();
	}

	public void Back() {

		if (btn_Trackeo.getText().toString()
				.equals(getString(R.string.btn_read_stop))) {
			showMsg(getString(R.string.uhf_please_stop), null);
			return;
		}

		DisposeAll();

		finish();

		TrackeoActivity.this.finish();
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



