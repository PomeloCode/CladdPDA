package com.cladd.modulos;

import static java.lang.Thread.sleep;

import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.cladd.entities.model.Pieza;
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 *  1. Abrir modulo de rfid
 *  2. Configurar Antena rfid
 *  3. Abrir modulo de bc
 *  4. Inicializar Modulo de sonido
 *  5. Abrir conexion con la db y traer datos
 *  6. Inicializar vista
 *  7. Bindear vista con variables
 *  8. Mostrar datos
 *	9. Inicializar Listeners (Callbacks visuales)
 *	10. Oprimir boton de lectura
 *	11. Reiniciar la vista (en caso de no ser la primera etiqueta consultada)
 *	12. Leer Etiqueta
 *	13. Llamar a la api para obtener la informacion de la pieza
 *	14. Mostrar informacion obtenida
 *	15. Repetir n veces
 *  16. Cerrar todas las conexiones y destruir la activity
 *
 *
 */

public class VerDetallePiezaActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	/** INICIO Definicion de elementos de la vista **/

	private TextView tv_UsuarioLogueado = null;
	private TextView tv_VerDetallePieza_Tipo = null;
	private TextView tv_VerDetallePieza_Pieza = null;
	private TextView tv_VerDetallePieza_DepSec = null;
	private TextView tv_VerDetallePieza_Fecha = null;
	private TextView tv_VerDetallePieza_Partida = null;
	private TextView tv_VerDetallePieza_CantEnPartida = null;
	private TextView tv_VerDetallePieza_Articulo = null;
	private TextView tv_VerDetallePieza_Color = null;
	private TextView tv_VerDetallePieza_Cliente = null;
	private TextView tv_VerDetallePieza_Comprobante = null;
	private TextView tv_VerDetallePieza_FechaComp = null;
	private Spinner sp_VerDetallePieza_Manual_TipoProducto  = null;
	private TextView et_VerDetallePieza_Manual_NSerie = null;
	private RadioGroup rg_VerDetallePieza = null;
	private RadioButton rb_VerDetallePieza_BC = null;
	private RadioButton rb_VerDetallePieza_RFID = null;
	private Button btn_VerDetallePieza = null;
	private View btn_VerDetallePieza_Manual_Lupa = null;


	/** FIN Definicion de elementos de la vista **/

	/** Inicio Definicion Variables Controller **/

	private Pieza piezaGlobal = null;

	/* MESSAGGE Handling */

	private final int MSG_RESULT_VerDetallePieza = MSG_USER_BEG + 1;


	/* Logging */

	private static final int BC = 0;
	private static final int RFID = 1;

	String tipo = null; // Tipo Producto
	String Nserie = new String();  // Nserie
	int tipoLectura = RFID; // BC o RFID

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
		showWait("Configurando Antena");
		InitRFIDModule(this,getString(R.string.VerDetallePieza));


	}

	protected void InitView() {

		this.setContentView(R.layout.verdetallepieza);

		showCustomBar(getString(R.string.tv_VerDetallePieza_Title),
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

	protected void BindViews(){

		// btn Leer etiqueta
		btn_VerDetallePieza = findViewById(R.id.btn_VerDetallePieza);
		// TextView campo Operario logeado
		tv_UsuarioLogueado = findViewById(R.id.tv_UsuarioLogueado);
		// TextView campo Tipo Producto
		tv_VerDetallePieza_Tipo = findViewById(R.id.tv_VerDetallePieza_Tipo);
		// TextView campo nserie
		tv_VerDetallePieza_Pieza = findViewById(R.id.tv_VerDetallePieza_Pieza);
		// TextView campo Deposito Sector
		tv_VerDetallePieza_DepSec = findViewById(R.id.tv_VerDetallePieza_DepSec);
		// TextView campo Fecha
		tv_VerDetallePieza_Fecha = findViewById(R.id.tv_VerDetallePieza_Fecha);
		// TextView campo nro Partida
		tv_VerDetallePieza_Partida = findViewById(R.id.tv_VerDetallePieza_Partida);
		// TextView campo cantidad de piezas en partida
		tv_VerDetallePieza_CantEnPartida = findViewById(R.id.tv_VerDetallePieza_CantEnPartida);
		// TextView campo descripcion de articulo
		tv_VerDetallePieza_Articulo = findViewById(R.id.tv_VerDetallePieza_Articulo);
		// TextView campo descripcion del color
		tv_VerDetallePieza_Color = findViewById(R.id.tv_VerDetallePieza_Color);
		// TextView campo Cliente
		tv_VerDetallePieza_Cliente = findViewById(R.id.tv_VerDetallePieza_Cliente);
		// TextView campo nro Comprobante
		tv_VerDetallePieza_Comprobante = findViewById(R.id.tv_VerDetallePieza_Comprobante);
		// TextView campo fecha de comprobante
		tv_VerDetallePieza_FechaComp = findViewById(R.id.tv_VerDetallePieza_FechaComp);
		// Select Tipo producto para busqueda manual
		sp_VerDetallePieza_Manual_TipoProducto = findViewById(R.id.sp_WriteTipo);
		// Campo para nSerie a buscar manual
		et_VerDetallePieza_Manual_NSerie = findViewById(R.id.et_VerDetallePieza_NSerie_Manual);
		// rg Ver Detalle BC:0 o RF:1
		rg_VerDetallePieza  = findViewById(R.id.rg_VerDetallePieza);
		// rb Ver Detalle Escaneando
		rb_VerDetallePieza_BC = findViewById(R.id.rb_VerDetallePieza_BC);
		// rb Ver Detalle RF
		rb_VerDetallePieza_RFID = findViewById(R.id.rb_VerDetallePieza_RF);
		// btn Busqueda manual
	 	btn_VerDetallePieza_Manual_Lupa = findViewById(R.id.btn_VerDetallePieza_Manual);

		InitListeners();

		DisplayData();

	}

	protected void DisplayData() {
		tv_UsuarioLogueado.setText(_Operario.getDescripcion());
		tipo = getString(R.string.PUNTO);
	}

	protected void InitListeners(){

		sp_VerDetallePieza_Manual_TipoProducto.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0,
									   View arg1, int arg2, long arg3) {
				int selectItem = sp_VerDetallePieza_Manual_TipoProducto
						.getSelectedItemPosition();
				switch (selectItem) {
					case PUNTO:
						tipo = getString(R.string.PUNTO);
						break;
					case PLANO:
						tipo = getString(R.string.PLANO);
						break;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		rg_VerDetallePieza.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RestartViewPieza();
				RadioButton rb = findViewById(checkedId);

				if (rb.getText().toString().equals("Codigo de barra")) {// BC
					tipoLectura = BC;
				} else if (rb.getText().toString().equals("Antena")) {// RF
					tipoLectura = RFID;
				}


			}
		});

		btn_VerDetallePieza.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				VirtualBtnKeyDown(v);
			}
		});

		btn_VerDetallePieza_Manual_Lupa.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				BusquedaManual(v);
			}
		});

	}

	public void VirtualBtnKeyDown (View v) {

		Button btn = (Button) v;

		String controlText = btn.getText().toString();

		if (controlText.equals(getString(R.string.btn_VerDetallePieza))) {
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

	@Override
	public void StartReading() {
		RestartViewPieza();

		if(tipoLectura == RFID) {

			btn_VerDetallePieza.setText(getString(R.string.btn_read_stop));

			if (!isStartPingPong) {

				isStartPingPong = true;

				StartReadingEPC();

			}
		}else{
			DeCode();
		}
	}

	protected void RestartViewPieza() {

		tv_VerDetallePieza_Tipo.setText(new String());
		tv_VerDetallePieza_Pieza.setText(new String());
		tv_VerDetallePieza_DepSec.setText(new String());
		tv_VerDetallePieza_Fecha.setText(new String());
		tv_VerDetallePieza_Partida.setText(new String());
		tv_VerDetallePieza_CantEnPartida.setText(new String());
		tv_VerDetallePieza_Articulo.setText(new String());
		tv_VerDetallePieza_Color.setText(new String());
		tv_VerDetallePieza_Cliente.setText(new String());
		tv_VerDetallePieza_Comprobante.setText(new String());
		tv_VerDetallePieza_FechaComp.setText(new String());

	}

	protected void BusquedaManual(View v){

		RestartViewPieza();

		Nserie = et_VerDetallePieza_Manual_NSerie.getText().toString();

		if(!Nserie.equals(new String()))
			GetPieza(new String(),tipo,Nserie);
		else
			showMsg(getString(R.string.error_VerDetallePieza_NSerie_Manual_Vacio));

	}

	protected void BCLeido (String nserie){

		et_VerDetallePieza_Manual_NSerie.setText(nserie);
		GetPieza(new String(),tipo,nserie);


	}

	@Override
	public void OutPutEPC(EPCModel model) {
		if (!model._EPC.substring(0, 4).equals(getString(R.string.RFID_VirginStartTag))) {
			if (model._EPC.substring(2, 4).equals(getString(R.string.StockITBusinessID))) {
				try {
					synchronized (hmList_Lock) {
						if (hmList.isEmpty()) {

							RestartViewPieza();
							hmList.put(model._EPC + model._TID, model);

						} else
							return;
					}
					synchronized (beep_Lock) {
						beep_Lock.notify();
						GetPieza(model._EPC, new String(), new String());
					}

				} catch (Exception ex) {
					Log.d("Erroroutput", ex.getMessage());
				}

			}else
				showMsg("Esta Etiqueta no pertenece a una pieza");
		}
		else
			showMsg("Etiqueta no Grabada");
	}

	/**
	 * API
	 * @return
	 */

	protected void GetPieza(String hex,String tipoProd,String nserie) {

		showWait(getString(R.string.waiting));

		List<String> reqKeys = GetReqKeysList();
		List<Object> reqValues = GetReqValuesList(hex,tipoProd,nserie);

		Call<JsonElement> call = GetApiService(BaseUrlApi).ConsultarPieza(CreateReqBodyAndLogToServer(reqKeys,reqValues,getString(R.string.API_VerDetallePieza_Nombre)));

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				try {
					if (response.isSuccessful()) {

						JSONObject resp = new JSONObject(response.body().toString());

						LogToExternalServer(getString(R.string.str_PDA),UNIQUEIDPDA,getString(R.string.API_VerDetallePieza_Nombre),2, Calendar.getInstance().toString(),resp.toString());

						if (IsAPIResponseStateOK(resp,getString(R.string.API_VerDetallePieza_Nombre))) {

							piezaGlobal = new Gson().fromJson(resp.getString(getString(R.string.API_DATASOURCE)), new TypeToken<Pieza>() {}.getType());;

							sendMessage(MSG_RESULT_VerDetallePieza, null);

						}

					} else {
						showMsg(getString(R.string.error_VerDetallePieza_ApiDevolvioRaro), null);
					}
				} catch (Exception ex) {
					showMsg("Error se leyo:" + hex + getString(R.string.str_br) + ex.getMessage().toString(), null);
				}
				Clear(null);
				RestartViewBuscadorManual();
				hideWait();
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				Clear(null);
				hideWait();
				showMsg(t.getMessage(), null);
				LogToExternalServer(getString(R.string.str_PDA),UNIQUEIDPDA,getString(R.string.API_VerDetallePieza_Nombre),4, Calendar.getInstance().toString(),t.getMessage());
			}
		});


	}

	protected List<String> GetReqKeysList(){

		List<String> list= new ArrayList<>();

		list.add(getString(R.string.API_VerDetallePieza_Req_ID));
		list.add(getString(R.string.API_VerDetallePieza_Req_TipoProd));
		list.add(getString(R.string.API_VerDetallePieza_Req_NSerie));

		return list;

	}

	protected List<Object> GetReqValuesList(String hex, String tipoProd, String nserie){

		List<Object> list= new ArrayList<>();

		list.add(hex);
		list.add(tipoProd);
		list.add(nserie);

		return list;

	}

	/**
	 * End API
	 */

	protected void RestartViewBuscadorManual() {

		et_VerDetallePieza_Manual_NSerie.setText(new String());

	}

	@Override
	protected void msgProcess(Message msg) {
		switch (msg.what) {
			case MSG_RESULT_VerDetallePieza:
				if (piezaGlobal != null)
					DisplayPieza();
				break;
			case MSG_RESULT_BC:
				if(!msg.obj.toString().equals(new String()))
					BCLeido(msg.obj.toString());
				break;
			case MSG_RESULT_BEEP:
					toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
				break;
			default:
				super.msgProcess(msg);
				break;
		}
	}

	protected void DisplayPieza() {

		tv_VerDetallePieza_Tipo.setText(piezaGlobal.getTipoProducto());
		tv_VerDetallePieza_Pieza.setText(piezaGlobal.getnSerie());
		tv_VerDetallePieza_DepSec.setText(piezaGlobal.getDeposito() + "/" + piezaGlobal.getSector());
		tv_VerDetallePieza_Fecha.setText(piezaGlobal.getFecha());
		tv_VerDetallePieza_Partida.setText(piezaGlobal.getPartida().toString());
		tv_VerDetallePieza_CantEnPartida.setText(piezaGlobal.getCantEnPartida());
		tv_VerDetallePieza_Articulo.setText(piezaGlobal.getDescArticulo());
		tv_VerDetallePieza_Color.setText(piezaGlobal.getDescColor());
		tv_VerDetallePieza_Cliente.setText(piezaGlobal.getCliente());
		tv_VerDetallePieza_Comprobante.setText(piezaGlobal.getComprobante());
		tv_VerDetallePieza_FechaComp.setText(piezaGlobal.getFechaComprobante());

		btn_VerDetallePieza.setText(R.string.btn_VerDetallePieza);

		piezaGlobal = null;

	}

	@Override
	public void StopReading() {
		super.StopReading();
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				btn_VerDetallePieza.setText(getString(R.string.btn_VerDetallePieza));
				btn_VerDetallePieza.setClickable(true);
			}
		});
	}

	/**
	 * End Controller
	 */ // Controller


}

