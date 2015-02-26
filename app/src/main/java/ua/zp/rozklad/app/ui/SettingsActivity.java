package ua.zp.rozklad.app.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import ua.zp.rozklad.app.R;

/**
 * Created by kkxmshu on 18.02.15.
 */
public class SettingsActivity extends PreferenceActivity {

    private Toolbar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.app_bar, root, false);
        root.addView(bar, 0);
        bar.setTitle(getResources().getString(R.string.general_settings));
        bar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        bar.setBackgroundColor(getResources().getColor(R.color.blue_grey_500));
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers, target);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if(header.id == R.id.clear_cache) {
            new MaterialDialog.Builder(this)
                    .content(R.string.change_group_question)
                    .positiveText(R.string.positive_answer)
                    .negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
//                            TODO: Add clearCache() method
                        }
                    }).show();
        }
    }
}
