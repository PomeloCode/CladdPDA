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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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


public class VerificacionActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	/**
	 * INICIO Definicion de elementos de la vista
	 **/

	private EditText et_Verificacion_Nro = null;
	private View v_editContainer = null;
	private ListView listView = null;

	private TextView tv_Verificacion_Leidos = null;
	private TextView tv_Verificacion_Asignados = null;
	private TextView tv_Verificacion_Cargados = null;
	private TextView tv_Verificacion_Verificadas = null;
	private Button btn_Verificacion_Operacion = null;
	private Button btn_Verificacion = null;

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

	private Contenedor contenedorVerificable = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());
	private Contenedor containerGlobalVerificadas = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());
	private Contenedor containerGlobalNoTrackeadas = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());

	/* MESSAGGE Handling */

	private final int MSG_RESULT_Verificacion = MSG_USER_BEG + 1;
	private final int MSG_MATCH_READ = MSG_USER_BEG + 2;

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

	public void SetAntennaConfigurations(String funcion) {

		ConfigureRFIDModule(getString(R.string.Verificacion));

	}

	protected void InitView() {

		this.setContentView(R.layout.verificacion);

		BindViews();
	}

	protected void BindViews() {

		BindToolBar();
		v_editContainer = findViewById(R.id.v_editContainer);
		et_Verificacion_Nro = findViewById(R.id.et_Verificacion_Nro);
		listView = findViewById(R.id.lv_Main);
		tv_Verificacion_Leidos = findViewById(R.id.tv_Verificacion_Leidos);
		tv_Verificacion_Asignados = findViewById(R.id.tv_Verificacion_Asignados);
		tv_Verificacion_Cargados = findViewById(R.id.tv_Verificacion_Cargados);
		tv_Verificacion_Verificadas = findViewById(R.id.tv_Verificacion_Verificados);
		btn_Verificacion = findViewById(R.id.btn_Verificacion);
		btn_Verificacion_Operacion = findViewById(R.id.btn_Verificacion_Operacion);

		InitListeners();

		DisplayData();

		InitListViewUpdateThread();
	}

	protected void DisplayData() {

		SetToolBar(
				getString(R.string.tv_Verificacion_Title),
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

		et_Verificacion_Nro.addTextChangedListener(new TextWatcher() {

			@RequiresApi(api = Build.VERSION_CODES.O)
			@Override
			public void afterTextChanged(Editable s) {
				if (et_Verificacion_Nro.getText().length() == 24) {
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
					ConfigureRFIDModule(getString(R.string.LeerTagContenedor));
					RestartVerificacion();
				} else {
					View currentFocus = getCurrentFocus();
					if (currentFocus != null) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
					}
					sendMessage(MSG_MATCH_READ, et_Verificacion_Nro.getText().toString());
				}
			}
		});

		btn_Verificacion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				VirtualBtnKeyDown(view);

			}
		});

		btn_Verificacion_Operacion.setOnClickListener(new View.OnClickListener() {
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
						sendMessage(MSG_RESULT_Verificacion, null);
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

		Button btnVerificacion = (Button) v;

		String controlText = btnVerificacion.getText().toString();

		if (controlText.equals(getString(R.string.btn_Verificacion_Verificar))) {
			StartReading();
		} else
			StopReading();

	}

	public void StartReading() {

		if (tv_Verificacion_Asignados.getText().toString().equals(getString(R.string.tv_Verificacion_Start))) {

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
					btn_Verificacion.setText(getText(R.string.btn_read_stop));
					CLReader.Stop(); // stop
					isStartPingPong = true;
					GetEPC_6C();
				}
			}
		}
	}

	public void RestartVerificacion() {

		et_Verificacion_Nro.setFocusable(true);
		et_Verificacion_Nro.setFocusableInTouchMode(true);
		et_Verificacion_Nro.setClickable(true);
		et_Verificacion_Nro.setTextColor(Color.BLACK);
		et_Verificacion_Nro.setText(new String());
		hmList.clear();
		Asignados = 0;
		Leidos = 0;
		Verificados = 0;
		Cargados = 0;
		et_Verificacion_Nro.setText(new String());
		tv_Verificacion_Verificadas.setText(String.valueOf(Verificados));
		tv_Verificacion_Cargados.setText(String.valueOf(Cargados));

		tv_Verificacion_Leidos.setText(String.valueOf(Asignados));
		tv_Verificacion_Asignados.setText(String.valueOf(Leidos));

		v_editContainer.setBackground(getDrawable(R.drawable.lupa));

		btn_Verificacion.setText(getString(R.string.btn_Verificacion_LeerContenedor));

		contenedorVerificable = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());

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
		} else {
			if (contenedorVerificable.getTagsList().contains(model._EPC)) {
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
				if (containerGlobalVerificadas.getTagsList().contains(model._EPC)) {
					// pieza que fue cargada previamente

				} else if (containerGlobalNoTrackeadas.getTagsList().contains(model._EPC)){
					// pieza que no fue trackeada
					ShowPopupErrorNoTrackeada(model._EPC);
				}
				else {
					// pieza que no esta asignada
					if (!model._EPC.substring(0, 4).equals(getString(R.string.RFID_VirginStartTag)))
						if (model._EPC.substring(2, 4).equals(getString(R.string.StockITBusinessID)))
							ShowPopupErrorNoAsignada(model._EPC);
				}
			}
		}
	}

	/**
	 * API
	 *
	 * @return
	 */

	protected void GetPiezasAVerificar(String num) {

		showWait(getString(R.string.waiting));

		List<String> reqKeys = GetReqKeysList();
		List<Object> reqValues = GetReqValuesList(num, 1);

		Call<JsonElement> call = GetApiService(BaseUrlApi).getContainer(CreateReqBodyAndLogToServer(reqKeys, reqValues, getString(R.string.API_Verificacion_Nombre)));

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				llamo = false;
				try {

					if (response.isSuccessful()) {

						JSONObject resp = new JSONObject(response.body().toString());

						LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_Verificacion_VerificarNombre), 2, Calendar.getInstance().toString(), resp.toString());

						if (IsAPIResponseStateOK(resp, getString(R.string.API_Verificacion_Nombre))) {

							JSONObject dataSource = new JSONObject(resp.getString(getString(R.string.API_DATASOURCE)));

							List<Pieza> piezas = new Gson().fromJson(dataSource.getJSONArray(getString(R.string.API_Verificacion_Res_Piezas)).toString(), new TypeToken<List<Pieza>>() {
							}.getType());


							if (piezas.size() > 0) {

								Numero = (dataSource.getString(getString(R.string.API_Verificacion_Res_ContenedorID)));
								Asignados = Integer.parseInt(dataSource.getString(getString(R.string.API_Verificacion_Res_Asignados)));
								Cargados = Integer.parseInt(dataSource.getString(getString(R.string.API_Verificacion_Res_Cargados)));
								Verificados = Integer.parseInt(dataSource.getString(getString(R.string.API_Verificacion_Res_Verificados)));

								List<String> hexListNoTrackeadas = new ArrayList<>(); // Las No Trackeadas
								List<String> hexListCargados = new ArrayList<>(); // Las que puede verificar
								List<String> hexListVerificadas = new ArrayList<>(); //Las Ya Verificadas

								for (int i = 0; i < piezas.size(); i++) {

									boolean carg = Boolean.parseBoolean(piezas.get(i).getPiezaCargada());
									boolean veri = Boolean.parseBoolean(piezas.get(i).getPiezaVerificada());

									if (!carg && !veri)
										hexListNoTrackeadas.add(piezas.get(i).getCodigoHexa());
									else if (carg && !veri)
										hexListCargados.add(piezas.get(i).getCodigoHexa());
									else
										hexListVerificadas.add(piezas.get(i).getCodigoHexa());
								}

								FillContainerGlobal(Numero, piezas, hexListNoTrackeadas, hexListCargados,hexListVerificadas);

								SetAntennaConfigurations(getString(R.string.tv_Verificacion_Title));

								MostrarDatosDeLaAPI();

								btn_Verificacion.setText(getString(R.string.btn_Verificacion_Verificar));
								btn_Verificacion.setClickable(true);

							} else {
								showMsg("Error No hay Piezas Trackeadas");
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

	public void FillContainerGlobal(String numero, List<Pieza> piezas, List<String> hexListNoTrackeadas,  List<String> hexListCargados, List<String> hexListVerificadas) {

		contenedorVerificable.setId(Numero);
		contenedorVerificable.setProductos(piezas);
		contenedorVerificable.setTagsList(hexListCargados);
		containerGlobalVerificadas.setTagsList(hexListVerificadas);
		containerGlobalNoTrackeadas.setTagsList(hexListNoTrackeadas);

	}

	public void VerificarApi(String container, int op, List<HashMap<String, String>> piezas, String operario) {
		showWait(getString(R.string.waiting));

		Map<String, Object> jsonPiezas = new LinkedHashMap<>();
		jsonPiezas.put(getString(R.string.API_Verificacion_Req_VerificarPiezas), piezas);

		List<String> reqKeys = GetReqKeysListVerificacion();
		List<Object> reqValues = GetReqValuesListVerificacion(container, 1, jsonPiezas, operario);

		Call<JsonElement> call = GetApiService(BaseUrlApi).Verificar(CreateReqBodyAndLogToServer(reqKeys, reqValues, getString(R.string.API_Verificacion_VerificarNombre)));
		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				llamo = false;
				try {
					JSONObject resp = new JSONObject(response.body().toString());

					LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_Verificacion_VerificarNombre), 2, Calendar.getInstance().toString(), resp.toString());

					if (IsAPIResponseStateOK(resp, getString(R.string.API_Verificacion_Nombre))) {

						JSONObject dataSource = new JSONObject(resp.getString(getString(R.string.API_DATASOURCE)));

						List<TrackeoPieza> aux = new Gson()
								.fromJson(dataSource.getJSONObject(getString(R.string.API_Verificacion_Res_VerificarPiezas)).getJSONArray(getString(R.string.API_Verificacion_Res_VerificarItems)).toString(),
										new TypeToken<List<TrackeoPieza>>() {
										}.getType());

						String asignadas = dataSource.getString(getString(R.string.API_Verificacion_Res_VerificarAsignados));
						String cargadas = dataSource.getString(getString(R.string.API_Verificacion_Res_VerificarCargados));
						String verificadas = dataSource.getString(getString(R.string.API_Verificacion_Res_VerificarVerificados));

						showConfim(getString(R.string.str_success), getString(R.string.PopUpTrackeo_Ver), getString(R.string.str_exit)
								, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										MostrarPopUpVerificacion(aux, asignadas, cargadas, verificadas);
									}
								}, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										VerificacionActivity.this.finish();
									}
								});

					}


				} catch (Exception ex) {
					hideWait();
					showMsg(getString(R.string.error_Verificacion_ApiNotFound));
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				Clear(null);
				hideWait();
				showMsg(t.getMessage(), null);
				LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_Verificacion_Nombre), 4, Calendar.getInstance().toString(), t.getMessage());
			}
		});
	}

	protected List<String> GetReqKeysList() {

		List<String> list = new ArrayList<>();

		list.add(getString(R.string.API_Verificacion_Req_ID));
		list.add(getString(R.string.API_Verificacion_Req_Criterio));

		return list;

	}

	protected List<String> GetReqKeysListVerificacion() {

		List<String> list = new ArrayList<>();

		list.add(getString(R.string.API_Verificacion_Req_VerificarContainerID));
		list.add(getString(R.string.API_Verificacion_Req_VerificarOperacion));
		list.add(getString(R.string.API_Verificacion_Req_VerificarPiezas));
		list.add(getString(R.string.API_Verificacion_Req_VerificarOperario));

		return list;

	}

	protected List<Object> GetReqValuesList(String id, int crit) {

		List<Object> list = new ArrayList<>();

		list.add(id);
		list.add(crit);

		return list;

	}

	protected List<Object> GetReqValuesListVerificacion(String cont, int op, Object jsonPiezas, String operario) {

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
			case MSG_RESULT_Verificacion:
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
		et_Verificacion_Nro.setText(tid);
		if (!llamo) {
			et_Verificacion_Nro.setText(tid);
			Numero = et_Verificacion_Nro.getText().toString();
			llamo = true;
			GetPiezasAVerificar(Numero);
		}
	}

	public void MostrarDatosDeLaAPI() {

		tv_Verificacion_Cargados.setText(String.valueOf(Cargados));
		tv_Verificacion_Verificadas.setText(String.valueOf(Verificados));
		tv_Verificacion_Asignados.setText(String.valueOf(Asignados));

		LockInputContainer();
	}

	public void LockInputContainer() {

		et_Verificacion_Nro.setText(String.valueOf(Numero));
		et_Verificacion_Nro.setFocusable(false);
		et_Verificacion_Nro.setTextColor(Color.GRAY);
		et_Verificacion_Nro.setFocusableInTouchMode(false);
		et_Verificacion_Nro.setClickable(false);

		v_editContainer.setBackground(getDrawable(R.drawable.edit));

	}

	protected void ShowList() {

		sa = new SimpleAdapter(this, GetData(), R.layout.epclist_item,
				new String[]{getString(R.string.GENERICEPC), getString(R.string.tv_Verificacion_TrackeoCount)}, new int[]{
				R.id.EPCList_TagID, R.id.EPCList_ReadCount}) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row = super.getView(position, convertView, parent);
				TextView tv = row.findViewById(R.id.EPCList_ReadCount);

				if (tv.getText().toString().equals("V"))
					row.setBackgroundColor(Color.rgb(0, 255, 0));
				else
					row.setBackgroundColor(Color.rgb(255, 255, 255));

				return row;
			}
		};

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
				if (contenedorVerificable != null)
					if (contenedorVerificable.getTagsList().contains(val._EPC)) {
						if (!LeidosList.contains(val._EPC)) {
							LeidosList.add(val._EPC);

							Pieza p = new Pieza();
							List<Pieza> lis = contenedorVerificable.getProductos();
							for (int i = 0; i < lis.size(); i++)
								if (lis.get(i).getCodigoHexa().equals(val._EPC)) {
									p = lis.get(i);
								}
							ProductosLeidosList.add(p);

						}
						Leidos = LeidosList.size();
						tv_Verificacion_Leidos.setText(String.valueOf(Leidos));

						List<Pieza> lp = contenedorVerificable.getProductos();
						int ind;
						Pieza aux;
						for (int i = 0; i < lp.size(); i++) {
							aux = lp.get(i);
							if (aux.getCodigoHexa().equals(val._EPC))
								map.put(getString(R.string.GENERICEPC), aux.getTipoProducto() + aux.getnSerie() + getString(R.string.GENERICGUION) + aux.getDescColor() + getString(R.string.GENERICBARRA) + aux.getDescArticulo());

						}

						int dis = val._RSSI;
						String RSSI = Integer.toString(dis);
						map.put(getString(R.string.tv_Verificacion_TrackeoCount), RSSI);

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
					btn_Verificacion.setText(getString(R.string.btn_Verificacion_Verificar));
					btn_Verificacion.setClickable(true);
				}
			});
	}

	public void ShowPopupErrorNoAsignada (String tag) {
		Intent popup = new Intent(VerificacionActivity.this, PopUpActivity.class);
		popup.putExtra(getString(R.string.tv_PopUpError_pieza), tag);
		String titulo = getString(R.string.tv_PopUpError_TitleNoAsignada);
		popup.putExtra(getString(R.string.tv_PopUpError_TipoError), getString(R.string.tv_PopUpError_TitleNoAsignada));
		startActivity(popup);
	}

	public void ShowPopupErrorNoTrackeada (String tag) {

		Intent popup = new Intent(VerificacionActivity.this, PopUpActivity.class);
		popup.putExtra(getString(R.string.tv_PopUpError_pieza), tag);
		String titulo = getString(R.string.tv_PopUpError_TitleNoTrackeada);
		popup.putExtra(getString(R.string.tv_PopUpError_TipoError), getString(R.string.tv_PopUpError_TitleNoTrackeada));
		startActivity(popup);

	}

	public void MostrarPopUpOperar() {
		showConfim("Desea " + btn_Verificacion_Operacion.getText().toString() + " las " + Leidos + " piezas al contenedor " + et_Verificacion_Nro.getText().toString() + getString(R.string.str_signoPregunta), getString(R.string.GENERICSI), getString(R.string.GENERICNO)
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
		String cont = contenedorVerificable.getId();
		String operario = _Operario.getLogeo();

		for (Pieza p : ProductosLeidosList) {

			HashMap x = new HashMap();
			x.put(getString(R.string.API_Verificacion_Req_VerificarCodigo), p.getTipoProducto() + p.getnSerie());
			x.put(getString(R.string.API_Verificacion_Req_VerificarCodigoHexa), p.getCodigoHexa());

			piezas.add(x);

		}

		VerificarApi(cont, _Operacion, piezas, operario);

	}

	public void MostrarPopUpVerificacion(List<TrackeoPieza> e, String asignadas, String cargadas, String verificadas) {

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

		if (btn_Verificacion.getText().toString()
				.equals(getString(R.string.btn_read_stop))) {
			showMsg(getString(R.string.uhf_please_stop), null);
			return;
		}

		DisposeAll();

		finish();

		VerificacionActivity.this.finish();
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





