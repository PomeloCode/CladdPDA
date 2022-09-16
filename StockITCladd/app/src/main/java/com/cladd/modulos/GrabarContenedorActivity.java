package com.cladd.modulos;

import static java.lang.Thread.sleep;

import android.content.DialogInterface;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cladd.entities.api.Operario;
import com.cladd.entities.model.PDASettings;
import com.cladd.services.DataBaseHelper;
import com.cladd.uhf.UHFBaseActivity;
import com.hopeland.pda.example.R;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.rfid.uhf.UHFReader;
import com.util.Helper.Helper_ThreadPool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 *
 *  1. Abrir modulo de rfid
 *  2. Configurar Antena rfid
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
public class GrabarContenedorActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	private TextView tv_UsuarioLogueado = null;
	private TextView tv_GrabarContenedor_TagAGrabar = null;
	private EditText et_GrabarContenedor_Contenedor = null;
	private Button btn_GrabarContenedor = null;

	boolean procesando = false;

	private final int MSG_MATCH_READ = MSG_USER_BEG + 1; // constant
	private final int MSG_RESULT_RFID = MSG_USER_BEG + 1; // constant
	private static final int MSG_RESULT_CHECK = MSG_USER_BEG + 2;

	private int cambioPotAntena = 0;

	/**
	 *  Controller
	 */ // Controller

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// create
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		InitDBConexion();
		InitRFIDModule(this,getString(R.string.GrabarContenedor));
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
				ConfigureRFIDModule(getString(R.string.GrabarContenedor));
			}
		});
	}

	protected void InitView() {

		this.setContentView(R.layout.grabarcontenedor);


		showCustomBar(getString(R.string.tv_GrabarContenedor_Title),
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

		// Visor de tag a grabar (TID)
		tv_GrabarContenedor_TagAGrabar = findViewById(R.id.tv_GrabarContenedor_TagAGrabar);
		// Campo de contenedor
		et_GrabarContenedor_Contenedor = findViewById(R.id.et_GrabarContenedor_Contenedor);
		// boton Grabar
		btn_GrabarContenedor = findViewById(R.id.btn_GrabarContenedor);
		tv_UsuarioLogueado = findViewById(R.id.tv_UsuarioLogueado);

		InitListeners();

		DisplayData();

	}

	protected void DisplayData() {
		tv_UsuarioLogueado.setText(_Operario.getDescripcion());
	}

	protected void InitListeners() {


		btn_GrabarContenedor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				VirtualBtnKeyDown(view);

			}
		});


	}

	public void VirtualBtnKeyDown (View v) {

		Button btnGrabar = (Button) v;

		String controlText = btnGrabar.getText().toString();

		if (controlText.equals(getString(R.string.btn_GrabarContenedor_Grabar))) {
			StartReading();
		}
		else
			StopReading();

	}

	public void StartReading () {
		if (!et_GrabarContenedor_Contenedor.getText().toString().equals(new String()))
		{
			if (!procesando) {

				procesando = true;
				btn_GrabarContenedor.setText(getString(R.string.btn_read_stop));
				tv_GrabarContenedor_TagAGrabar.setText(new String());
				isStartPingPong = true;
				in_reading = true;

				int ret = -1;

				UHFReader._Config.SetANTPowerParam(1, Integer.parseInt(dataBaseHelper.getDynamicConfigsData(getString(R.string.RFID_AntPowerWriteContainerRead))));

				if (cambioPotAntena == 0) {

					GetEPCTID();

				}
				StopperTimer();
			}
		}
		else
			showMsg(getString(R.string.error_GrabarContenedor_Contenedor_Vacio));
	}

	protected void StopperTimer() {
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
					CLReader.Stop();
					sendMessage(MSG_RESULT_CHECK, 1);
				} catch (Exception e) {

				}
			}
		}.start();

	}

	protected void RestartViewGrabador() {
		tv_GrabarContenedor_TagAGrabar.setText(new String());
		et_GrabarContenedor_Contenedor.setText(new String());
	}

	private int GetEPCTID() {

		int ret = -1;
		in_reading = true;

		ret = UHFReader._Tag6C.GetEPC_TID(_NowAntennaNo, _RFIDSingleRead);

		return ret;

	}

	@Override
	public void OutPutEPC(EPCModel model) {

		try {
			synchronized (hmList_Lock) {

				if (hmList.isEmpty()) {

					hmList.put(model._EPC + model._TID, model);
					sendMessage(MSG_MATCH_READ, model._TID);
					in_reading = false;

				} else
					return;
			}
			synchronized (beep_Lock) {
				beep_Lock.notify();
				sendMessage(MSG_RESULT_RFID, model._TID);

			}

		} catch (Exception ex) {
			Log.d(new String(),ex.getMessage());
		}

	}

	/**
	 * API
	 */

	public void getDataAGrabar(String contenedor) {

		Clear(null);

		List<String> reqKeys = GetReqKeysList();
		List<String> reqValues = GetReqValuesList(contenedor);

		Call<ResponseBody> call = GetApiService(BaseUrlApi).GrabarEtiquetaContenedor(contenedor);

		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				procesando = false;
				btn_GrabarContenedor.setEnabled(true);
				try {
					/*
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
					}
					*/
					if (response.isSuccessful()) {
						String b=response.body().string().replace('"',' ').trim().toString();
						if (b.length() == 24)
							GrabarTag(b);
						else {
							showMsg(getString(R.string.error_GrabarContenedor_ApiDevolvioRaro), null);
						}
					}
				} catch (Exception ex) {
					LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA,getString(R.string.API_GrabarContenedor_Nombre), 4, Calendar.getInstance().toString(), ex.getMessage());
					showMsg(getString(R.string.error_GrabarContenedor_ApiDevolvioRaro), null);
				}
				hideWait();
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				procesando = false;
				LogToExternalServer(getString(R.string.str_PDA), UNIQUEIDPDA,"getDataAGrabar", 4, Calendar.getInstance().toString(), t.getMessage());
				btn_GrabarContenedor.setEnabled(true);
				showMsg(t.getMessage());
				hideWait();
			}
		});
	}

	protected List<String> GetReqKeysList(){

		List<String> list= new ArrayList<>();

		list.add(getString(R.string.RFID_ErrorGrabacion));

		return list;

	}

	protected List<String> GetReqValuesList(String contenedor){

		List<String> list= new ArrayList<>();

		list.add(contenedor);

		return list;

	}

	/**
	 * End API
	 */

	@Override
	protected void msgProcess(Message msg) {
		switch (msg.what) {
			case MSG_RESULT_CHECK:
				checkRFIDAndContenedor(Integer.parseInt(msg.obj.toString()));
				break;
			case MSG_RESULT_RFID:
				TagAGrabar(msg.obj.toString()); // Refresh the list
				break;
			default:
				super.msgProcess(msg);
				break;
		}
	}

	public void TagAGrabar(String tid) {


		tv_GrabarContenedor_TagAGrabar.setText(tid);


	}

	public void checkRFIDAndContenedor(int rfobc) {

		String tagid = tv_GrabarContenedor_TagAGrabar.getText().toString();
		String barcodeid = et_GrabarContenedor_Contenedor.getText().toString().replace(getString(R.string.str_br), new String());

		//RF=1
		if (rfobc == 1) {
			procesando=false;
			if (tagid.equals(new String())) {
				hideWait();
				showMsg(getString(R.string.RFID_ErrorLectura));
			}
		}

		if (!tagid.equals(new String())){
			procesando=false;
			showWait(getString(R.string.waiting));
			getDataAGrabar(barcodeid);
		}
	}

	public void GrabarTag(String hex) {
		int dataLen = 0; // Length of Write Data
		dataLen = et_GrabarContenedor_Contenedor.getText().length() % 4 == 0 ? et_GrabarContenedor_Contenedor
				.getText().length() / 4
				: et_GrabarContenedor_Contenedor.getText().length() / 4 + 1;
		if (dataLen > 0) {

			int ret = -1;
			cambioPotAntena = UHFReader._Config.SetANTPowerParam(1, 33);
			if (cambioPotAntena == 0) {
				ret = UHFReader._Tag6C.WriteEPC_MatchTID(_NowAntennaNo, hex, tv_GrabarContenedor_TagAGrabar.getText().toString(), 0);
				if (ret != 0) {
					hideWait();
					showMsg(getString(R.string.RFID_ErrorGrabacion));
				} else {
					hideWait();
					RestartViewGrabador();
					showMsg("Se grabo Con exito: " + hex);
				}
			} else {
				hideWait();
				showMsg(getString(R.string.RFID_ErrorCambioPotencia), null);
			}
		}
	}

	public void StopReading() {

		isStartPingPong = false;
		CLReader.Stop();
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				btn_GrabarContenedor.setText(getString(R.string.btn_GrabarContenedor_Grabar));
				btn_GrabarContenedor.setClickable(true);
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

		GrabarContenedorActivity.this.finish();
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