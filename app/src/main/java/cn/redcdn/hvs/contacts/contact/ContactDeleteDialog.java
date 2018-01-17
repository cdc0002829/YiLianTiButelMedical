package cn.redcdn.hvs.contacts.contact;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import cn.redcdn.hvs.R;

public class ContactDeleteDialog extends Dialog {
	private OkClickListener okListener = null;
	private NoClickListener noListener = null;
	private Button cancelBtn = null;
	private Button sureBtn = null;

	public ContactDeleteDialog(Context context) {
		super(context, R.style.dialog);

	}

	public interface OkClickListener {
		public void clickListener();
	}
	 public ContactDeleteDialog(Context context, int theme) {
		    super(context, theme);
		  }
	public interface NoClickListener {
		public void clickListener();
	}

	public void setOkClickListener(OkClickListener ok) {
		this.okListener = ok;
	}

	public void setNoClickListener(NoClickListener no) {
		this.noListener = no;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.contactdeletedialog);
		sureBtn = (Button) this.findViewById(R.id.contact_del_btn);
		cancelBtn = (Button) this.findViewById(R.id.contact_cancel_btn);
		cancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (noListener != null)
					noListener.clickListener();

			}
		});
		sureBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (okListener != null)
					okListener.clickListener();
			}
		});
	}

}
