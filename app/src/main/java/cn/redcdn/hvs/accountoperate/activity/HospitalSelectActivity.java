package cn.redcdn.hvs.accountoperate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.adapter.HospitalSelectAdapter;
import cn.redcdn.hvs.accountoperate.info.Province;

public class HospitalSelectActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<Province> arrayList;
    private HospitalSelectAdapter adapter;
    private Province pro1;
    private Province pro2;
    private Province pro3;
    private EditText hospitaledit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_select);
        listView = (ListView) findViewById(R.id.hospital_list);
        hospitaledit = (EditText) findViewById(R.id.hospital_select_edit);
        arrayList = new ArrayList<Province>();
        init();
        adapter = new HospitalSelectAdapter(arrayList, HospitalSelectActivity.this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), arrayList.get(i).getHospitalprovince(), Toast.LENGTH_SHORT).show();
                arrayList.clear();
                initcity();
                adapter = new HospitalSelectAdapter(arrayList, HospitalSelectActivity.this);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Toast.makeText(getApplicationContext(), arrayList.get(i).getHospitalprovince(), Toast.LENGTH_SHORT).show();
                        arrayList.clear();
                        inithospital();
                        adapter = new HospitalSelectAdapter(arrayList, HospitalSelectActivity.this);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }

    private void init() {
        pro1 = new Province();
        pro2 = new Province();
        pro3 = new Province();
        pro1.setHospitalprovince("zhejiang");
        pro2.setHospitalprovince("beijing");
        pro3.setHospitalprovince("jiangshu");
        arrayList.add(pro1);
        arrayList.add(pro2);
        arrayList.add(pro3);

    }

    private void initcity() {
        pro1.setHospitalprovince("杭州");
        pro2.setHospitalprovince("嘉兴");
        pro3.setHospitalprovince("衢州");
        arrayList.add(pro1);
        arrayList.add(pro2);
        arrayList.add(pro3);

    }

    private void inithospital() {
//        pro1 = new Province();
//        pro2 = new Province();
//        pro3 = new Province();
        pro1.setHospitalprovince("hospital_one");
        pro2.setHospitalprovince("hospital_two");
        pro3.setHospitalprovince("hospital_there");
        arrayList.add(pro1);
        arrayList.add(pro2);
        arrayList.add(pro3);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intentback =new Intent(HospitalSelectActivity.this,DoctorActivity.class);
        startActivity(intentback);
    }
}

