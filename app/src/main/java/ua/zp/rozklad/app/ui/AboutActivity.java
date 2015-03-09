package ua.zp.rozklad.app.ui;

import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ua.zp.rozklad.app.BuildConfig;
import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.rest.RESTMethod;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.app_bar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            findViewById(R.id.app_bar_shadow).setVisibility(View.GONE);
            getSupportActionBar()
                    .setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
        }

        /*
        * Social
        * */
        ((TextView) findViewById(R.id.social).findViewById(R.id.text))
                .setText(R.string.social);

        TextView joinUsVk = (TextView) findViewById(R.id.join_vk);
        joinUsVk.setText(R.string.join_us_vk);
        joinUsVk.setOnClickListener(this);
        
        
        /*
        * We
        * */
        ((TextView) findViewById(R.id.we).findViewById(R.id.text))
                .setText(R.string.we);

        View vojkoVolodymyr = findViewById(R.id.vojko_volodymyr);
        ((ImageView) vojkoVolodymyr.findViewById(R.id.icon))
                .setImageResource(R.drawable.pic_vojko_volodymyr_40dp);
        ((TextView) vojkoVolodymyr.findViewById(R.id.primary_text))
                .setText(R.string.vojko_volodymyr);
        ((TextView) vojkoVolodymyr.findViewById(R.id.secondary_text))
                .setText(R.string.android_app_dev);
        vojkoVolodymyr.setOnClickListener(this);

        View ambroskinMaksym = findViewById(R.id.ambroskin_maksym);
        ((ImageView) ambroskinMaksym.findViewById(R.id.icon))
                .setImageResource(R.drawable.pic_ambroskin_maksym_40dp);
        ((TextView) ambroskinMaksym.findViewById(R.id.primary_text))
                .setText(R.string.ambroskin_maksym);
        ((TextView) ambroskinMaksym.findViewById(R.id.secondary_text))
                .setText(R.string.android_app_dev);
        ambroskinMaksym.setOnClickListener(this);

        View klymenkoVadym = findViewById(R.id.klymenko_vadym);
        ((ImageView) klymenkoVadym.findViewById(R.id.icon))
                .setImageResource(R.drawable.pic_klymenko_vadym_40dp);
        ((TextView) klymenkoVadym.findViewById(R.id.primary_text))
                .setText(R.string.klymenko_vadym);
        ((TextView) klymenkoVadym.findViewById(R.id.secondary_text))
                .setText(R.string.website_dev);
        klymenkoVadym.setOnClickListener(this);

        /*
        * Additionally
        * */
        ((TextView) findViewById(R.id.additionally).findViewById(R.id.text))
                .setText(R.string.additionally);

        TextView scheduleSite = (TextView) findViewById(R.id.schedule_site);
        scheduleSite.setText(R.string.site_with_schedule);
        scheduleSite.setOnClickListener(this);

        TextView writeToUs = (TextView) findViewById(R.id.write_to_us);
        writeToUs.setText(R.string.write_to_us);
        writeToUs.setOnClickListener(this);

        TextView leaveComment = (TextView) findViewById(R.id.rate);
        leaveComment.setText(R.string.rate);
        leaveComment.setOnClickListener(this);

        /*
        * Version
        * */
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(getString(R.string.app_version) + " " + BuildConfig.VERSION_NAME);
        version.setGravity(Gravity.CENTER);
        version.setClickable(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.animator.activity_open_alpha,
                R.animator.activity_close_translate_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.schedule_site: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(RESTMethod.SITE_URL));
                startActivity(intent);
            }
            break;
            case R.id.join_vk: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.vk_group_url)));
                startActivity(intent);
            }
            break;
            case R.id.vojko_volodymyr: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.vojko_volodymyr_vk_url)));
                startActivity(intent);
            }
            break;
            case R.id.ambroskin_maksym: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.ambroskin_maksym_vk_url)));
                startActivity(intent);
            }
            break;
            case R.id.klymenko_vadym: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.klymenko_vadym_vk_url)));
                startActivity(intent);
            }
            break;
            case R.id.write_to_us: {
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + getString(R.string.vojko_volodymyr_email))
                );
                intent.putExtra("subject", getString(R.string.write_to_us_subject));
                try {
                    startActivity(
                            Intent.createChooser(intent, getString(R.string.write_to_us_doted))
                    );
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, R.string.no_email_clients, Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.rate: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    intent.setData(Uri.parse(
                            "http://play.google.com/store/apps/details?id=" + getPackageName()
                    ));
                    startActivity(intent);
                }
            }
            break;
        }
    }

    @Override
    protected void onAccountDeleted() {
        MainActivity.startClearTask(this);
    }
}
