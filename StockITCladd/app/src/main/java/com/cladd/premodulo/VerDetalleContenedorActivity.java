package com.cladd.premodulo;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.cladd.entities.api.Productos;
import com.cladd.services.DataBaseHelper;
import com.cladd.uhf.UHFBaseActivity;
import com.hopeland.pda.example.R;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.scanner.ScanReader;
import com.pda.scanner.Scanner;

import java.util.LinkedHashMap;


public class VerDetalleContenedorActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	/** INICIO Definicion de elementos de la vista **/

	private Button btn_VerDetalle = null;

	private TextView tv_VerDetalle_Tipo = null;
	private TextView tv_VerDetalle_Pieza = null;
	private TextView tv_VerDetalle_DepSec = null;
	private TextView tv_VerDetalle_Fecha = null;
	private TextView tv_VerDetalle_Partida = null;
	private TextView tv_VerDetalle_CantEnPartida = null;
	private TextView tv_VerDetalle_Articulo = null;
	private TextView tv_VerDetalle_Color = null;
	private TextView tv_VerDetalle_Cliente = null;
	private TextView tv_VerDetalle_Comprobante = null;
	private TextView tv_VerDetalle_FechaComp = null;
	private Spinner sp_WriteTipo = null;
	private TextView et_VerDetalle_NSerie_Manual = null;
	private RadioGroup rg_VerDetalle = null;
	private RadioButton rb_VerDetalle_BC = null;
	private RadioButton rb_VerDetalle_RF = null;
	private View btn_VerDetalle_Manual = null;

	private Boolean IsFlushList = true; //Whether to refresh the list

	/** FIN Definicion de elementos de la vista **/

	/** INICIO Definicion de Variables del Modulo RF **/

	private static boolean isStartPingPong = false; //Whether to start VerDetalleing tag
	boolean in_reading = false;
	private final LinkedHashMap<String, EPCModel> hmList = new LinkedHashMap<String, EPCModel>();
	private final Object hmList_Lock = new Object();

	/* Sonido */

	private final Object beep_Lock = new Object();
	ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM,
			ToneGenerator.MAX_VOLUME);

	/* Bateria */

	private static final boolean isPowerLowShow = false;//Low or not

	/** FIN Definicion de Variables del Modulo RF **/

	/**
	 * INICIO Definicion de Variables del Modulo Scanner
	 **/

	boolean busy = false;

	//ScanReader object
	private final Scanner scanReader = ScanReader.getScannerInstance();

	/** FIN Definicion de Variables del Modulo Scanner **/


	/** Inicio Definicion Variables Controller **/

	/* Database */

	DataBaseHelper dataBaseHelper=null;

	/* Variable Global */

	private Productos productoGlobal = null;


	/* MESSAGGE Handling */

	private final int MSG_RESULT_VerDetalle = MSG_USER_BEG + 1; //constant
	private final int MSG_UPDATE_ID = MSG_USER_BEG + 2; //constant
	private final int MSG_UHF_POWERLOW = MSG_USER_BEG + 3;

	/* Logging */

	private IAsynchronousMessage log = null;

	static final int PLANO = MSG_USER_BEG + 1;
	static final int PUNTO = MSG_USER_BEG + 2;

	String tipo = getString(R.string.PUNTO); // Tipo Producto
	String Nserie = new String(); // Nserie
	int tipoLectura = 1; // BC o RF

	@Override
	public void OutPutEPC(EPCModel epcModel) {

	}

	/** FIN Definicion Variables Controller **/


}
