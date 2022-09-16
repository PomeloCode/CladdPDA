package com.cladd.uhf;

import static com.pda.rfid.uhf.UHFReader._Config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.cladd.entities.api.Operario;
import com.cladd.entities.model.PDASettings;
import com.cladd.services.DataBaseHelper;
import com.hopeland.pda.example.R;
import com.pda.rfid.EPCModel;
import com.port.Adapt;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.rfid.uhf.UHF;
import com.pda.rfid.uhf.UHFReader;
import com.util.BaseActivity;
import com.util.Helper.Helper_ThreadPool;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;



/**
 * @author RFID_C Base Activity
 */
public class UHFBaseActivity extends BaseActivity implements IAsynchronousMessage {

	static Boolean _UHFSTATE = false; // Determine if the module is open
	public static int _NowAntennaNo = 1; // Antenna No.
	static int _UpDataTime = 0; // Repeat tag upload time, control tag upload speed not too fast
	public static int _Max_Power = 30; // Maximum transmitting power of the reader
	public static int _Min_Power = 0; // Minimum transmitting power of the reader


	public static final int _RFIDSingleRead = 0;
	public static final int _RFIDInventoryRead = 1;

	static int low_power_soc = 10;

	public IAsynchronousMessage log = null;
	public static boolean isStartPingPong = false; //Whether to start reading tag
	public boolean isKeyDown = false; // Whether the trigger is pressed
	public Boolean IsFlushList = true; //Whether to refresh the list
	public boolean in_reading = false;
	public final LinkedHashMap<String, EPCModel> hmList = new LinkedHashMap<String, EPCModel>();
	public final Object hmList_Lock = new Object();

	/* Sonido */

	public final Object beep_Lock = new Object();
	public ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM,
			ToneGenerator.MAX_VOLUME);

	/* Bateria */

	public static final boolean isPowerLowShow = false;//Low or not

	public static UHF CLReader = UHFReader.getUHFInstance();

	/**
	 * UHF initialization
	 *
	 * @param log Interface callback method
	 * @return Whether the initialization was successful
	 */
	public Boolean UHF_Init(IAsynchronousMessage log) {
		Boolean rt = false;
		try {
			if (_UHFSTATE == false) {
				boolean ret = UHFReader.getUHFInstance().OpenConnect(log);
				if (ret) {
					rt = true;
					_UHFSTATE = true;
				}

				Thread.sleep(500);
			} else {
				rt = true;
			}
		} catch (Exception ex) {
		}
		return rt;
	}

	/**
	 * UHF closes connection
	 */
	public void UHF_Dispose() {
		if (_UHFSTATE == true) {
			_Config.CloseConnect();
			_UHFSTATE = false;
		}
	}

	/**
	 * Acquire the read-write ability of the reader
	 */
	@SuppressLint("UseSparseArrays")
	@SuppressWarnings("serial")
	protected void UHF_GetReaderProperty() {
		//String propertyStr = CLReader.GetReaderProperty();
		String propertyStr = _Config.GetReaderProperty();
		String[] propertyArr = propertyStr.split("\\|");
		HashMap<Integer, Integer> hm_Power = new HashMap<Integer, Integer>() {
			{
				put(1, 1);
				put(2, 3);
				put(3, 7);
				put(4, 15);
			}
		};
		if (propertyArr.length > 3) {
			try {
				_Min_Power = Integer.parseInt(propertyArr[0]);
				_Max_Power = Integer.parseInt(propertyArr[1]);
				int powerIndex = Integer.parseInt(propertyArr[2]);
				_NowAntennaNo = hm_Power.get(powerIndex);
			} catch (Exception ex) {
			}
		} else {
		}
	}

	/**
	 * Set tag upload parameters
	 */
	protected void UHF_SetTagUpdateParam() {
		// First query whether the current Settings are consistent, if not before setting
		//String searchRT = CLReader.GetTagUpdateParam();
		String searchRT = _Config.GetTagUpdateParam();
		String[] arrRT = searchRT.split("\\|");
		if (arrRT.length >= 2) {
			int nowUpDataTime = Integer.parseInt(arrRT[0]);
			if (_UpDataTime != nowUpDataTime) {
				//CLReader.SetTagUpdateParam("1," + _UpDataTime); // Set the tag repeat upload time to 20ms
				_Config.SetTagUpdateParam(_UpDataTime, 0);//RSSIFilter
			} else {

			}
		} else {
		}
	}

	//Determine the backup power
	protected Boolean canUsingBackBattery() {
		if (Adapt.getPowermanagerInstance().getBackupPowerSOC() < low_power_soc) {
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	protected boolean UHF_CheckReadResult(int retval) {
		if (99 == retval) {
			showMsg(getString(R.string.uhf_read_power_over),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							UHFBaseActivity.this.finish();
						}
					});
			return false;
		}
		return true;
	}


	/**
	 * Fit the screen
	 */

	protected void ChangeLayout(Configuration newConfig) {
		LinearLayout main = (LinearLayout) findViewById(R.id.view_split_main);
		LinearLayout left = (LinearLayout) findViewById(R.id.view_split_left);
		LinearLayout right = (LinearLayout) findViewById(R.id.view_split_right);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { //The current screen is landscape
			main.setOrientation(LinearLayout.HORIZONTAL);

			LinearLayout.LayoutParams leftParam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 0.5);
			LinearLayout.LayoutParams rightParam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) 0.5);

			leftParam.setMargins(0, 0, 10, 0);
			rightParam.setMargins(10, 0, 0, 0);

			left.setLayoutParams(leftParam);
			right.setLayoutParams(rightParam);

		} else { // The current screen is vertical
			main.setOrientation(LinearLayout.VERTICAL);

			LinearLayout.LayoutParams leftParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams rightParam = (new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

			leftParam.setMargins(0, 0, 0, 0);
			rightParam.setMargins(0, 0, 0, 0);

			left.setLayoutParams(leftParam);
			right.setLayoutParams(rightParam);
		}

	}

	public void DisposeRFID() {
		CLReader.Stop();
		isStartPingPong = false;
		IsFlushList = false;
		synchronized (beep_Lock) {
			beep_Lock.notifyAll();
		}
		UHF_Dispose();
	}

	public void DisposeDB() {
		if (dataBaseHelper != null)
			dataBaseHelper.close();
	}

	public void DisposeAll() {

		DisposeRFID();
		DisposeScan();
		DisposeDB();
	}

	public String GetTipoProductoNserie(String hex) {
		String id = hex.substring(hex.length() - 17);
		String NserieIDHexa = id.substring(1, 9);
		String TipoProdIDHexa = id.substring(0, 1);

		int nserie = Integer.parseInt(NserieIDHexa, 16);
		int tp = Integer.parseInt(TipoProdIDHexa, 10);

		if (tp == 2)
			return "PU" + String.valueOf(nserie);
		else
			return "PL" + String.valueOf(nserie);

	}

	public static int getDecimal(String hex) {
		String digits = "0123456789ABCDEF";
		hex = hex.toUpperCase();
		int val = 0;
		for (int i = 0; i < hex.length(); i++) {
			char c = hex.charAt(i);
			int d = digits.indexOf(c);
			val = 16 * val + d;
		}
		return val;
	}

	public static <K, V> K getKey(Map<K, V> map, V value) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public static Map<String, Integer> filterMap(HashMap<String, Integer> hashMap, String idprod) {

		return hashMap.entrySet()
				.stream().filter(x -> String.valueOf(getDecimal(x.getKey().substring(0, 4))).equals(idprod))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	}

	public static boolean SetBaseBand(String baseBand, String QValue, String session, String flag, String tagFocus, String FastID) {
		//(0: Flag A
		// 1: Flag B
		// 2: Flag A and Flag B
		int Flag = Integer.parseInt(flag);

		// session: 0~3
		int Session = Integer.parseInt(session);

		//（0-Tari=25us, FM0, LHF=40KHz）
		// （1-TTari=25us, Miller4, LHF=250KHz）
		// （2-Tari=25us, Miller4, LHF=300KHz）
		// （3-Tari=6.25us, FM0, LHF=400KHz）
		// （4-255-Auto)
		int typePosition = Integer.parseInt(baseBand);

		if (typePosition == 4)
			typePosition = 255;
		// qValue: 0~15, the initial Q value used by the reader。
		int qValue = Integer.parseInt(QValue);
		int rt = _Config.SetEPCBaseBandParam(typePosition, qValue, Session, Flag);

//      1,00000000&//nada
//      1,00000010&//Tag focus
//      1,00000020//FastID
//      1,00000030//TagFocus & FastID

		if (Boolean.parseBoolean(tagFocus) && Boolean.parseBoolean(FastID))
			UHFReader.getUHFInstance().SetBaseBandX("1,00000030");
		if (Boolean.parseBoolean(tagFocus) && !Boolean.parseBoolean(FastID))
			UHFReader.getUHFInstance().SetBaseBandX("1,00000010");
		if (!Boolean.parseBoolean(tagFocus) && Boolean.parseBoolean(FastID))
			UHFReader.getUHFInstance().SetBaseBandX("1,00000020");
		if (!Boolean.parseBoolean(tagFocus) && !Boolean.parseBoolean(FastID))
			UHFReader.getUHFInstance().SetBaseBandX("1,00000000");

		if (rt == 0) {
			return true;
		} else {
			return false;
		}
	}

	public HashMap<String,String> GetPDAConfigDB(String module)
	{
		return dataBaseHelper.getDynamicConfigsByTypeAndName(getString(R.string.DB_DynamicConfig_Type_PDAConfig),module);
	}

	public PDASettings FillPDASettings(HashMap<String, String> pdaConfig,String metodo) {
		PDASettings pda = new PDASettings();

		String s = pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_baseband)).toLowerCase(Locale.ROOT));

		pda.baseband = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_baseband)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_baseband)).toLowerCase(Locale.ROOT)))) : null;
		pda.qValue = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_qValue)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_qValue)).toLowerCase(Locale.ROOT)))) : null;
		pda.session = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_session)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_session)).toLowerCase(Locale.ROOT)))) : null;
		pda.flag = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_flag)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_flag)).toLowerCase(Locale.ROOT)))) : null;
		pda.antCount = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_antCount)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_antCount)).toLowerCase(Locale.ROOT)))) : null;
		pda.antPower = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_antPower)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_antPower)).toLowerCase(Locale.ROOT)))) : null;
		pda.frequency = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_frequency)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_frequency)).toLowerCase(Locale.ROOT)))) : null;
		pda.repeatTimeFilter = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_repeatTimeFilter)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_repeatTimeFilter)).toLowerCase(Locale.ROOT)))) : null;
		pda.rssiFilter = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_rssiFilter)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_rssiFilter)).toLowerCase(Locale.ROOT)))) : null;
		pda.isOpen = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_isOpen)).toLowerCase(Locale.ROOT)) ? Boolean.parseBoolean((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_isOpen)).toLowerCase(Locale.ROOT)))) : null;
		pda.timeAutoIdle = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_timeAutoIdle)).toLowerCase(Locale.ROOT)) ? Integer.parseInt((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_timeAutoIdle)).toLowerCase(Locale.ROOT)))) : null;
		pda.rfu = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_rfu)).toLowerCase(Locale.ROOT)) ? pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_rfu)).toLowerCase(Locale.ROOT)).toLowerCase(Locale.ROOT) : null;
		pda.tagFocus = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_tagFocus)).toLowerCase(Locale.ROOT)) ? Boolean.parseBoolean((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_tagFocus)).toLowerCase(Locale.ROOT)))) : null;
		pda.fastID = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_fastID)).toLowerCase(Locale.ROOT)) ? Boolean.parseBoolean((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_fastID)).toLowerCase(Locale.ROOT)))) : null;
		pda.rfu2 = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_rfu2)).toLowerCase(Locale.ROOT)) ? pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_rfu2)).toLowerCase(Locale.ROOT)).toLowerCase(Locale.ROOT) : null;
		pda.maxQ = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_maxQ)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_maxQ)).toLowerCase(Locale.ROOT)))) : null;
		pda.minQ = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_minQ)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_minQ)).toLowerCase(Locale.ROOT)))) : null;
		pda.tmult = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_tmult)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_tmult)).toLowerCase(Locale.ROOT)))) : null;
		pda.DynamicStartQenable = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_DynamicStartQenable)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_DynamicStartQenable)).toLowerCase(Locale.ROOT)))) : null;
		pda.antenna = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_antenna)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_antenna)).toLowerCase(Locale.ROOT)))) : null;
		pda.numberOfRetries = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_numberOfRetries)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_numberOfRetries)).toLowerCase(Locale.ROOT)))) : null;
		pda.maxAntennaResistancetime = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_maxAntennaResistancetime)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_maxAntennaResistancetime)).toLowerCase(Locale.ROOT)))) : null;
		pda.waitingTimeAntSwitch = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_waitingTimeAntSwitch)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_waitingTimeAntSwitch)).toLowerCase(Locale.ROOT)))) : null;
		pda.antennaSwitchingSequence = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_antennaSwitchingSequence)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_antennaSwitchingSequence)).toLowerCase(Locale.ROOT)))) : null;
		pda.antennaProtectionThreshold = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_antennaProtectionThreshold)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_antennaProtectionThreshold)).toLowerCase(Locale.ROOT)))) : null;
		pda.LBTMode = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_LBTMode)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_LBTMode)).toLowerCase(Locale.ROOT)))) : null;
		pda.RSSIMaxVal = pdaConfig.containsKey((metodo + getString(R.string.DB_DynamicConfig_Name_RSSIMaxVal)).toLowerCase(Locale.ROOT)) ? ((pdaConfig.get((metodo + getString(R.string.DB_DynamicConfig_Name_RSSIMaxVal)).toLowerCase(Locale.ROOT)))) : null;

		return pda;
	}

	public void SetPDAConfigurations(PDASettings pda) {
		if(true) {
			if (pda.frequency != _Config.GetFrequency()) {
				try {

					_Config.SetFrequency(pda.frequency);
					Thread.sleep(500);

				} catch (Exception ex) {
				}
			}

			if (pda.antPower != _Config.GetANTPowerParam()) {
				try {

					_Config.SetANTPowerParam(pda.antCount, pda.antPower);
					Thread.sleep(500);
				} catch (Exception ex) {
				}

			}

			String baseband = pda.baseband + "|" + pda.qValue + "|" + pda.session + "|" + pda.flag;

			if (!baseband.equals(_Config.GetEPCBaseBandParam())) {
				try {

					_Config.SetEPCBaseBandParam(pda.baseband, pda.qValue, pda.session, pda.flag);
					Thread.sleep(500);
				} catch (Exception ex) {
				}
			}

			String tagUpdate = pda.repeatTimeFilter + "|" + pda.rssiFilter;

			if (!tagUpdate.equals(_Config.GetTagUpdateParam())) {
				try {
					_Config.SetTagUpdateParam(pda.repeatTimeFilter, pda.rssiFilter);
					Thread.sleep(500);
				} catch (Exception ex) {
				}
			}


			String autoSleep = (pda.isOpen) ? "1" : "0" + "|" + pda.timeAutoIdle;
			String trsaa = UHFReader._Config.GetReaderAutoSleepParam();
			if (!autoSleep.equals(trsaa)) {
				try {

					UHFReader._Config.SetReaderAutoSleepParam(pda.isOpen, pda.timeAutoIdle);
					Thread.sleep(500);

				} catch (Exception ex) {
				}
			}

			String basex = UHFReader.getUHFInstance().GetBaseBandX();
			String[] basexParams = basex.split("&");

			String basex1 = getString(R.string.RFID_BaseBandX1) + pda.rfu;

//      1,00000000&//nada
//      1,00000010&//Tag focus
//      1,00000020//FastID
//      1,00000030//TagFocus & FastID

			if (!pda.tagFocus && !pda.fastID)
				basex1 = basex1 + "0";
			if (pda.tagFocus && !pda.fastID)
				basex1 = basex1 + "1";
			if (!pda.tagFocus && pda.fastID)
				basex1 = basex1 + "2";
			if (pda.tagFocus && pda.fastID)
				basex1 = basex1 + "3";

			basex1 = basex1 + pda.rfu2;


			//BaseBandx 1
			if (!basexParams[0].equals(basex1)) {
				try {

					UHFReader.getUHFInstance().SetBaseBandX(basex1);
					Thread.sleep(500);

				} catch (Exception ex) {
				}
			}

			String basex2 = getString(R.string.RFID_BaseBandX2) + pda.maxQ + pda.minQ + pda.tmult + pda.DynamicStartQenable;

			//BaseBandx 2
			if (!basexParams[1].equals(basex2)) {
				try {

					UHFReader.getUHFInstance().SetBaseBandX(basex2);
					Thread.sleep(500);

				} catch (Exception ex) {
				}

			}

			String basex3 = getString(R.string.RFID_BaseBandX3) + pda.antenna + pda.numberOfRetries + pda.maxAntennaResistancetime;

			//BaseBandx 3
			if (!basexParams[2].equals(basex3)) {
				try {

					UHFReader.getUHFInstance().SetBaseBandX(basex3);
					Thread.sleep(500);

				} catch (Exception ex) {
				}

			}

			String basex4 = getString(R.string.RFID_BaseBandX4) + pda.waitingTimeAntSwitch + pda.antennaSwitchingSequence + pda.antennaProtectionThreshold + "00";

			//BaseBandx 4
			if (!basexParams[3].equals(basex4)) {
				try {

					UHFReader.getUHFInstance().SetBaseBandX(basex4);
					Thread.sleep(500);

				} catch (Exception ex) {
				}

			}

			String basex5 = getString(R.string.RFID_BaseBandX5) + pda.LBTMode + pda.RSSIMaxVal + "0000";

			//BaseBandx 5
			if (!basexParams[4].equals(basex5)) {
				try {

					UHFReader.getUHFInstance().SetBaseBandX(basex5);
					Thread.sleep(500);

				} catch (Exception ex) {
				}

			}
		}
	}

	protected void InitRFIDModule(IAsynchronousMessage log,String module) {

		showWait("Configurando Antena");
		if (!UHF_Init(log)) { // Failed to power on the module
			showMsg(getString(R.string.RFID_ErrorConexion),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
						}
					});
		}else
			ConfigureRFIDModule(module);

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

	}

	protected void InitDBConexion() {

		dataBaseHelper = new DataBaseHelper(this);

		BaseUrlApi = dataBaseHelper.getDynamicConfigsData(getString(R.string.API_ENDPOINT));

		GetOperarioDB();


	}

	protected void ConfigureRFIDModule(String module){
		try {

			PDASettings pda = FillPDASettings(GetPDAConfigDB(module),module);
			SetPDAConfigurations(pda);
			hideWait();

		} catch (Exception ee) {
			hideWait();
			showMsg(getString(R.string.RFID_ErrorCambioPotencia));
		}


	}

	public void GetOperarioDB(){

		_Operario = new Operario();

		_Operario.setLogeo(dataBaseHelper.getDynamicConfigsData(getString(R.string.OPERARIOLOG)));
		_Operario.setDescripcion(dataBaseHelper.getDynamicConfigsData(getString(R.string.OPERARIODESC)));;
		_Operario.setHabilitaUsoTracker(dataBaseHelper.getDynamicConfigsData(getString(R.string.OPERARIOUSOTRACKER)));
		_Operario.setPlanta(dataBaseHelper.getDynamicConfigsData(getString(R.string.OPERARIOPLANTA)));

	}

	@Override
	public void OutPutEPC(EPCModel epcModel) {

	}

	@Override
	protected void onResume() {
		super.onResume();
		InitDBConexion();

		String Classname=this.getClass().getSimpleName();
		Classname = Classname.substring(0,Classname.length()-8);

		InitRFIDModule(this, Classname);
		InitBCModule();
		InitSoundModule();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (TriggerPressed(keyCode)) { // Press the handle button
			StartReading();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (TriggerPressed(keyCode) && isStartPingPong) {
			StopReading();
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		Back(null);
	}

	public void Back(View v) {

		CLReader.Stop();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		DisposeAll();

		finish();

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

	public void StartReading() {

	}

	public void StartReadingEPC() {

		int ret = -1;
		in_reading = true;

		ret = UHFReader._Tag6C.GetEPC(_NowAntennaNo, _RFIDSingleRead);

	}


	public void StopReading() {

		isStartPingPong = false;
		CLReader.Stop();

	}

	public void Clear(View v) {
		hmList.clear();
	}

}
