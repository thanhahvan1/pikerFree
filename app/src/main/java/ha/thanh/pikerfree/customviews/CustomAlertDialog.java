package ha.thanh.pikerfree.customviews;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import ha.thanh.pikerfree.R;


/**
 * Created by HaVan on 6/1/2017.
 */

public class CustomAlertDialog {
    private Dialog alertDialog;
    private AlertListener listener;

    public CustomAlertDialog(Activity activity) {
        alertDialog = new Dialog(activity);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setCancelable(false);
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setContentView(R.layout.view_alert_dialog);
    }

    public void showAlertDialog(String title, String message) {

        TextView tvTitle = alertDialog.findViewById(R.id.tv_title);
        tvTitle.setText(title);

        TextView tvContent = alertDialog.findViewById(R.id.tv_content);
        tvContent.setText(message);

        TextView tvCancel = alertDialog.findViewById(R.id.btn_ok_dialog);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (listener != null)
                    listener.onOkClicked();
            }
        });

        try {
            alertDialog.show();
        } catch (Exception e) {

        }

    }

    public void setListener(AlertListener listener) {
        this.listener = listener;
    }

    public interface AlertListener {
        void onOkClicked();
    }
}
