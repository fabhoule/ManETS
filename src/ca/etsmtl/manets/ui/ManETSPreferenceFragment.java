package ca.etsmtl.manets.ui;


import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import ca.etsmtl.manets.R;

public class ManETSPreferenceFragment extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        intent.setClass(this, ManETSPreferenceFragment.class);
        startActivityForResult(intent, 0);
        return true;
    }
}

