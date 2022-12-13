package com.cladd.modulos;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;



import com.cladd.entities.api.Contenedor;
import com.cladd.entities.api.Operario;
import com.cladd.entities.model.PDASettings;
import com.cladd.entities.model.Pieza;
import com.cladd.services.DataBaseHelper;
import com.cladd.uhf.UHFBaseActivity;
import com.google.gson.Gson;
import com.hopeland.pda.example.R;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.rfid.uhf.UHFReader;
import com.util.Helper.Helper_ThreadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class FinderActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	/**
	 * INICIO Definicion de elementos de la vista
	 **/


	private TextView et_Finder_Nro = null;
	private View v_editContainer = null;
	private ListView listView = null; // Data list object
	private TextView tv_Finder_Encontrados = null;
	private TextView tv_Finder_Asignados = null;
	private Button btn_Finder = null;

	/** FIN Definicion de elementos de la vista **/

	/**
	 * Inicio Definicion Variables Controller
	 **/

	/* Variable Global */

	private List<String> encontradosList = new ArrayList<String>();

	private Contenedor containerGlobal = new Contenedor(new String(), new String(), new ArrayList<>(), new ArrayList<>());

	/* MESSAGGE Handling */

	private final int MSG_RESULT_Finder = MSG_USER_BEG + 1;

	/* Logging */

	private boolean session = false;
	private int asignados = 0;
	private int encontrados = 0;

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
	protected void onResume() {

		super.onResume();

		StopReading();

	}


	protected void InitView() {

		this.setContentView(R.layout.finder);

		BindViews();
	}

	protected void BindViews() {

		BindToolBar();

		v_editContainer = findViewById(R.id.v_editContainer);
		et_Finder_Nro = findViewById(R.id.et_Finder_Nro);
		listView = findViewById(R.id.lv_Main);
		tv_Finder_Encontrados = findViewById(R.id.tv_Finder_Encontrados);
		tv_Finder_Asignados = findViewById(R.id.tv_Finder_Asignados);
		btn_Finder = findViewById(R.id.btn_Finder);
		InitListeners();

		DisplayData();

	}

	protected void DisplayData() {
		SetToolBar(
				getString(R.string.tv_Finder_Title),
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

		String extra = getIntent().getStringExtra("productos");

		Contenedor productos = new Gson().fromJson(extra, Contenedor.class);

		if (productos.getProductos().size() > 0) {
			GetProductsByCriterio(productos.getProductos());
			et_Finder_Nro.setText(productos.getProductos().get(0).getSector());
		} else {
			GetProductsByCriterioTodos(productos.getTodos());
			et_Finder_Nro.setText("Todos");
		}


	}

	protected void InitListeners() {

		et_Finder_Nro.addTextChangedListener(new TextWatcher() {

			@RequiresApi(api = Build.VERSION_CODES.O)
			@Override
			public void afterTextChanged(Editable s) {
				if (et_Finder_Nro.getText().length() >= 6) {
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

			}
		});

		v_editContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (asignados != 0) {
					Back(btn_Finder);
				}
			}
		});

		btn_Finder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				VirtualBtnKeyDown(view);

			}
		});

	}

	public void VirtualBtnKeyDown(View v) {

		Button btnVerDetalle = (Button) v;

		String controlText = btnVerDetalle.getText().toString();

		if (controlText.equals(getString(R.string.btn_Finder_Leer))) {
			StartReading();
		} else
			StopReading();

	}

	public void StartReading() {
		RestartViewPieza();
		if (!isKeyDown) {
			isKeyDown = true; //
			if (!isStartPingPong) {
				btn_Finder.setText(getText(R.string.btn_read_stop));
				CLReader.Stop(); // stop
				isStartPingPong = true;
				GetEPC_6C();
			}
		}
	}

	protected void RestartViewPieza() {

	}

	private int GetEPC_6C() {

		int ret = -1;
		in_reading = true;

		ret = UHFReader._Tag6C.GetEPC(_NowAntennaNo, _RFIDInventoryRead);

		return ret;

	}

	@Override
	public void OutPutEPC(EPCModel model) {
		if (containerGlobal.getTagsList().contains(model._EPC.toString())) {

			if (!session) {
				session = true;
				Helper_ThreadPool.ThreadPool_StartSingle(new Runnable() {
					@Override
					public void run() {
						try {
							CLReader.Stop();
							Thread.sleep(20);
							ConfigureRFIDModule(getString(R.string.FinderModo2));
							GetEPC_6C();

						} catch (Exception ex) {

						}
					}
				});

			}
			if (in_reading) {
				in_reading = false;
			}
			if (!isStartPingPong)
				return;
			try {
				synchronized (hmList_Lock) {
					if (hmList.containsKey(model._EPC + model._TID)) {
						hmList.get(model._EPC + model._TID);
						hmList.remove(model._EPC + model._TID);
						hmList.put(model._EPC + model._TID, model);
					} else {
						hmList.put(model._EPC + model._TID, model);
					}
				}
				synchronized (beep_Lock) {
					beep_Lock.notify();
					sendMessage(MSG_RESULT_Finder, new String());
				}
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Charge List of tags to read
	 */

	// Get List Of Hex to search Phisically
	public Contenedor GetProductsByCriterio(List<Pieza> prods) {
		showWait(getString(R.string.waiting));

		//ConfigureRFIDModule(getString(R.string.FinderModo1));
		containerGlobal.setProductos(prods);
		List<String> hexList = new ArrayList<>();
		for (int i = 0; i < prods.size(); i++)
			hexList.add(prods.get(i).getCodigoHexa());
		containerGlobal.setTagsList(hexList);
		asignados = containerGlobal.getTagsList().size();

		tv_Finder_Asignados.setText(String.valueOf(asignados));
		hideWait();

		return containerGlobal;

	}

	/**
	 * Charge List of tags to read
	 */

	// Get List Of Hex to search Phisically
	public Contenedor GetProductsByCriterioTodos(List<List<Pieza>> prods) {
		showWait(getString(R.string.waiting));

		//ConfigureRFIDModule(getString(R.string.FinderModo1));

		List<String> hexList = new ArrayList<>();
		List<Pieza> produc = new ArrayList<>();
		for (int j = 0; j < prods.size(); j++) {
			for (int i = 0; i < prods.get(j).size(); i++) {
				produc.add(prods.get(j).get(i));
				hexList.add(prods.get(j).get(i).getCodigoHexa());
			}
		}
		containerGlobal.setProductos(produc);
		containerGlobal.setTagsList(hexList);
		asignados = containerGlobal.getTagsList().size();

		tv_Finder_Asignados.setText(String.valueOf(asignados));
		hideWait();

		return containerGlobal;

	}

	/**
	 * End Charge List of tags to read
	 */ // Charge List of tags to read

	@Override
	protected void msgProcess(Message msg) {
		switch (msg.what) {
			case MSG_RESULT_Finder:
				ShowList();
				break;
			default:
				super.msgProcess(msg);
				break;
		}
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
				if (containerGlobal.getTagsList().contains(val._EPC)) {
					if (!encontradosList.contains(val._EPC))
						encontradosList.add(val._EPC);
					encontrados = encontradosList.size();
					tv_Finder_Encontrados.setText(String.valueOf(encontrados));
					List<Pieza> lp = containerGlobal.getProductos();
					int ind;
					Pieza aux;
					for (int i = 0; i < lp.size(); i++) {
						aux = lp.get(i);
						if (aux.getCodigoHexa().equals(val._EPC))
							map.put("EPC", aux.getTipoProducto() + aux.getnSerie() + " - " + aux.getDescColor() + "/" + aux.getDescArticulo());
					}

					int dis = val._RSSI;
					String RSSI = Integer.toString(dis);
					map.put("FinderCount", RSSI);

					rt.add(map);
				}
			}
			Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
				public int compare(Map<String, String> m1, Map<String, String> m2) {
					if (m1.size() > 0 && m2.size() > 0) {
						int m1int = Integer.parseInt(m1.get("FinderCount"));
						int m2int = Integer.parseInt(m2.get("FinderCount"));
						if (m1int < m2int)
							return 1;
						if (m1int == m2int)
							return 0;
						if (m1int > m2int)
							return -1;

					}
					return 0;
				}
			};
			Collections.sort(rt, mapComparator);
			return rt;
		}
	}

	protected void ShowList() {
		sa = new SimpleAdapter(this, GetData(), R.layout.finder_item,
				new String[]{"EPC", "FinderCount"}, new int[]{
				R.id.EPCList_TagID, R.id.EPCList_ReadCount}) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row = super.getView(position, convertView, parent);
				TextView tv = row.findViewById(R.id.EPCList_ReadCount);
				if (!tv.getText().toString().equals("Count")) {
					int rssi = Integer.parseInt(tv.getText().toString());

					int r, g, b;

					// yellow to red
					g = (int) Math.round(3.27 * rssi - 127.53);
					r = (int) Math.round(-3.27 * rssi + 382.51);

					row.setBackgroundColor(Color.rgb(r, g, 0));
				}
				return row;
			}
		};

		listView.setAdapter(sa);
		listView.invalidate();

	}

	public void StopReading() {

		isStartPingPong = false;
		isKeyDown = false;
		CLReader.Stop();
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				btn_Finder.setText(getString(R.string.btn_Finder_Leer));
				btn_Finder.setClickable(true);
			}
		});
	}

	@Override
	public void onBackPressed() {
		Back(null);
	}

	public void Back(View v) {

		if (btn_Finder.getText().toString()
				.equals(getString(R.string.btn_read_stop))) {
			showMsg(getString(R.string.uhf_please_stop), null);
			return;
		}

		DisposeAll();

		finish();

		FinderActivity.this.finish();
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