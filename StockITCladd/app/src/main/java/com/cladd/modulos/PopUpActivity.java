package com.cladd.modulos;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cladd.entities.model.Pieza;
import com.cladd.services.DataBaseHelper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.hopeland.pda.example.R;
import com.util.BaseActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PopUpActivity extends BaseActivity {

    /**
     * INICIO Definicion de elementos de la vista
     **/
    private RelativeLayout bg_PopUpError = null;
    private Button btnCerrar = null;
    private TextView tv_PopUp_TipoError=null;
    private TextView btn_PopUp_cerrar=null;
    private TextView tv_PopUpError_Tipo=null;
    private TextView tv_PopUpError_Pieza=null;
    private TextView tv_PopUpError_DepSec=null;
    private TextView tv_PopUpError_Fecha=null;
    private TextView tv_PopUpError_Partida=null;
    private TextView tv_PopUpError_Partida2=null;
    private TextView tv_PopUpError_Articulo=null;
    private TextView tv_PopUpError_Color=null;
    private TextView tv_PopUpexito_Tipo=null;
    private TextView tv_PopUpexito_Pieza=null;
    private TextView tv_PopUpexito_DepSec=null;
    private TextView tv_PopUpexito_Fecha=null;
    private TextView tv_PopUpexito_Partida=null;
    private TextView tv_PopUpexito_Partida2=null;
    private TextView tv_PopUpexito_Articulo=null;
    private TextView tv_PopUpexito_Color=null;

    /** FIN Definicion de elementos de la vista **/

    /**
     * Inicio Definicion Variables Controller
     **/

    private String hex=new String();
    private String title=new String();
    private boolean exito =false;
    Pieza productoGlobal=new Pieza();

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

        hex = getIntent().getStringExtra(getString(R.string.tv_PopUpError_pieza));
        title = getIntent().getStringExtra(getString(R.string.tv_PopUpError_TipoError));
        exito = getIntent().getBooleanExtra("exito", false);

        InitDBConexion();
        InitView();

    }

    protected void InitDBConexion() {

        dataBaseHelper = new DataBaseHelper(PopUpActivity.this);

        BaseUrlApi = dataBaseHelper.getDynamicConfigsData(getString(R.string.API_ENDPOINT));



    }

    protected void InitView() {

        if (!exito) {
            setContentView(R.layout.popup_error);
        } else {
            setContentView(R.layout.popup_exito);
        }

        BindViews();
    }

    protected void BindViews(){
        bg_PopUpError = findViewById(R.id.bg_PopUpError);
        tv_PopUp_TipoError= findViewById(R.id.tv_PopUp_TipoError);
        btn_PopUp_cerrar = findViewById(R.id.btn_PopUp_cerrar);
        tv_PopUpexito_Tipo = findViewById(R.id.tv_PopUpexito_Tipo);
        tv_PopUpexito_Pieza = findViewById(R.id.tv_PopUpexito_Pieza);
        tv_PopUpexito_DepSec = findViewById(R.id.tv_PopUpexito_DepSec);
        tv_PopUpexito_Fecha = findViewById(R.id.tv_PopUpexito_Fecha);
        tv_PopUpexito_Partida = findViewById(R.id.tv_PopUpexito_Partida);
        tv_PopUpexito_Partida2 = findViewById(R.id.tv_PopUpexito_Partida_2);
        tv_PopUpexito_Articulo = findViewById(R.id.tv_PopUpexito_Articulo);
        tv_PopUpexito_Color = findViewById(R.id.tv_PopUpexito_Color);

        tv_PopUpError_Tipo = findViewById(R.id.tv_PopUpError_Tipo);
        tv_PopUpError_Pieza = findViewById(R.id.tv_PopUpError_Pieza);
        tv_PopUpError_DepSec = findViewById(R.id.tv_PopUpError_DepSec);
        tv_PopUpError_Fecha = findViewById(R.id.tv_PopUpError_Fecha);
        tv_PopUpError_Partida = findViewById(R.id.tv_PopUpError_Partida);
        tv_PopUpError_Partida2 = findViewById(R.id.tv_PopUpError_Partida_2);
        tv_PopUpError_Articulo = findViewById(R.id.tv_PopUpError_Articulo);
        tv_PopUpError_Color = findViewById(R.id.tv_PopUpError_Color);

        btnCerrar = findViewById(R.id.btnCerrarPopUp);


        InitListeners();


    }

    protected void InitListeners() {

        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpActivity.this.finish();
            }
        });
        btn_PopUp_cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpActivity.this.finish();
            }
        });

        ConstruirPopUp();
    }

    /**
     * API
     *
     * @return
     */

    protected void GetPieza(String hex) {

        showWait(getString(R.string.waiting));

        List<String> reqKeys = GetReqKeysList();
        List<Object> reqValues = GetReqValuesList(hex);

        Call<JsonElement> call = GetApiService(BaseUrlApi).ConsultarPieza(CreateReqBodyAndLogToServer(reqKeys,reqValues,getString(R.string.API_PopUp_Nombre)));

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                try {
                    if (response.isSuccessful()) {

                        JSONObject resp = new JSONObject(response.body().toString());

                        LogToExternalServer(getString(R.string.str_PDA),UNIQUEIDPDA,getString(R.string.API_PopUp_Nombre),2, Calendar.getInstance().toString(),resp.toString());

                        if (IsAPIResponseStateOK(resp,getString(R.string.API_PopUp_Nombre))) {

                            productoGlobal = new Gson().fromJson(resp.getString(getString(R.string.API_DATASOURCE)), new TypeToken<Pieza>() {}.getType());;

                            SetData(productoGlobal);

                        }

                    } else {
                        showMsg(getString(R.string.error_PopUp_ApiDevolvioRaro), null);
                    }
                } catch (Exception ex) {
                    showMsg("Error se leyo:" + hex + getString(R.string.str_br) + ex.getMessage().toString(), null);
                }
                hideWait();
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                hideWait();
                showMsg(t.getMessage(), null);
                LogToExternalServer(getString(R.string.str_PDA),UNIQUEIDPDA,getString(R.string.API_PopUp_Nombre),4, Calendar.getInstance().toString(),t.getMessage());
            }
        });

    }

    protected List<String> GetReqKeysList(){

        List<String> list= new ArrayList<>();

        list.add(getString(R.string.API_PopUp_Req_ID));

        return list;

    }

    protected List<Object> GetReqValuesList(String hex){

        List<Object> list= new ArrayList<>();

        list.add(hex);

        return list;

    }

    /**
     * End API
     */

    protected void ConstruirPopUp(){
        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.9),(int)(height*.63));

        WindowManager.LayoutParams params=getWindow().getAttributes();
        params.gravity= Gravity.CENTER;
        params.x=0;
        params.y=-20;

        getWindow().setAttributes(params);

        GetPieza(hex);

    }

    protected void SetData(Pieza p) {
        if (!productoGlobal.getnSerie().equals(new String())) {
            if (!exito) {
                if(title.equals(getString(R.string.tv_PopUpError_TitleNoTrackeada)))
                {
                    tv_PopUp_TipoError.setText(getString(R.string.tv_PopUpError_TitleNoTrackeada));
                    bg_PopUpError.setBackgroundColor(Color.rgb(255, 255, 0));
                }

                if(title.equals(getString(R.string.tv_PopUpError_TitleNoAsignada)))
                    tv_PopUp_TipoError.setText(getString(R.string.tv_PopUpError_TitleNoAsignada));

                tv_PopUpError_Tipo.setText(p.getTipoProducto());
                tv_PopUpError_Pieza.setText(p.getnSerie());
                tv_PopUpError_DepSec.setText(p.getDeposito() + "/" + productoGlobal.getSector());
                tv_PopUpError_Fecha.setText(p.getFecha());
                tv_PopUpError_Partida.setText(p.getPartida().toString());
                tv_PopUpError_Partida2.setText("-1");
                tv_PopUpError_Articulo.setText(p.getDescArticulo());
                tv_PopUpError_Color.setText(p.getDescColor());

            } else {

                tv_PopUpexito_Tipo.setText(p.getTipoProducto());
                tv_PopUpexito_Pieza.setText(p.getnSerie());
                tv_PopUpexito_DepSec.setText(p.getDeposito() + "/" + productoGlobal.getSector());
                tv_PopUpexito_Fecha.setText(p.getFecha());
                tv_PopUpexito_Partida.setText(p.getPartida().toString());
                tv_PopUpexito_Partida2.setText("-1");
                tv_PopUpexito_Articulo.setText(p.getDescArticulo());
                tv_PopUpexito_Color.setText(p.getDescColor());

            }
        } else
            PopUpActivity.this.finish();
        hideWait();
    }

    /**
 * End Controller
 */ // Controller

}
