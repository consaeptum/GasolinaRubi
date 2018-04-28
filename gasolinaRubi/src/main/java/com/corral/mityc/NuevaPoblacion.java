package com.corral.mityc;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.corral.mityc.servicios.WSJsonGetMunicipiosPorProvincia;
import com.corral.mityc.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.corral.mityc.Parseo.buscarCodigoProvincia;

//import static com.corral.mityc.Parseo.buscarCodigoPoblacion;

public class NuevaPoblacion extends AppCompatActivity  {

    private static final String TAG = NuevaPoblacion.class.getSimpleName();

    public static final String RESULTADO = "poblacion";
    private static ProgressDialog progressBar;
    private static String provincia = "";
    ExpandableListAdapter listAdapter;
    ExpandableListView menuProvincias;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    String poblacion = "";
    ResultReceiver rr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nueva_poblacion);

        menuProvincias = (ExpandableListView) findViewById(R.id.provinciaListView);
        provincia = "";

        preparaDatos();

        listAdapter = new MyExpandableListAdapter(this, listDataHeader, listDataChild);
        menuProvincias.setAdapter(listAdapter);

        // Listview Group click listener
        menuProvincias.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                if (listDataChild.get(listDataHeader.get(groupPosition)).isEmpty()) {
                    preparaDatosGrupo(groupPosition);
                    return true;
                } else {
                    return false;
                }
            }
        });


        // Listview Group expanded listener
        menuProvincias.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });

        // Listview Group collasped listener
        menuProvincias.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });

        // Listview on child click listener
        menuProvincias.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                provincia = listDataHeader.get(groupPosition);
                String cprov = buscarCodigoProvincia(provincia);
                poblacion = listDataChild.get(provincia).get(childPosition);
                WSJsonGetMunicipiosPorProvincia.obtenMunicipio(rr, cprov, poblacion);
                return true;
            }
        });

        //
        // cuando pidamos el código de la población a WSJsonGetMunicipiosPorProvincia, lo trataremos aquí.
        //
        rr = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                Intent intent = new Intent();
                if (resultCode == Constantes.SUCCESS_RESULT) {
                    String mCodigoMitycPoblacionResultado = resultData.getString(Constantes.RESULT_DATA_KEY);
                    // Enviarmos un string con codigo#nombrePoblación a MitycRubi onActivityResult
                    intent.putExtra(RESULTADO, mCodigoMitycPoblacionResultado.concat("#").concat(poblacion));
                    setResult(RESULT_OK, intent);
                    MitycRubi.PROV_DRAWERLIST = provincia;
                    MitycRubi.cambioPoblacion = true;
                    finish();

                } else {
                    // Enviarmos un string con codigo#nombrePoblación a MitycRubi onActivityResult
                    intent.putExtra(RESULTADO, "" );
                    setResult(Constantes.FAILURE_RESULT, intent);
                    finish();
                }

            }
        };

    }

    @Override
    public void onBackPressed() {
        // Enviarmos un string con codigo#nombrePoblación a MitycRubi onActivityResult
        Intent intent = new Intent();
        intent.putExtra(RESULTADO, "" );
        setResult(Constantes.FAILURE_RESULT, intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportActionBar().hide();
    }
    @Override
    public void onStop() {
        super.onStop();
    getSupportActionBar().show();
    }

    private void preparaDatos() {

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        for (String[] p : Constantes.codigosProvincia) {
            listDataHeader.add(p[0]);
            listDataChild.put(p[0], new ArrayList<String>());
        }
    }

    private void preparaDatosGrupo(final int position) {

        final String cpprov = Constantes.codigosProvincia[position][1];

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Cargando datos de Mityc ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        @SuppressLint("StaticFieldLeak") AsyncTask<String, Void, String> mTask = new AsyncTask<String, Void, String>() {

            protected String doInBackground(String... urls) {
                String res;
                try {
                    res = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildUrlMuniciposPorProvincia(cpprov));
                } catch (IOException e) {
                    res = null;
                }
                return res;
            }

            protected void onPostExecute(String result) {
                if (result != null) {
                    HashMap<String, String> codMitycPobs = cargaMunicipios(result);
                    ArrayList<String> pbs = new ArrayList<String>(codMitycPobs.values());
                    Collections.sort(pbs);
                    listDataChild.put(listDataHeader.get(position), pbs);
                    progressBar.hide();
                    menuProvincias.expandGroup(position);
                } else {
                    progressBar.hide();
                    Intent intent = new Intent();
                    intent.putExtra(RESULTADO, "" );
                    setResult(Constantes.FAILURE_RESULT, intent);
                    finish();
                }
            }


            /*
             * Carga la lista de Municipios para el código de provincia dado y lo devuelve
             * en un HashMap<codigo_poblacion_de_mityc, nombre_población>
             */
            HashMap<String, String> cargaMunicipios(String result) {

                HashMap<String, String> hm = new HashMap<String, String>();

                try {
                    JSONArray jsonArray = new JSONArray(result);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject value = jsonArray.getJSONObject(i);
                        hm.put(value.getString("IDMunicipio"), value.getString("Municipio"));
                    }

                } catch (Exception e) {
                    return null;
                }
                return hm;
            }
        };
        mTask.execute();



    }



    /***
     * El adapter para ExpandableListView
     */
    class MyExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<String>> _listDataChild;

        public MyExpandableListAdapter(Context context, List<String> listDataHeader,
                                       HashMap<String, List<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.nueva_poblacion_list_item, null);
            }

            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.lblListItem);

            txtListChild.setText(childText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.nueva_poblacion_list_group, null);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}