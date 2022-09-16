package com.cladd.modulos;

import static java.lang.Thread.sleep;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.cladd.entities.api.Operario;
import com.cladd.entities.model.PDASettings;
import com.cladd.services.DataBaseHelper;
import com.cladd.uhf.UHFBaseActivity;
import com.google.gson.JsonElement;
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
import java.util.Locale;
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
 *	9. Inicializar Listeners
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

public class GrabarPiezaActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	/**
	 * INICIO Definicion de elementos de la vista
	 **/

	private View v_GrabarPieza_LupaSector = null;
	private TextView tb_Write_MatchTID = null;
	private TextView tv_UsuarioLogueado = null;
	private EditText tb_Write_WriteNSerie = null;
	private TextView tb_Write_Sector = null;
	private RadioButton rb_WriteBcToRf_Escanear = null;
	private RadioButton rb_WriteBcToRf_Escribir = null;
	private RadioGroup rg_WriteBcToRf_EscanearOEscribir = null;
	private RadioButton rb_WriteBcToRf_Grabar = null;
	private RadioButton rb_WriteBcToRf_GrabarUbicar = null;
	private RadioGroup rg_WriteBcToRf_GrabarUbicar = null;
	private Spinner sp_WriteTipo = null;
	private Button btn_GrabarPieza = null;

	/** FIN Definicion de elementos de la vista **/


	/**
	 * Inicio Definicion Variables Controller
	 **/

	/* Variable Global */

	boolean rfid = false; // Tag a grabar leido
	String tipo = null; // Tipo Producto
	boolean barcode = false; // BarCode o codigo escrito
	boolean grabando = false;
	boolean procesando = false;// Me aseguro que si apreto 10 veces solo tomo la primera

	/* MESSAGGE Handling */

	private final int MSG_RESULT_RFID = MSG_USER_BEG + 1; // constant
	private static final int MSG_RESULT_SECTOR = MSG_USER_BEG + 3;
	private static final int MSG_WRITE_TAG = MSG_USER_BEG + 4;

	/* Logging */

	private int BcOManual = 0;// 0: Scanner o 1: Escribe codigo
	private int GrabaYUbica = 1;// 0: Graba o 1: Graba y ubica

	/**
	 * FIN Definicion Variables Controller
	 **/

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
		InitRFIDModule(this,getString(R.string.GrabarPieza));
		InitBCModule();
		InitSoundModule();
		InitView();

	}

	@Override
	protected void onResume(){
		super.onResume();

	}

	protected void InitView() {

		this.setContentView(R.layout.grabarpieza);


		showCustomBar(getString(R.string.tv_GrabarPieza_Title),
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

		// campo para escanear Sector
		tv_UsuarioLogueado = findViewById(R.id.tv_UsuarioLogueado);
		 // campo para escanear Sector
		tb_Write_Sector = findViewById(R.id.tb_Write_Sector);
		 // campo para Leer tag a grabar
		tb_Write_MatchTID = findViewById(R.id.tb_Write_MatchTID);

		v_GrabarPieza_LupaSector = findViewById(R.id.v_GrabarPieza_LupaSector);
		 // campo para escanear o escribir nserie
		tb_Write_WriteNSerie = findViewById(R.id.tb_Write_WriteNSerie);
		 // RadioGroup Escanea o Escribe NSerie
		rg_WriteBcToRf_EscanearOEscribir = findViewById(R.id.rg_WriteBcToRf_EscanearOEscribir);
		 // RadioButton Escanear NSerie
		rb_WriteBcToRf_Escanear = findViewById(R.id.rb_WriteBcToRf_Escanear);
		 // RadioButton Escribir NSerie
		rb_WriteBcToRf_Escribir = findViewById(R.id.rb_WriteBcToRf_Escribir);
		 // Select Tipo Producto PU / PL
		sp_WriteTipo = findViewById(R.id.sp_WriteTipo);
		 // Button Grabar
		btn_GrabarPieza = findViewById(R.id.btn_GrabarPieza);
		 // RadioGroup graba solo o graba y ubica
		rg_WriteBcToRf_GrabarUbicar = findViewById(R.id.rg_WriteBcToRf_GrabarUbicar);
		 // RadioButton Grabar solo
		rb_WriteBcToRf_Grabar = findViewById(R.id.rb_WriteBcToRf_Grabar);
		 // RadioButton  Grabar y ubicar
		rb_WriteBcToRf_GrabarUbicar = findViewById(R.id.rb_WriteBcToRf_GrabarUbicar);


		InitListeners();

		DisplayData();

	}

	protected void DisplayData() {
		tv_UsuarioLogueado.setText(_Operario.getDescripcion());
		v_GrabarPieza_LupaSector.setBackground(getDrawable(R.drawable.lupa));
		tipo = getString(R.string.PUNTO);

	}

	protected void InitListeners() {


		v_GrabarPieza_LupaSector.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				tb_Write_Sector.setText(new String());
				RestartViewGrabador();
				HabilitarCampo(1);

			}
		});

		rg_WriteBcToRf_EscanearOEscribir.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == rb_WriteBcToRf_Escanear.getId()) {
					btn_GrabarPieza.setText(R.string.btn_GrabarPieza_Grabar);
					tb_Write_WriteNSerie.setEnabled(false);
					BcOManual = 0;
				}
				if (checkedId == rb_WriteBcToRf_Escribir.getId()) {
					btn_GrabarPieza.setText(R.string.btn_GrabarPieza_GrabarAMano);
					tb_Write_WriteNSerie.setEnabled(true);
					BcOManual = 1;
				}
			}
		});

		sp_WriteTipo.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0,
									   View arg1, int arg2, long arg3) {
				int selectItem = sp_WriteTipo
						.getSelectedItemPosition();
				if(selectItem == 0)
					tipo = getString(R.string.PUNTO);
				if(selectItem == 1)
					tipo = getString(R.string.PLANO);


			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		rg_WriteBcToRf_GrabarUbicar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == rb_WriteBcToRf_Grabar.getId()) {
					//btn_GrabarPieza.setText(R.string.btn_Write_From_Bar_Code);
					RestartViewGrabador();
					tb_Write_Sector.setText("No Ubica");
					HabilitarCampo(2);

					GrabaYUbica = 0;
				}
				if (checkedId == rb_WriteBcToRf_GrabarUbicar.getId()) {
					RestartViewGrabador();
					tb_Write_Sector.setText(new String());
					HabilitarCampo(1);
					GrabaYUbica = 1;
				}
			}
		});

		HabilitarCampo(1);

	}

	protected void HabilitarCampo(int parte) {
		switch (parte) {
			case 1: // inicio de la vista solo puede poner Sector
			{
				tb_Write_Sector.setEnabled(true);
				tb_Write_Sector.setBackgroundColor(Color.WHITE);
				tb_Write_MatchTID.setEnabled(false);
				tb_Write_MatchTID.setBackgroundColor(Color.LTGRAY);
				tb_Write_WriteNSerie.setEnabled(false);
				tb_Write_WriteNSerie.setBackgroundColor(Color.LTGRAY);
				rg_WriteBcToRf_EscanearOEscribir.setEnabled(false);
				rg_WriteBcToRf_EscanearOEscribir.setBackgroundColor(Color.LTGRAY);
				rb_WriteBcToRf_Escanear.setEnabled(false);
				rb_WriteBcToRf_Escanear.setBackgroundColor(Color.LTGRAY);
				rb_WriteBcToRf_Escribir.setEnabled(false);
				rb_WriteBcToRf_Escribir.setBackgroundColor(Color.LTGRAY);
				sp_WriteTipo.setEnabled(false);
				sp_WriteTipo.setBackgroundColor(Color.LTGRAY);
				btn_GrabarPieza.setEnabled(false);
				break;
			}
			case 2: // una vez compleatado el sector puede grabar
			{
				tb_Write_Sector.setEnabled(false);
				tb_Write_Sector.setBackgroundColor(Color.LTGRAY);
				tb_Write_MatchTID.setEnabled(false);
				tb_Write_MatchTID.setBackgroundColor(Color.WHITE);
				tb_Write_WriteNSerie.setEnabled(false);
				tb_Write_WriteNSerie.setBackgroundColor(Color.WHITE);
				rg_WriteBcToRf_EscanearOEscribir.setEnabled(true);
				rg_WriteBcToRf_EscanearOEscribir.setBackgroundColor(Color.WHITE);
				rb_WriteBcToRf_Escanear.setEnabled(true);
				rb_WriteBcToRf_Escanear.setBackgroundColor(Color.WHITE);
				rb_WriteBcToRf_Escribir.setEnabled(true);
				rb_WriteBcToRf_Escribir.setBackgroundColor(Color.WHITE);
				sp_WriteTipo.setEnabled(true);
				sp_WriteTipo.setBackgroundColor(Color.WHITE);
				btn_GrabarPieza.setEnabled(true);
				break;
			}
			default:
				break;

		}
	}

	public void VirtualBtnKeyDown (View v) {

		Button btnGrabarPieza = (Button) v;

		String controlText = btnGrabarPieza.getText().toString();

		if (controlText.equals(getString(R.string.btn_GrabarPieza_Grabar))) {
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

	public void StartReading () {

		if (!tb_Write_Sector.getText().equals(new String())) {
			if (!procesando) {

				procesando = true;
				if (BcOManual == 0)
					RestartViewGrabador();
				else
					tb_Write_MatchTID.setText(new String());

				in_reading = true;
				int ret = -1;

				ret = UHFReader._Tag6C.GetEPC_TID(_NowAntennaNo, _RFIDSingleRead);


				//After reading the tag for 2 seconds, stop
				new Thread() {
					public void run() {
						int wait = 50;
						int timout = 2000;
						try {
							for (int i = 0; i < timout; i += wait) {
								Thread.sleep(wait);
								if (!in_reading) {
									break;
								}
							}
							CLReader.Stop(); //You must stop reading or the write tag will fail

							if (BcOManual == 0) {

								sendMessage(MSG_WRITE_TAG, 1);
								tb_Write_WriteNSerie.setText(new String());

							} // Escanea codigo

							if (BcOManual == 1) {

								barcode = true;
								sendMessage(MSG_WRITE_TAG, 1);

							} // Escribe codigo

						} catch (Exception e) {
						}
					}
				}.start();

			}



			if (!isStartPingPong) {

				btn_GrabarPieza.setText(getString(R.string.btn_read_stop));
				isStartPingPong = true;

			}
		}else
			DeCode();

	}

	protected void RestartViewGrabador() {

		tb_Write_MatchTID.setText(new String());
		tb_Write_WriteNSerie.setText(new String());
		if (BcOManual == 1)
			tb_Write_WriteNSerie.setEnabled(true);

	}

	protected void DeCode() {
		if (busy) {
			showTip(getString(R.string.str_busy));
			return;
		}

		busy = true;

		new Thread() {
			@Override
			public void run() {

				byte[] id = scanReader.decode();

				String idString;
				if (id != null) {

					String utf8 = new String(id, StandardCharsets.UTF_8);

					if (utf8.contains("\ufffd")) {
						utf8 = new String(id, Charset.forName("gbk"));
					}

					idString = utf8;
					toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
					if (tb_Write_Sector.getText().equals(new String()))
						sendMessage(MSG_RESULT_SECTOR, idString);
					else
						sendMessage(MSG_RESULT_BC, idString);

				} else {

					hideWait();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							btn_GrabarPieza.setEnabled(true);
							showMsg(getString(R.string.BC_ErrorLectura));
						}
					});

					procesando = false;

				}


				busy = false;

			}

		}.start();
	}

	@Override
	public void OutPutEPC(EPCModel model) {
		try {
			synchronized (hmList_Lock) {
				if (hmList.isEmpty()) {
					tb_Write_MatchTID.setText(new String());

					//RestartViewGrabador();
					hmList.put(model._EPC + model._TID, model);

				} else
					return;
			}
			synchronized (beep_Lock) {
				beep_Lock.notify();
				sendMessage(MSG_RESULT_RFID, model._TID);

			}

		} catch (Exception ex) {
			Log.d("Erroroutput",ex.getMessage());
		}

	}

	/**
	 * API
	 */

	public void getDataAGrabar(String tipo, String nSerie,String deposito) {
		rfid=false;
		barcode=false;
		Clear(null);
		List<String> reqKeys = GetReqKeysList();
		List<Object> reqValues = GetReqValuesList(tipo,nSerie,deposito);

		Call<JsonElement> call = GetApiService(BaseUrlApi).GrabarEtiquetaStockInicial(CreateReqBodyAndLogToServer(reqKeys,reqValues,getString(R.string.API_GrabarPieza_Nombre)));

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				procesando = false;
				btn_GrabarPieza.setEnabled(true);
				try {
					if (response.isSuccessful()) {
						JSONObject j = new JSONObject(response.body().toString());

						JSONObject responseState = new JSONObject(j.getString("responseState"));

						if (responseState.getBoolean("isError")) {
							LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA,"getDataAGrabarYUbicar", 4, Calendar.getInstance().toString(), responseState.getString("errorMessage"));
							showMsg(responseState.getString("errorMessage") + getString(R.string.str_br) + responseState.getString("observacion"));
						} else {
							LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA,"getDataAGrabarYUbicar", 2, Calendar.getInstance().toString(), j.toString());

							JSONObject dataSource = new JSONObject(j.getString("dataSource"));
							String id = dataSource.getString("id");
							if (id.length() == 24)
								GrabarTag(id);

						}
					}else{
						showMsg(response.message());
					}
				} catch (Exception ex) {
					LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA,"getDataAGrabarYUbicar", 4, Calendar.getInstance().toString(), ex.getMessage());
					showMsg("ERROR inesperado:" + ex.getMessage(), null);
				}
				hideWait();
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				procesando = false;
				btn_GrabarPieza.setEnabled(true);
				LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA,"getDataAGrabarYUbicar", 4, Calendar.getInstance().toString(), t.getMessage());
				showMsg(t.getMessage(), null);
				hideWait();
			}
		});
	}

	public void getDataAGrabarSinUbicar(String tipo, String nSerie) {
		rfid=false;
		barcode=false;
		Clear(null);

		List<String> reqKeys = GetReqKeysListSinUbicar();
		List<Object> reqValues = GetReqValuesListSinUbicar(tipo,nSerie);

		Call<JsonElement> call = GetApiService(BaseUrlApi).GrabarEtiquetaSinUbicarStockInicial(CreateReqBodyAndLogToServer(reqKeys,reqValues,getString(R.string.API_GrabarPieza_NombreSinUbicar)));

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				procesando = false;
				btn_GrabarPieza.setEnabled(true);
				try {
					if (response.isSuccessful()) {
						JSONObject resp = new JSONObject(response.body().toString());

						LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA, getString(R.string.API_GrabarPieza_NombreSinUbicar), 2, Calendar.getInstance().toString(), resp.toString());

						if (IsAPIResponseStateOK(resp, getString(R.string.API_GrabarPieza_NombreSinUbicar))) {

							JSONObject dataSource = new JSONObject(resp.getString(getString(R.string.API_DATASOURCE)));
							String id = dataSource.getString("id");
							if (id.length() == 24)
							{
								UHFReader._Config.SetANTPowerParam(_NowAntennaNo, Integer.parseInt(getString(R.string.RFID_MAXPOWER)));
								Thread.sleep(500);
								GrabarTag(id);
							}

						}
					}else
						showMsg(response.message().toString());

				} catch (Exception ex) {
					showMsg("ERROR inesperado:" + ex.getMessage(), null);
				}
				hideWait();
			}


			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				procesando = false;
				btn_GrabarPieza.setEnabled(true);
				LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA,getString(R.string.API_GrabarPieza_NombreSinUbicar), 4, Calendar.getInstance().toString(), t.getMessage());
				showMsg(t.getMessage(), null);
				Clear(null);
				hideWait();
			}


		});
	}

	protected List<String> GetReqKeysList(){

		List<String> list= new ArrayList<>();

		list.add(getString(R.string.API_GrabarPieza_Req_TipoProd));
		list.add(getString(R.string.API_GrabarPieza_Req_NSerie));
		list.add(getString(R.string.API_GrabarPieza_Req_Deposito));
		list.add(getString(R.string.API_GrabarPieza_Req_Operario));
		list.add(getString(R.string.API_GrabarPieza_Req_EscribeNSerie));

		return list;

	}

	protected List<Object> GetReqValuesList(String tipo, String nSerie, String deposito){

		List<Object> list= new ArrayList<>();

		list.add(tipo);
		list.add(nSerie);
		list.add(deposito);
		list.add(_Operario.getLogeo().toLowerCase(Locale.ROOT).equals("ADMIN".toLowerCase(Locale.ROOT))?"MG":_Operario.getLogeo());
		list.add(BcOManual == 1 ? true : false);

		return list;

	}

	protected List<String> GetReqKeysListSinUbicar(){

		List<String> list= new ArrayList<>();

		list.add(getString(R.string.API_GrabarPieza_Req_TipoProd));
		list.add(getString(R.string.API_GrabarPieza_Req_NSerie));
		list.add(getString(R.string.API_GrabarPieza_Req_Operario));
		list.add(getString(R.string.API_GrabarPieza_Req_EscribeNSerie));

		return list;

	}

	protected List<Object> GetReqValuesListSinUbicar(String tipo, String nSerie){

		List<Object> list= new ArrayList<>();

		list.add(tipo);
		list.add(nSerie);
		list.add(_Operario.getLogeo());
		list.add(BcOManual == 1);

		return list;

	}

	/**
	 * End API
	 */

	@Override
	protected void msgProcess(Message msg) {
		switch (msg.what) {
			case MSG_RESULT_RFID:
				TagAGrabar(msg.obj.toString()); 
				break;
			case MSG_RESULT_SECTOR:
				if (msg.obj != null) {
					SectorLeido(msg.obj.toString());
				}
				break;
			case MSG_RESULT_BC:
				if (msg.obj != null) {
					BCLeido(msg.obj.toString());
				}
				break;
			case MSG_WRITE_TAG:
				checkRFIDAndBC(Integer.parseInt(msg.obj.toString()));
				break;
			default:
				super.msgProcess(msg);
				break;
		}
	}

	public void TagAGrabar(String tid) {

		tb_Write_MatchTID.setText(tid);
		rfid = true;
		if (BcOManual == 0)
			DeCode();
		else {
			procesando = false;
			btn_GrabarPieza.setEnabled(true);
			if (tb_Write_WriteNSerie.getText().toString().equals(new String()))
				showMsg(getString(R.string.error_GrabarPieza_CodigoErroneo));
			else
				sendMessage(MSG_WRITE_TAG, 2);
		}

	}
	
	public void SectorLeido(String sector) {
		tb_Write_Sector.setText(sector);
		HabilitarCampo(2);
		v_GrabarPieza_LupaSector.setBackground(getDrawable(R.drawable.edit));

	}

	protected void BCLeido (String nserie){

		tb_Write_WriteNSerie.setText(nserie);
		barcode = true;
		sendMessage(MSG_WRITE_TAG, 2);


	}

	public void checkRFIDAndBC(int rfobc) {

		grabando = rfid && barcode;

		String tagid = tb_Write_MatchTID.getText().toString();
		String barcodeid = tb_Write_WriteNSerie.getText().toString().replace(getString(R.string.str_br), new String());

		//RF=1
		if (rfobc == 1) {
			if (tagid.equals(new String())) {
				hideWait();
				procesando = false;
				btn_GrabarPieza.setEnabled(true);
				Clear(null);
				showMsg(getString(R.string.RFID_ErrorLectura));
			}
		}
		//BC=2
		if (rfobc == 2) {
			if (barcodeid.equals(new String())) {
				hideWait();
				btn_GrabarPieza.setEnabled(true);
				procesando = false;
				Clear(null);
			}
		}

		if (grabando && !barcodeid.equals(new String()) && !tagid.equals(new String())) {
			if (GrabaYUbica == 0) {
				showWait(getString(R.string.waiting));
				getDataAGrabarSinUbicar(tipo, barcodeid);
			}else{
				showWait(getString(R.string.waiting));
				getDataAGrabar(tipo, barcodeid,tb_Write_Sector.getText().toString());
			}
		}
	}
	
	public void StopReading() {

		isStartPingPong = false;
		CLReader.Stop();
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				btn_GrabarPieza.setText(getString(R.string.btn_GrabarPieza_Grabar));
				btn_GrabarPieza.setClickable(true);
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

		DisposeAll();

		finish();

		GrabarPiezaActivity.this.finish();
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

	/**
	 * Functions
	 */

	public void ShowPopupExitoGrabacion(String tag) {

		Intent popup = new Intent(GrabarPiezaActivity.this, PopUpActivity.class);
		popup.putExtra("pieza", tag);
		popup.putExtra("exito", true);
		startActivity(popup);

	}

	public void GrabarTag(String hex) {

		int dataLen = 0; // Length of Write Data
		String strInput = hex;
		strInput = String.format(strInput, 24, "0").replace(getString(R.string.str_br), new String());

		if (!checkHexInput(strInput)) { // check
			showMsg("ERROR", null);
			return;
		}

		dataLen = hex.length() % 4 == 0 ? hex.length() / 4
				: hex.length() / 4 + 1;

		if (dataLen > 0) {

			int ret = -1;
				ret = UHFReader._Tag6C.WriteEPC_MatchTID(_NowAntennaNo, hex, tb_Write_MatchTID.getText().toString(), 0);
				if (ret != 0) {

					hideWait();
					LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA,getString(R.string.metodo_GrabarPieza_GrabarTag), 4, Calendar.getInstance().toString(), "Salio del rango de la etiqueta");
					showMsg(getString(R.string.RFID_ErrorGrabacion));

				} else {

					try {
						CLReader.Stop();
						Thread.sleep(500);
						UHFReader._Config.SetANTPowerParam(_NowAntennaNo, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_MAXPOWER))));
						Thread.sleep(500);
						CLReader.Stop();
						Thread.sleep(500);
						hideWait();
						RestartViewGrabador();
						HabilitarCampo(2);
						ShowPopupExitoGrabacion(hex);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

				grabando = false;

		}
	}

	// Boton grabar en View
	public void WriteHand(View v) {
		btn_GrabarPieza.setEnabled(false);
		if (rb_WriteBcToRf_Escanear.isChecked())
			ReadMactchTag();

		if (rb_WriteBcToRf_Escribir.isChecked())
			HandWriteTag();

	}

	// Read the tag TID that you want to write
	public void ReadMactchTag() {
		tb_Write_MatchTID.setText(new String());
		//tb_Write_WriteNSerie.setText(new String());
		in_reading = true;
		String rt = new String();
		int ret = -1;

		CLReader.Stop();
		UHFReader._Config.SetANTPowerParam(1, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_AntPowerBCtoRFRead))));

		ret = UHFReader._Tag6C.GetEPC_TID(_NowAntennaNo, _RFIDSingleRead);

		int retval = CLReader.GetReturnData(rt);
		if (!UHF_CheckReadResult(retval)) {
			CLReader.Stop();
			sendMessage(MSG_WRITE_TAG, 1);
			in_reading = false;
			return;
		}

		//After reading the tag for 2 seconds, stop
		new Thread() {
			public void run() {
				int wait = 50;
				int timout = 2000;
				try {
					for (int i = 0; i < timout; i += wait) {
						Thread.sleep(wait);
						if (!in_reading) {
							break;
						}
					}
					CLReader.Stop(); //You must stop reading or the write tag will fail
					sendMessage(MSG_WRITE_TAG, 1);

					DeCode();


				} catch (Exception e) {
				}

			}
		}.start();
	}

	// Read the tag TID that you want to hand write
	public void HandWriteTag() {
		tb_Write_MatchTID.setText(new String());

		in_reading = true;
		String rt = new String();
		int ret = -1;

		CLReader.Stop();

		UHFReader._Config.SetANTPowerParam(_NowAntennaNo, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.LeerTagContenedor)+getString(R.string.DB_DynamicConfig_Name_antPower))));

		Delay(50);
		ret = UHFReader._Tag6C.GetEPC_TID(_NowAntennaNo, _RFIDSingleRead);

		int retval = CLReader.GetReturnData(rt);
		if (!UHF_CheckReadResult(retval)) {

			CLReader.Stop();
			sendMessage(MSG_WRITE_TAG, 1);
			in_reading = false;
			return;

		}
		//After reading the tag for 2 seconds, stop
		new Thread() {
			public void run() {
				int wait = 50;
				int timout = 2000;
				try {
					for (int i = 0; i < timout; i += wait) {
						Thread.sleep(wait);
						if (!in_reading) {
							break;
						}
					}
					CLReader.Stop(); //You must stop reading or the write tag will fail

					if (tb_Write_WriteNSerie.getText().toString().equals(new String())) {

						showMsg(getString(R.string.error_GrabarPieza_CodigoErroneo));

					} else {

						barcode = true;
						sendMessage(MSG_WRITE_TAG, 2);

					}
				} catch (Exception e) {
				}
			}
		}.start();

	}



}
