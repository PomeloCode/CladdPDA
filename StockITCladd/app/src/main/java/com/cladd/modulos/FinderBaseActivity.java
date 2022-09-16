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
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;



import com.cladd.entities.api.Contenedor;
import com.cladd.entities.api.Finder;
import com.cladd.entities.api.Operario;
import com.cladd.entities.model.PDASettings;
import com.cladd.entities.model.Pieza;
import com.cladd.services.DataBaseHelper;
import com.cladd.uhf.UHFBaseActivity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FinderBaseActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	/** INICIO Definicion de elementos de la vista **/

	private TextView tv_UsuarioLogueado = null;
	private TextView tv_FinderBase_NombreBuscador = null;
	private RadioGroup rg_FinderBaseCriterio = null;
	private RadioButton rb_FinderBase_Partida = null;
	private RadioButton rb_FinderBase_Container = null;
	private EditText et_FinderBase_Nro = null;
	private View v_FinderBase_BuscadorBtn = null;
	private ListView listView = null; // Data list object
	private TextView tv_FinderBase_Asignados = null;
	private Button btn_FinderBase = null;

	/** FIN Definicion de elementos de la vista **/

	/** Inicio Definicion Variables Controller **/

	/* Variable Global */

	private Finder containerGlobal = new Finder(new String(), new String(), new ArrayList<>(), new ArrayList<>());

	/* MESSAGGE Handling */

	private final int MSG_MATCH_READ = MSG_USER_BEG + 2;

	/* Logging */

	private static int _FinderType = 0;
	private int cambioPotAntena = 0;
	private boolean llamo = false;
	private int asignados = 0;

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
		InitRFIDModule(this,getString(R.string.LeerTagContenedor));
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
				ConfigureRFIDModule(getString(R.string.LeerTagContenedor));
			}
		});
	}

	protected void InitView() {

		this.setContentView(R.layout.finder_base);


		showCustomBar(getString(R.string.tv_FinderBase_Title),
				getString(R.string.str_back), null,
				R.drawable.left, 0,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Back(v);
					}
				},
				null
		);

		BindViews();
	}

	protected void BindViews() {

		tv_UsuarioLogueado = findViewById(R.id.tv_UsuarioLogueado);
		tv_FinderBase_NombreBuscador = findViewById(R.id.tv_FinderBase_NombreBuscador);
		rg_FinderBaseCriterio = findViewById(R.id.rg_FinderBaseCriterio);
		rb_FinderBase_Partida = findViewById(R.id.rb_FinderBase_Partida);
		rb_FinderBase_Container = findViewById(R.id.rb_FinderBase_Container);
		v_FinderBase_BuscadorBtn = findViewById(R.id.v_FinderBase_BuscadorBtn);
		et_FinderBase_Nro = findViewById(R.id.et_FinderBase_Nro);
		listView = findViewById(R.id.lv_Main);
		tv_FinderBase_Asignados = findViewById(R.id.tv_FinderBase_Asignados);
		btn_FinderBase = findViewById(R.id.btn_FinderBase);

		InitListeners();

		DisplayData();
	}

	protected void DisplayData() {

		tv_UsuarioLogueado.setText(_Operario.getDescripcion());

		v_FinderBase_BuscadorBtn.setBackground(getDrawable(R.drawable.lupa));

	}

	protected void InitListeners(){

		et_FinderBase_Nro.addTextChangedListener(new TextWatcher() {

			@RequiresApi(api = Build.VERSION_CODES.O)
			@Override
			public void afterTextChanged(Editable s) {
				llamo = false;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
										  int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
									  int before, int count) {
				v_FinderBase_BuscadorBtn.setBackground(getDrawable(R.drawable.lupa));

			}
		});

		v_FinderBase_BuscadorBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (asignados != 0) {
					cambioPotAntena = UHFReader._Config.SetANTPowerParam(1, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_AntPowerFinderRead))));
					RestartViewBuscadorFinder();
				} else {
					View currentFocus = getCurrentFocus();
					if (currentFocus != null) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
					}
					sendMessage(MSG_MATCH_READ, et_FinderBase_Nro.getText().toString());
				}
			}
		});

		rg_FinderBaseCriterio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RestartViewBuscadorFinder();
				cambioPotAntena = UHFReader._Config.SetANTPowerParam(1, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_AntPowerFinderRead))));
				RadioButton rb = findViewById(checkedId);

				if (rb.getText().toString().equals("Partida")) {// Partida
					_FinderType = 0;
					tv_FinderBase_NombreBuscador.setText(getString(R.string.tv_FinderBase_BuscarPartida));
				} else if (rb.getText().toString().equals("Contenedor")) {// Contenedor
					_FinderType = 1;
					tv_FinderBase_NombreBuscador.setText(getString(R.string.tv_FinderBase_BuscarContenedor));
				}


			}
		});

		btn_FinderBase.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				VirtualBtnKeyDown(view);

			}
		});

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				Contenedor aux = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());
				for(int i = 0 ; i < containerGlobal.getProductos().size(); i++ ){
					String sector = containerGlobal.getProductos().get(i).get(0).getSector();
					String selected = ((TextView)((LinearLayout) v).getChildAt(0)).getText().toString();
					if(!selected.equals("Todos")) {
						if (sector.equals(selected)) {
							aux.setProductos(containerGlobal.getProductos().get(i));
						}
					}else{
						aux.setTodos(containerGlobal.getProductos());
					}
				}
				Intent i = new Intent(FinderBaseActivity.this, FinderActivity.class);
				Gson gson = new Gson();
				String json = gson.toJson(aux);
				i.putExtra("productos", json);
				startActivity(i);
			};
		});

	}

	public void VirtualBtnKeyDown (View v) {

		Button btnVerDetalle = (Button) v;

		String controlText = btnVerDetalle.getText().toString();

		if (controlText.equals(getString(R.string.btn_Finder_Leer))) {
			StartReading();

			new Thread() {
				@Override
				public void run() {
					new Timer().schedule(
							new TimerTask() {
								@Override
								public void run() {
									StopReading();
								}
							},
							2000
					);
				}
			}.start();
		}
		else
			StopReading();

	}

	public void StartReading() {

		if (tv_FinderBase_Asignados.getText().toString().equals(getString(R.string.tv_FinderBase_Start))) {

			in_reading = true;
			int ret = -1;
			if (cambioPotAntena == 0) {
				ret = UHFReader._Tag6C.GetEPC_TID(_NowAntennaNo, _RFIDSingleRead);
			}
		}
	}

	protected void RestartViewBuscadorFinder() {

		if (containerGlobal != null)
			containerGlobal = new Finder(new String(), new String(), new ArrayList<>(), new ArrayList<>());

		et_FinderBase_Nro.setFocusable(true);
		et_FinderBase_Nro.setFocusableInTouchMode(true);
		et_FinderBase_Nro.setClickable(true);
		et_FinderBase_Nro.setTextColor(Color.BLACK);
		et_FinderBase_Nro.setText(new String());
		hmList.clear();
		asignados = 0;
		et_FinderBase_Nro.setText(new String());
		tv_FinderBase_Asignados.setText(String.valueOf(asignados));
		v_FinderBase_BuscadorBtn.setBackground(getDrawable(R.drawable.lupa));
		btn_FinderBase.setText(getString(R.string.btn_FinderBase_Leer));
		listView.setAdapter(null);


	}

	private int GetEPC_6C() {

		int ret = -1;
		in_reading = true;

		ret = UHFReader._Tag6C.GetEPC_TID(_NowAntennaNo, _RFIDSingleRead);

		return ret;

	}

	@Override
	public void OutPutEPC(EPCModel model) {

		if (et_FinderBase_Nro.getText().toString().equals(new String())) {
			if (in_reading) {
				sendMessage(MSG_MATCH_READ, model._EPC);
				in_reading = false;
			}
			if (!isStartPingPong)
				return;
			try {
				synchronized (hmList_Lock) {
					if (hmList.isEmpty()) {

						RestartViewBuscadorFinder();
						hmList.put(model._EPC + model._TID, model);

					} else
						return;
				}
				synchronized (beep_Lock) {
					beep_Lock.notify();
				}
			} catch (Exception ex) {
				showMsg("Error inesperado: \n" + ex.getMessage());
			}
		}
	}

	/**
	 * API
	 * @return
	 */

	protected void GetPiezasToFind(int crit, String num) {

		showWait(getString(R.string.waiting));

		List<String> reqKeys = GetReqKeysList();
		List<Object> reqValues = GetReqValuesList(num, crit);

		Call<JsonElement> call = GetApiService(BaseUrlApi).getProductosByCriterio(CreateReqBodyAndLogToServer(reqKeys,reqValues,getString(R.string.API_FinderBase_Nombre)));

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				llamo = false;

				try {
					if (response.isSuccessful()) {

						JSONObject resp = new JSONObject(response.body().toString());

						LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_FinderBase_Nombre), 2, Calendar.getInstance().toString(), resp.toString());

						if (IsAPIResponseStateOK(resp, getString(R.string.API_FinderBase_Nombre))) {

							ArrayList<JsonObject> depositos = new Gson().fromJson(resp.getJSONArray(getString(R.string.API_DATASOURCE))
									.toString(), new TypeToken<ArrayList<JsonObject>>() {}.getType());
							List<List<Pieza>> listaDepositos = new ArrayList<>();
							for (int i = 0 ; i < depositos.size() ; i++ )
							{
								JSONObject jsonObject = new JSONObject(String.valueOf(depositos.get(i)));
								List<Pieza> PiezaList = new Gson().fromJson(jsonObject.getJSONArray(getString(R.string.API_FinderBase_Res_Deposito)).toString(), new TypeToken<List<Pieza>>() {}.getType());
								listaDepositos.add(PiezaList);
								asignados = asignados + PiezaList.size();
							}
							containerGlobal.setProductos(listaDepositos);
							cambioPotAntena = UHFReader._Config.SetANTPowerParam(1, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_AntPowerFinderSearch))));
							Thread.sleep(20);

							tv_FinderBase_Asignados.setText(String.valueOf(asignados));

							et_FinderBase_Nro.setFocusable(false);
							et_FinderBase_Nro.setTextColor(Color.GRAY);
							et_FinderBase_Nro.setFocusableInTouchMode(false);
							et_FinderBase_Nro.setClickable(false);

							v_FinderBase_BuscadorBtn.setBackground(getDrawable(R.drawable.edit));

							btn_FinderBase.setClickable(true);

							DisplayListaDepositos();

						}

					} else {
						hideWait();
						showMsg(getString(R.string.error_FinderBase_ApiDevolvioRaro));
					}
				}


				catch (Exception ex) {
					hideWait();
					showMsg("Error :  " + getString(R.string.str_br) + ex.getMessage().toString(), null);
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
				LogToExternalServer(getString(R.string.str_PDA),UNIQUEIDPDA,getString(R.string.API_FinderBase_Nombre),4, Calendar.getInstance().toString(),t.getMessage());
			}
		});


	}

	protected List<String> GetReqKeysList(){

		List<String> list= new ArrayList<>();

		list.add(getString(R.string.API_FinderBase_Req_ID));
		list.add(getString(R.string.API_FinderBase_Req_Criterio));

		return list;

	}

	protected List<Object> GetReqValuesList(String id, int crit){

		List<Object> list= new ArrayList<>();

		list.add(id);
		list.add(crit);

		return list;

	}

	/**
	 * End API
	 */

	@Override
	protected void msgProcess(Message msg) {
		switch (msg.what) {
			case MSG_MATCH_READ:
				if (!msg.obj.toString().equals(new String())) {
					DisplayContenedorName(msg.obj.toString()); // Refresh the list
				}
				break;
			default:
				super.msgProcess(msg);
				break;
		}
	}

	public void DisplayContenedorName(String tid) {
		et_FinderBase_Nro.setText(tid);
		if (!llamo) {
			llamo = true;
			GetPiezasToFind(_FinderType, tid);
		}

	}

	protected void DisplayListaDepositos() {
		sa = new SimpleAdapter(this, GetDepositos(), R.layout.finder_base_item,
				new String[]{"Deposito", "CantidadPiezas"}, new int[]{
				R.id.tv_FinderBase_ItemDeposito, R.id.tv_FinderBase_ItemCantidadPiezas}) {
		};

		listView.setAdapter(sa);

		listView.invalidate();
	}

	protected List<Map<String, String>> GetDepositos() {

		List<Map<String, String>> rt = new ArrayList<Map<String, String>>();

		List<List<Pieza>> lp = containerGlobal.getProductos();
		for (int i=0;i<lp.size();i++){
			Map<String, String> map = new HashMap<String, String>();
			map.put("Deposito", lp.get(i).get(0).getSector());
			map.put("CantidadPiezas", String.valueOf(lp.get(i).size()));
			rt.add(map);
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("Deposito", "Todos");
		map.put("CantidadPiezas", String.valueOf(asignados));
		rt.add(map);

		return rt;

	}

	public void StopReading() {
		CLReader.Stop();
		isStartPingPong = false;
		isKeyDown = false;
		if (asignados != 0)
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					btn_FinderBase.setText(getString(R.string.btn_read_stop));
					btn_FinderBase.setClickable(true);
				}
			});
	}

	public void Clear(View v) {
		hmList.clear();
	}

	@Override
	public void onBackPressed() {
		Back(null);
	}

	public void Back(View v) {

		if (btn_FinderBase.getText().toString()
				.equals(getString(R.string.btn_read_stop))) {
			showMsg(getString(R.string.uhf_please_stop), null);
			return;
		}

		DisposeAll();

		finish();

		FinderBaseActivity.this.finish();
	}

	@Override
	protected void onDestroy() {
		DisposeAll();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		DisposeAll();
	}

	/**
	 * End Controller
	 */ // Controller

}



