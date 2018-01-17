package cn.redcdn.hvs.contacts.contact.hpucontact;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.ContactCardActivity;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.util.TitleBar;

public class ShareContactActivity extends BaseActivity {

    private ListView ScListView = null;
    public static final String HPU_ID = "id";
    public static final String HPU_NAME = "name";
    public List<Contact> contacts = new ArrayList<>();
    private  String titleName;
    private Button backBtn;
    private TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_contact);
        ScListView = (ListView) findViewById(R.id.shareContactList);
        titleName = getIntent().getStringExtra(HPU_NAME);

        initTitleBar();
        initData();
  }

  private void initData(){
      String hpuId = getIntent().getStringExtra(HPU_ID);

      contacts = ContactManager.getInstance(ShareContactActivity.this).getContactsBydtId(hpuId);
      HpuAdapter adapter = new HpuAdapter(ShareContactActivity.this,R.layout.hpu_contact_layout,contacts);
      ScListView.setAdapter(adapter);
      ScListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
              Intent intent = new Intent();
              intent.setClass(ShareContactActivity.this, ContactCardActivity.class);
              intent.putExtra("hpuContact",contacts.get(i));
              startActivity(intent);
          }
      });
  }

  private void initTitleBar(){
      backBtn = (Button)findViewById(R.id.btn_back);
      title = (TextView) findViewById(R.id.tvtitle);
      backBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              finish();
          }
      });
      title.setText(titleName);
  }
}
