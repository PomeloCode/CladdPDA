package com.cladd.modulos;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.content.DialogInterface;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Estado4Activity extends UHFBaseActivity implements
		IAsynchronousMessage {

	/**
	 * INICIO Definicion de elementos de la vista
	 **/

	private RadioGroup rg_Estado4 = null;
	private RadioButton rb_Estado4_Carga = null;
	private RadioButton rb_Estado4_Descarga = null;
	private RadioButton rb_Estado4_Trasbordo = null;

	private EditText et_Estado4_Nro = null;
	private View v_editContainer = null;
	private ListView listView = null;

	private Button btn_Estado4_Operacion = null;
	private Button btn_Estado4 = null;

	private TextView tv_Estado4_Leidos = null;

	/** FIN Definicion de elementos de la vista **/

	/**
	 * Inicio Definicion Variables Controller
	 **/

	private String Numero = new String();

	private int Leidos = 0;
	private int _Operacion = 5; // 5:CargaEstado4, 4:Trasbordo

	private List<String> LeidosList = new ArrayList<String>();
	private List<Pieza> ProductosLeidosList = new ArrayList<>();
	private List<Map<String, String>> FullList = new ArrayList<>();

	/* Variable Global */


	/* MESSAGGE Handling */

	private final int MSG_RESULT_Estado4 = MSG_USER_BEG + 1;
	private final int MSG_MATCH_READ = MSG_USER_BEG + 2;
	private final int MSG_RESULT_SHOWLIST = MSG_USER_BEG + 3;

	/* Logging */

	private boolean llamo = false;
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
		InitBCModule();
		InitSoundModule();
		InitView();

	}

	@Override
	protected void onResume(){
		super.onResume();

		StopReading();

	}

	protected void InitView() {

		this.setContentView(R.layout.estado4);

		BindViews();
	}

	protected void BindViews() {
		BindToolBar();

		rg_Estado4 = findViewById(R.id.rg_Estado4);
		rb_Estado4_Carga = findViewById(R.id.rb_Estado4_Carga);
		rb_Estado4_Descarga = findViewById(R.id.rb_Estado4_Descarga);
		rb_Estado4_Trasbordo = findViewById(R.id.rb_Estado4_Trasbordo);


		v_editContainer = findViewById(R.id.v_editContainer);

		et_Estado4_Nro = findViewById(R.id.et_Estado4_Nro);

		listView = findViewById(R.id.lv_Main);

		tv_Estado4_Leidos = findViewById(R.id.tv_Estado4_Leidos);

		btn_Estado4 = findViewById(R.id.btn_Estado4);
		btn_Estado4_Operacion = findViewById(R.id.btn_Estado4_Operacion);

		InitListeners();

		DisplayData();

		InitListViewUpdateThread();
	}

	protected void DisplayData() {

		SetToolBar(
				getString(R.string.tv_Estado4_Title),
				_Operario.getDescripcion(),
				getString(R.string.str_back),
				new String(),
				R.drawable.left,
				0,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Back(v);
					}
				},
				null
		);

		v_editContainer.setBackground(getDrawable(R.drawable.lupa));

	}

	protected void InitListeners() {

		et_Estado4_Nro.addTextChangedListener(new TextWatcher() {

			@RequiresApi(api = Build.VERSION_CODES.O)
			@Override
			public void afterTextChanged(Editable s) {
				if (et_Estado4_Nro.getText().length() == 24) {
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
				if (!et_Estado4_Nro.isFocusable()) {

					ConfigureRFIDModule(getString(R.string.LeerTagContenedor));

					RestartEstado4();
				} else {
					View currentFocus = getCurrentFocus();
					if (currentFocus != null) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
					}
					sendMessage(MSG_MATCH_READ, et_Estado4_Nro.getText().toString());
				}
			}
		});

		rg_Estado4.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// checkedId is the RadioButton selected
				RadioButton rb = findViewById(checkedId);

				ConfigureRFIDModule(getString(R.string.LeerTagContenedor));

				RestartEstado4();

				btn_Estado4_Operacion.setText(rb.getText().toString());

				if (rb.getText().toString().equals(getString(R.string.rb_Estado4_Carga)))
					_Operacion = 5;
				if (rb.getText().toString().equals(getString(R.string.rb_Estado4_Descarga)))
					_Operacion = 0; //1; Etapa 2
				if (rb.getText().toString().equals(getString(R.string.rb_Estado4_Trasbordo)))
					_Operacion = 4; //2; Etapa 2

			}
		});

		btn_Estado4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				VirtualBtnKeyDown(view);

			}
		});

		btn_Estado4_Operacion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// preventing double, using threshold of 1000 ms
				if (SystemClock.elapsedRealtime() - timerBtnOperacion < 1000) {
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
						sendMessage(MSG_RESULT_Estado4, null);
						Thread.sleep(20); // Refresh every second
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	public void VirtualBtnKeyDown(View v) {
		if (SystemClock.elapsedRealtime() - timerBtnLeer < 1000) {
			return;
		}

		timerBtnLeer = SystemClock.elapsedRealtime();

		Button btnEstado4 = (Button) v;

		String controlText = btnEstado4.getText().toString();

		if (controlText.equals(getString(R.string.btn_Estado4_Trackear))) {
			StartReading();
		} else
			StopReading();

	}

	public void StartReading() {

		if (et_Estado4_Nro.getText().toString().equals(new String())) {

			in_reading = true;
			int ret = -1;
			ret = UHFReader._Tag6C.GetEPC(_NowAntennaNo, _RFIDSingleRead);

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
					btn_Estado4.setText(getText(R.string.btn_read_stop));
					CLReader.Stop(); // stop
					isStartPingPong = true;
					GetEPC_6C();
				}
			}
		}
	}

	public void RestartEstado4() {

		et_Estado4_Nro.setFocusable(true);
		et_Estado4_Nro.setFocusableInTouchMode(true);
		et_Estado4_Nro.setClickable(true);
		et_Estado4_Nro.setTextColor(Color.BLACK);
		et_Estado4_Nro.setText(new String());
		hmList.clear();
		FullList.clear();
		LeidosList.clear();
		ProductosLeidosList.clear();
		Leidos = 0;
		et_Estado4_Nro.setText(new String());
		tv_Estado4_Leidos.setText(String.valueOf(Leidos));
		v_editContainer.setBackground(getDrawable(R.drawable.lupa));
		btn_Estado4.setText(getString(R.string.btn_Estado4_LeerContenedor));

	}

	private int GetEPC_6C() {

		int ret = -1;
		in_reading = true;

		ret = UHFReader._Tag6C.GetEPC(_NowAntennaNo, _RFIDInventoryRead);

		return ret;

	}

	@Override
	public void OutPutEPC(EPCModel model) {
		if (et_Estado4_Nro.isFocusable()) {
			hmList.clear();
			sendMessage(MSG_MATCH_READ, model._EPC);
			try {
				synchronized (beep_Lock) {
					beep_Lock.notify();
				}
			} catch (Exception ex) {
			}
		} else {
			if (!LeidosList.contains(model._EPC))
				if (!model._EPC.substring(0, 4).equals(getString(R.string.RFID_VirginStartTag)) && model._EPC.substring(2, 4).equals(getString(R.string.StockITBusinessID))) {
					try {
						synchronized (hmList_Lock) {
							if (hmList.containsKey(model._EPC + model._TID)) {
								hmList.get(model._EPC + model._TID);
								hmList.remove(model._EPC + model._TID);
								hmList.put(model._EPC + model._TID, model);
							} else {
								hmList.put(model._EPC + model._TID, model);
							}
							sendMessage(MSG_RESULT_SHOWLIST, model);
						}
						synchronized (beep_Lock) {
							beep_Lock.notify();
						}
					} catch (Exception ex) {
					}
				}

		}
	}

	/**
	 * API
	 *
	 * @return
	 */

	public void GrabarEstado4(String container, int op, List<HashMap<String, String>> piezas, String operario) {
		showWait(getString(R.string.waiting));

		Map<String, Object> jsonPiezas = new LinkedHashMap<>();
		jsonPiezas.put(getString(R.string.API_Estado4_Req_GrabarEstado4Piezas), piezas);

		List<String> reqKeys = GetReqKeysListEstado4();
		List<Object> reqValues = GetReqValuesListEstado4(container, op, jsonPiezas, operario);

		Call<JsonElement> call = GetApiService(BaseUrlApi).Trackear(CreateReqBodyAndLogToServer(reqKeys, reqValues, getString(R.string.API_Estado4_GrabarEstado4Nombre)));

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				llamo = false;
				try {
					JSONObject resp = new JSONObject(response.body().toString());

					LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_Estado4_GrabarEstado4Nombre), 2, Calendar.getInstance().toString(), resp.toString());

					if (IsAPIResponseStateOK(resp, getString(R.string.API_Estado4_GrabarEstado4Nombre))) {

						JSONObject dataSource = new JSONObject(resp.getString(getString(R.string.API_DATASOURCE)));

						List<TrackeoPieza> aux = new Gson()
								.fromJson(dataSource.getJSONObject(getString(R.string.API_Estado4_Res_GrabarEstado4Piezas)).getJSONArray(getString(R.string.API_Estado4_Res_GrabarEstado4Items)).toString(),
										new TypeToken<List<TrackeoPieza>>() {
										}.getType());

						String asignadas = dataSource.getString(getString(R.string.API_Estado4_Res_GrabarEstado4Asignados));
						String cargadas = dataSource.getString(getString(R.string.API_Estado4_Res_GrabarEstado4Cargados));
						String verificadas = dataSource.getString(getString(R.string.API_Estado4_Res_GrabarEstado4Verificados));

						showConfim(getString(R.string.str_success), getString(R.string.PopUpTrackeo_Ver), getString(R.string.str_exit)
								, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										MostrarPopUpEstado4(aux, asignadas, cargadas, verificadas);
									}
								}, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Estado4Activity.this.finish();
									}
								});

					}

				} catch (Exception ex) {

					showMsg(getString(R.string.error_Estado4_ApiNotFound));
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

	protected List<String> GetReqKeysListEstado4() {

		List<String> list = new ArrayList<>();

		list.add(getString(R.string.API_Estado4_Req_GrabarEstado4ContainerID));
		list.add(getString(R.string.API_Estado4_Req_GrabarEstado4Operacion));
		list.add(getString(R.string.API_Estado4_Req_GrabarEstado4Piezas));
		list.add(getString(R.string.API_Estado4_Req_GrabarEstado4Operario));

		return list;

	}

	protected List<Object> GetReqValuesListEstado4(String cont, int op, Object jsonPiezas, String operario) {

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
			case MSG_RESULT_Estado4:
				ShowList(FullList);
				break;
			case MSG_RESULT_SHOWLIST:
				if (msg.obj != null)
					GetData((EPCModel) msg.obj); // Refresh the list
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
		et_Estado4_Nro.setText(tid);
		if (!llamo) {
			llamo = true;
			Numero = et_Estado4_Nro.getText().toString();
			LockInputContainer();

			ConfigureRFIDModule(getString(R.string.Estado4));
		}
	}

	public void LockInputContainer() {

		et_Estado4_Nro.setText(String.valueOf(Numero));
		et_Estado4_Nro.setFocusable(false);
		et_Estado4_Nro.setTextColor(Color.GRAY);
		et_Estado4_Nro.setFocusableInTouchMode(false);
		et_Estado4_Nro.setClickable(false);

		v_editContainer.setBackground(getDrawable(R.drawable.edit));

	}

	protected void ShowList(List<Map<String, String>> data) {

		sa = new SimpleAdapter(this, data, R.layout.epclist_item,
				new String[]{getString(R.string.GENERICEPC), getString(R.string.tv_Estado4_TrackeoCount)}, new int[]{
				R.id.EPCList_TagID, R.id.EPCList_ReadCount});
		listView.setAdapter(sa);
		listView.invalidate();
		tv_Estado4_Leidos.setText(String.valueOf(Leidos));


	}

	protected void GetData(EPCModel val) {
		if (!LeidosList.contains(val._EPC)) {
			Map<String, String> map = new HashMap<String, String>();
			LeidosList.add(val._EPC);

			Pieza p = new Pieza();

			p.setCodigoHexa(val._EPC);

			String fullpieza = GetTipoProductoNserie(val._EPC);
			p.setTipoProducto(fullpieza.substring(0, 2));
			p.setnSerie(fullpieza.substring(2));

			ProductosLeidosList.add(p);

			map.put(getString(R.string.GENERICEPC), p.getTipoProducto() + p.getnSerie());


			int dis = val._RSSI;
			String RSSI = Integer.toString(dis);
			map.put(getString(R.string.tv_Estado4_TrackeoCount), RSSI);
			FullList.add(map);
			Leidos = LeidosList.size();
		}
	}

	public void StopReading() {
		CLReader.Stop();
		isStartPingPong = false;
		isKeyDown = false;
		if (!et_Estado4_Nro.getText().toString().equals(new String()))
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					btn_Estado4.setText(getString(R.string.btn_Estado4_Trackear));
					btn_Estado4.setClickable(true);
				}
			});
	}

	public void MostrarPopUpOperar() {
		showConfim("Desea " + btn_Estado4_Operacion.getText().toString() + " las " + Leidos + " piezas al contenedor " + et_Estado4_Nro.getText().toString() + getString(R.string.str_signoPregunta), getString(R.string.GENERICSI), getString(R.string.GENERICNO)
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

		List<HashMap<String, String>> piezas = new ArrayList<>();
		String operario = _Operario.getLogeo();

		for (Pieza p : ProductosLeidosList) {

			HashMap x = new HashMap();
			x.put(getString(R.string.API_Estado4_Req_GrabarEstado4Codigo), p.getTipoProducto() + p.getnSerie());
			x.put(getString(R.string.API_Estado4_Req_GrabarEstado4CodigoHexa), p.getCodigoHexa());

			piezas.add(x);

		}

		GrabarEstado4(Numero, _Operacion, piezas, operario);

	}

	public void MostrarPopUpEstado4(List<TrackeoPieza> e, String asignadas, String cargadas, String verificadas) {

		String vista = new String();
		for (TrackeoPieza row : e) {

			boolean error = (row.isError());
			String em = row.getErrorMensaje();
			String ns = row.getnSerie();
			String tp = row.getTipoProducto();
			String nvista;

			if (!error)
				nvista = vista + tp + ns + "   " + getString(R.string.GENERICPUNTOS) + "   " + getString(R.string.GENERICCORRECTO) + getString(R.string.str_br);
			else
				nvista = vista + tp + ns + "   " + getString(R.string.GENERICPUNTOS) + "   " + em + getString(R.string.str_br);


			vista = nvista;
		}
		;
		vista = vista + getString(R.string.str_br) + getString(R.string.str_br) + getString(R.string.str_br);
		vista = vista + getString(R.string.PopUpTrackeo_Asignados) + asignadas + getString(R.string.str_br);
		vista = vista + getString(R.string.PopUpTrackeo_Cargados) + cargadas + getString(R.string.str_br);
		vista = vista + getString(R.string.PopUpTrackeo_Verificados) + verificadas + getString(R.string.str_br);
		hideWait();
		showMsg(vista, new DialogInterface.OnClickListener() {
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

		if (btn_Estado4.getText().toString()
				.equals(getString(R.string.btn_read_stop))) {
			showMsg(getString(R.string.uhf_please_stop), null);
			return;
		}

		DisposeAll();

		finish();

		Estado4Activity.this.finish();
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


