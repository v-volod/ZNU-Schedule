package ua.pp.rozkladznu.app.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;

import ua.pp.rozkladznu.app.App;
import ua.pp.rozkladznu.app.BuildConfig;
import ua.pp.rozkladznu.app.R;
import ua.pp.rozkladznu.app.account.GroupAuthenticator;
import ua.pp.rozkladznu.app.account.GroupAuthenticatorHelper;
import ua.pp.rozkladznu.app.provider.ScheduleContract;
import ua.pp.rozkladznu.app.rest.GetDepartmentsMethod;
import ua.pp.rozkladznu.app.rest.GetGroupsMethod;
import ua.pp.rozkladznu.app.rest.RESTMethod;
import ua.pp.rozkladznu.app.rest.ResponseCallback;
import ua.pp.rozkladznu.app.rest.resource.Department;
import ua.pp.rozkladznu.app.rest.resource.Group;
import ua.pp.rozkladznu.app.util.MetricaUtils;
import ua.pp.rozkladznu.app.util.PreferencesUtils;

public class LoginActivity extends AccountAuthenticatorActivity
        implements View.OnClickListener {
    private static final int NO_SCHEDULE = 0;

    private DepartmentAdapter departmentAdapter;
    private GroupAdapter groupAdapter;

    private Button loginButton;
    private Button retryButton;

    private AccountManager accountManager;
    private LayoutInflater inflater;
    private ProgressWheel progressWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        accountManager = AccountManager.get(this);

        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(this);

        departmentAdapter = new DepartmentAdapter();
        groupAdapter = new GroupAdapter();

        if (null != savedInstanceState) {
            departmentAdapter.onRestoreInstanceState(savedInstanceState);
        }

        progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        departmentAdapter.onSaveInstanceState(outState);
        groupAdapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        departmentAdapter.invalidate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                finishLogin(
                        departmentAdapter.getSelectedDepartment(),
                        groupAdapter.getSelectedGroup(),
                        groupAdapter.getSelectedSubgroup()
                );
                break;
            case R.id.retry_button:
                retryButton.setVisibility(View.GONE);
                departmentAdapter.reload();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void finishLogin(Department department, Group group, int subgroup) {
        String accountType = getIntent().getStringExtra(GroupAuthenticator.KEY_AUTH_TOKEN_TYPE);

        if (accountType == null) {
            accountType = GroupAuthenticator.ACCOUNT_TYPE;
        }

        final Bundle userData = new Bundle();
        userData.putString(GroupAuthenticator.KEY_GROUP_ID, "" + group.getId());
        userData.putString(GroupAuthenticator.KEY_SUBGROUP_COUNT, "" + group.getSubgroupCount());
        userData.putString(GroupAuthenticator.KEY_SUBGROUP, "" + subgroup);
        userData.putString(GroupAuthenticator.KEY_DEPARTMENT_NAME, department.getName());

        if (new GroupAuthenticatorHelper(this).hasAccount()) {
            Toast.makeText(this, R.string.error_already_has_account, Toast.LENGTH_SHORT).show();
            return;
        }

        final Account account = new Account(group.getName(), accountType);
        boolean isAdded = accountManager.addAccountExplicitly(account, null, userData);

        if (isAdded) {
            if (App.getInstance().isMetricaInitialized())
                MetricaUtils.reportGroupAdd(department, group, subgroup);

            final Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, accountType);

            App.getInstance().getPreferencesUtils().saveActiveAccount(account.name);
            ContentResolver.setSyncAutomatically(
                    account, ScheduleContract.CONTENT_AUTHORITY, true
            );
            ContentResolver.addPeriodicSync(
                    account,
                    ScheduleContract.CONTENT_AUTHORITY,
                    Bundle.EMPTY,
                    PreferencesUtils.HOUR_IN_SECONDS
            );

            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, R.string.can_not_create_account, Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorMessage(int errorCode) {
        int errorTextResId;
        switch (errorCode) {
            case RESTMethod.ResponseCode.NETWORK_ERROR:
                errorTextResId = R.string.network_error;
                break;
            case RESTMethod.ResponseCode.TIMEOUT_ERROR:
                errorTextResId = R.string.time_out_error;
                break;
            case RESTMethod.ResponseCode.NO_CONNECTION_ERROR:
                errorTextResId = R.string.no_connection_error;
                break;
            case NO_SCHEDULE:
                errorTextResId = R.string.no_schedule;
                break;
            case RESTMethod.ResponseCode.SERVER_ERROR:
            case RESTMethod.ResponseCode.AUTH_FAILURE_ERROR:
            case RESTMethod.ResponseCode.PARSE_ERROR:
            case RESTMethod.ResponseCode.VOLLEY_ERROR:
                errorTextResId = R.string.server_error;
                break;
            default:
                errorTextResId = R.string.unknown_error;
        }
        retryButton.setVisibility(View.VISIBLE);
        Toast.makeText(this, errorTextResId, Toast.LENGTH_SHORT).show();
    }

    private Context getContext() {
        return this;
    }

    public class DepartmentAdapter extends BaseAdapter
            implements AdapterView.OnItemClickListener, View.OnClickListener,
            ResponseCallback<ArrayList<Department>> {
        public static final String KEY_SELECTED_DEPARTMENT = "SELECTED_DEPARTMENT";
        public static final String KEY_DEPARTMENTS_LOADED = "DEPARTMENTS_LOADED";
        public static final String KEY_DEPARTMENTS = "DEPARTMENTS";

        private boolean loaded = false;
        private int selectedDepartment = -1;
        private ArrayList<Department> departments;
        private GetDepartmentsMethod method;

        private MaterialDialog dialog;
        private TextView departmentName;
        private View chooserView;

        public DepartmentAdapter() {
            departments = new ArrayList<>();
            method = new GetDepartmentsMethod();
            method.prepare(RESTMethod.Filter.NONE);

            dialog = new MaterialDialog.Builder(LoginActivity.this)
                    .title(R.string.choose_department_hint)
                    .adapter(this)
                    .negativeText(R.string.cancel)
                    .build();

            ListView list = dialog.getListView();

            if (null != list) {
                list.setOnItemClickListener(this);
            }

            chooserView = findViewById(R.id.department_chooser);
            chooserView.setOnClickListener(this);

            departmentName = (TextView) findViewById(R.id.department_name);
        }

        @Override
        public int getCount() {
            return departments.size();
        }

        @Override
        public Department getItem(int position) {
            return departments.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater
                        .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(departments.get(position).getName());

            return convertView;
        }

        public void setDepartments(ArrayList<Department> departments) {
            this.departments = departments;
            notifyDataSetChanged();
            if (getCount() > 0) {
                loaded = true;
                chooserView.setVisibility(View.VISIBLE);
                departmentName.setText(R.string.choose_department_hint);
            } else {
                loaded = false;
                hide();
                showErrorMessage(NO_SCHEDULE);
            }
        }

        public void invalidate() {
            if (!loaded) {
                reload();
            }
        }

        public void reload() {
            showProgressWheel();
            hide();
            method.execute(this, RESTMethod.LOW_INTERNET_RETRY);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectDepartment(position, true);
            dialog.dismiss();
        }

        @Override
        public void onClick(View v) {
            if (loaded) {
                dialog.show();
            }
        }

        @Override
        public void onResponse(ArrayList<Department> departments) {
            hideProgressWheel();
            setDepartments(departments);
        }

        @Override
        public void onError(int responseCode) {
            hideProgressWheel();
            showErrorMessage(responseCode);
        }

        public void hide() {
            chooserView.setVisibility(View.GONE);
            groupAdapter.hide();
        }

        private void selectDepartment(int position, boolean clearGroupSelection) {
            selectedDepartment = position;
            if (position != -1) {
                Department department = getItem(position);
                departmentName.setText(department.getName());
                groupAdapter.load(department, clearGroupSelection);
            }
        }

        public Department getSelectedDepartment() {
            return getItem(selectedDepartment);
        }

        public void onSaveInstanceState(Bundle outState) {
            outState.putBoolean(KEY_DEPARTMENTS_LOADED, loaded);
            outState.putInt(KEY_SELECTED_DEPARTMENT, selectedDepartment);
            outState.putSerializable(KEY_DEPARTMENTS, departments);
        }

        public void onRestoreInstanceState(Bundle savedInstanceState) {
            loaded = savedInstanceState.getBoolean(KEY_DEPARTMENTS_LOADED);
            //noinspection unchecked
            setDepartments((ArrayList<Department>)
                    savedInstanceState.getSerializable(KEY_DEPARTMENTS));
            groupAdapter.onRestoreInstanceState(savedInstanceState);
            selectDepartment(savedInstanceState.getInt(KEY_SELECTED_DEPARTMENT, -1), false);
        }
    }

    public class GroupAdapter extends BaseAdapter
            implements ResponseCallback<ArrayList<Group>>, View.OnClickListener, MaterialDialog.ListCallback {
        public static final String KEY_DEPARTMENT_ID = "DEPARTMENT_ID";
        public static final String KEY_SELECTED_GROUP = "SELECTED_GROUP";
        public static final String KEY_SELECTED_SUBGROUP = "SELECTED_SUBGROUP";
        public static final String KEY_GROUPS_LOADED = "GROUPS_LOADED";
        public static final String KEY_GROUPS = "GROUPS";

        private int departmentId = -1;
        private int selectedGroup = -1;
        private int selectedSubgroup = -1;
        private boolean loaded = false;
        private ArrayList<Group> groups;
        private GetGroupsMethod method;

        private MaterialDialog chooseGroupDialog;
        private TextView subgroupName;
        private TextView groupName;
        private View groupChooserView;
        private View subgroupChooserView;

        private AdapterView.OnItemClickListener selectGroup = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectGroup(position);
                chooseGroupDialog.dismiss();
            }
        };

        public GroupAdapter() {
            groups = new ArrayList<>();
            method = new GetGroupsMethod();

            chooseGroupDialog = new MaterialDialog.Builder(getContext())
                    .title(R.string.choose_group_hint)
                    .adapter(this)
                    .negativeText(R.string.cancel)
                    .build();

            ListView list = chooseGroupDialog.getListView();

            if (null != list) {
                list.setOnItemClickListener(selectGroup);
            }

            groupChooserView = findViewById(R.id.group_chooser);
            groupChooserView.setOnClickListener(this);

            subgroupChooserView = findViewById(R.id.subgroup_chooser);
            subgroupChooserView.setOnClickListener(this);

            groupName = (TextView) findViewById(R.id.group_name);
            subgroupName = (TextView) findViewById(R.id.subgroup_name);
        }

        @Override
        public int getCount() {
            return groups.size();
        }

        @Override
        public Group getItem(int position) {
            return groups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater
                        .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(groups.get(position).getName());

            return convertView;
        }

        public void setGroups(ArrayList<Group> groups) {
            this.groups = groups;
            if (getCount() > 0) {
                groupChooserView.setVisibility(View.VISIBLE);
                groupName.setText("");
                loaded = true;
            } else {
                hide();
                loaded = false;
                showErrorMessage(NO_SCHEDULE);
            }
            notifyDataSetChanged();
        }

        public void load(Department department, boolean clearSelection) {
            if (departmentId == department.getId()) {
                if (clearSelection) {
                    clearGroupSelection();
                }
            } else {
                departmentId = department.getId();
                hide();
                showProgressWheel();
                method.prepare(RESTMethod.Filter.BY_DEPARTMENT_ID,
                        String.valueOf(department.getId()));
                method.execute(this, RESTMethod.LOW_INTERNET_RETRY);
            }
        }

        @Override
        public void onResponse(ArrayList<Group> groups) {
            hideProgressWheel();
            setGroups(groups);
        }

        @Override
        public void onError(int responseCode) {
            hideProgressWheel();
            showErrorMessage(responseCode);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.group_chooser:
                    if (loaded) {
                        chooseGroupDialog.show();
                    }
                    break;
                case R.id.subgroup_chooser:
                    Group group = getItem(selectedGroup);
                    String[] subgroups = new String[group.getSubgroupCount()];
                    for (int i = 0; i < group.getSubgroupCount(); i++) {
                        subgroups[i] = String.format(getString(R.string.subgroup_format_1), i + 1);
                    }

                    new MaterialDialog.Builder(getContext())
                            .title(R.string.choose_subgroup_hint)
                            .items(subgroups)
                            .itemsCallbackSingleChoice(selectedSubgroup, this)
                            .negativeText(R.string.cancel)
                            .show();
                    break;
            }
        }

        @Override
        public void onSelection(MaterialDialog dialog, View v, int position, CharSequence cs) {
            selectSubgroup(position);
        }

        public void hide() {
            groupChooserView.setVisibility(View.GONE);
            subgroupChooserView.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
        }

        public void selectGroup(int position) {
            selectedGroup = position;
            if (selectedGroup == -1) {
                clearGroupSelection();
            } else {
                Group group = getItem(selectedGroup);
                groupName.setText(group.getName());
                if (group.getSubgroupCount() == 0) {
                    subgroupChooserView.setVisibility(View.GONE);
                    loginButton.setVisibility(View.VISIBLE);
                } else {
                    subgroupChooserView.setVisibility(View.VISIBLE);
                    clearSubgroupSelection();
                }
            }
        }

        public void selectSubgroup(int subgroup) {
            selectedSubgroup = subgroup;
            if (selectedSubgroup == -1) {
                clearSubgroupSelection();
            } else {
                subgroupName.setText(
                        String.format(getString(R.string.subgroup_format_1), getSelectedSubgroup())
                );
                loginButton.setVisibility(View.VISIBLE);
            }
        }

        private void clearGroupSelection() {
            selectedGroup = -1;
            groupName.setText("");
            clearSubgroupSelection();
            subgroupChooserView.setVisibility(View.GONE);
        }

        public void clearSubgroupSelection() {
            selectedSubgroup = -1;
            subgroupName.setText("");
            loginButton.setVisibility(View.GONE);
        }

        public Group getSelectedGroup() {
            return getItem(selectedGroup);
        }

        public int getSelectedSubgroup() {
            return selectedSubgroup + 1;
        }

        public void onSaveInstanceState(Bundle outState) {
            outState.putInt(KEY_DEPARTMENT_ID, departmentId);
            outState.putBoolean(KEY_GROUPS_LOADED, loaded);
            if (loaded) {
                outState.putSerializable(KEY_GROUPS, groups);
                outState.putInt(KEY_SELECTED_GROUP, selectedGroup);
                outState.putInt(KEY_SELECTED_SUBGROUP, selectedSubgroup);
            }
        }

        public void onRestoreInstanceState(Bundle savedInstanceState) {
            departmentId = savedInstanceState.getInt(KEY_DEPARTMENT_ID, -1);
            loaded = savedInstanceState.getBoolean(KEY_GROUPS_LOADED, false);
            if (departmentId != -1 && loaded) {
                //noinspection unchecked
                setGroups((ArrayList<Group>) savedInstanceState.getSerializable(KEY_GROUPS));
                selectGroup(savedInstanceState.getInt(KEY_SELECTED_GROUP, -1));
                selectSubgroup(savedInstanceState.getInt(KEY_SELECTED_SUBGROUP, -1));
            }
        }

    }

    public void showProgressWheel() {
        retryButton.setVisibility(View.GONE);
        progressWheel.setVisibility(View.VISIBLE);
    }

    public void hideProgressWheel() {
        progressWheel.setVisibility(View.GONE);
    }
}



