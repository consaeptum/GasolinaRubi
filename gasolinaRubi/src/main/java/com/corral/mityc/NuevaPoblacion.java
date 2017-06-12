package com.corral.mityc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.corral.mityc.Parseo.*;

public class NuevaPoblacion extends AppCompatActivity {

    public static final String RESULTADO = "poblacion";
    private static final float SWIPE_MIN_DISTANCE = 100;
    ExpandableListAdapter listAdapter;
    ExpandableListView menuProvincias;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nueva_poblacion);

        menuProvincias = (ExpandableListView) findViewById(R.id.provinciaListView);

        preparaDatos();

        listAdapter = new MyExpandableListAdapter(this, listDataHeader, listDataChild);
        menuProvincias.setAdapter(listAdapter);

        // Listview Group click listener
        menuProvincias.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                preparaDatosGrupo(groupPosition);
                return false;
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
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                String prov = listDataHeader.get(groupPosition);
                String cprov = buscarCodigoProvincia(prov);
                if (cprov.startsWith("0")) cprov = cprov.substring(1);
                String pobl = listDataChild.get(prov).get(childPosition);
                String cpob = null;
                try {
                    cpob = buscarCodigoPoblacion(cprov, pobl);
                } catch (RegistroNoExistente rne) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                intent.putExtra(RESULTADO, cpob);
                setResult(RESULT_OK,intent);
                finish();
                return false;
            }
        });
    }

    private void preparaDatos() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        for (String[] p : Constantes.codigosProvincia) {
            listDataHeader.add(p[0]);
            listDataChild.put(p[0], new ArrayList<String>());
        }
    }

    private void preparaDatosGrupo(int position) {
        listDataChild.put(listDataHeader.get(position),
                listaNomPobXProv(buscarCodigoProvincia(listDataHeader.get(position))));
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