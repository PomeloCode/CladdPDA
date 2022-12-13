package com.cladd.services;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.cladd.entities.api.AppFileData;
import com.hopeland.pda.example.R;
import com.util.BaseActivity;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class UpdateAppService  extends BaseActivity {

    private Context _context = null;
    private String apkFileUrl = null;

    public void DoUpdate(Context applicationContext, String baseUrlApi) {
        _context = applicationContext;
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrlApi)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface json = retrofit.create(ApiInterface.class);

        Call<AppFileData> call = json.GetDataUpdateApp("extrakey", "extravalue");

        call.enqueue(new Callback<AppFileData>() {
            @Override
            public void onResponse(Call<AppFileData> call, Response<AppFileData> response) {
                if (!response.isSuccessful())
                    return;
                AppFileData server = response.body();
                AppFileData local = GetLocalAppInfo();
                dataBaseHelper= new DataBaseHelper(_context);
                apkFileUrl = dataBaseHelper.getDynamicConfigsData(_context.getString(R.string.DB_DynamicConfig_Name_UpdateAppUrl));
                if (!(server.getVersionName().equals(local.getVersionName()) && server.getVersionCode() == local.getVersionCode())) {
                    ShowDownloadAppDialog();
                }else
                    ShowNoUpdate();
            }

            @Override
            public void onFailure(Call<AppFileData> call, Throwable t) {
                Log.d("",t.getMessage());
            }
        });
    }

    private void ShowDownloadAppDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this._context)
                .setTitle("Nueva Version")
                .setMessage("Hay una nueva version de la app, por favor actualizar")
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        redirectStore(apkFileUrl);
                    }
                })
                .setCancelable(false).create();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataBaseHelper = new DataBaseHelper(UpdateAppService.this);
    }
    private void ShowNoUpdate() {
        AlertDialog dialog = new AlertDialog.Builder(this._context)
                .setTitle("Todo Ok!")
                .setMessage("No hay actualizaciones nuevas")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(true).create();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();

            }
        });
    }

    private void redirectStore(String apkFileUrl) {
        final Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(apkFileUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this._context.startActivity(intent);
    }

    private AppFileData GetLocalAppInfo() {
        AppFileData ret = new AppFileData();

        final PackageManager pm = this._context.getPackageManager();
        final String strPM = this._context.getPackageName();

        try{
            PackageInfo pi = pm.getPackageInfo(strPM,0);
            ret.setVersionName(pi.versionName);
            ret.setVersionCode(pi.versionCode);
        }
        catch (Exception ex)
        {

        }

        return ret;
    }
}
