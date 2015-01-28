package ua.zp.rozklad.app.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
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

import java.util.ArrayList;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.account.GroupAuthenticator;
import ua.zp.rozklad.app.rest.GetDepartmentsMethod;
import ua.zp.rozklad.app.rest.GetGroupsMethod;
import ua.zp.rozklad.app.rest.RESTMethod;
import ua.zp.rozklad.app.rest.ResponseCallback;
import ua.zp.rozklad.app.rest.resource.Department;
import ua.zp.rozklad.app.rest.resource.Group;

public class LoginActivity extends AccountAuthenticatorActivity
        implements View.OnClickListener {
    private static final int NO_SCHEDULE = 0;

    public static final String KEY_AUTH_TOKEN_TYPE = "AUTH_TOKEN_TYPE";
    public static final String KEY_GROUP_ID = "KEY_GROUP_ID";
    public static final String KEY_LAST_UPDATE = "KEY_LAST_UPDATE";

    private DepartmentAdapter departmentAdapter;
    private GroupAdapter groupAdapter;

    private Button loginButton;

    private AccountManager accountManager;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        accountManager = AccountManager.get(this);

        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        departmentAdapter = new DepartmentAdapter();
        groupAdapter = new GroupAdapter();

        if (null != savedInstanceState) {
            departmentAdapter.onRestoreInstanceState(savedInstanceState);
        }
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
                finishLogin(groupAdapter.getSelectedGroup());
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void finishLogin(Group group) {
        String accountType = this.getIntent().getStringExtra(KEY_AUTH_TOKEN_TYPE);

        if (accountType == null) {
            accountType = GroupAuthenticator.ACCOUNT_TYPE;
        }

        final Bundle userData = new Bundle();
        userData.putString(KEY_GROUP_ID, String.valueOf(group.getId()));
        userData.putString(KEY_LAST_UPDATE, String.valueOf(0));

        final Account account = new Account(group.getName(), accountType);
        accountManager.addAccountExplicitly(account, null, userData);


        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, accountType);

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showErrorMessage(int errorCode) {
        switch (errorCode) {
            case RESTMethod.ResponseCode.VOLLEY_ERROR:
                Toast.makeText(this, R.string.volley_error, Toast.LENGTH_SHORT).show();
                break;
            case NO_SCHEDULE:
                Toast.makeText(this, R.string.no_schedule, Toast.LENGTH_SHORT).show();
                break;
        }
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
                // TODO: Show progress animation
                hide();
                method.execute(this);
            }
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
            setDepartments(departments);
        }

        @Override
        public void onError(int responseCode) {
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
                groupAdapter.load(department.getId(), clearGroupSelection);
            }
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
            implements ResponseCallback<ArrayList<Group>>, AdapterView.OnItemClickListener,
            View.OnClickListener {
        public static final String KEY_DEPARTMENT_ID = "DEPARTMENT_ID";
        public static final String KEY_SELECTED_GROUP = "SELECTED_GROUP";
        public static final String KEY_GROUPS_LOADED = "GROUPS_LOADED";
        public static final String KEY_GROUPS = "GROUPS";

        private int departmentId = -1;
        private int selectedGroup = -1;
        private boolean loaded = false;
        private ArrayList<Group> groups;
        private GetGroupsMethod method;

        private MaterialDialog dialog;
        private TextView groupName;
        private View chooserView;

        public GroupAdapter() {
            groups = new ArrayList<>();
            method = new GetGroupsMethod();

            dialog = new MaterialDialog.Builder(LoginActivity.this)
                    .title(R.string.choose_group_hint)
                    .adapter(this)
                    .negativeText(R.string.cancel)
                    .build();

            ListView list = dialog.getListView();

            if (null != list) {
                list.setOnItemClickListener(this);
            }

            chooserView = findViewById(R.id.group_chooser);
            chooserView.setOnClickListener(this);

            groupName = (TextView) findViewById(R.id.group_name);
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
                chooserView.setVisibility(View.VISIBLE);
                groupName.setText("");
                loaded = true;
            } else {
                hide();
                loaded = false;
                showErrorMessage(NO_SCHEDULE);
            }
            notifyDataSetChanged();
        }

        public void load(int departmentId, boolean clearSelection) {
            if (this.departmentId == departmentId) {
                if (clearSelection) {
                    clearSelection();
                }
            } else {
                this.departmentId = departmentId;
                hide();
                // TODO: Show progress animation
                method.prepare(RESTMethod.Filter.BY_DEPARTMENT_ID, String.valueOf(departmentId));
                method.execute(this);
            }
        }

        @Override
        public void onResponse(ArrayList<Group> groups) {
            setGroups(groups);
        }

        @Override
        public void onError(int responseCode) {
            showErrorMessage(responseCode);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectGroup(position);
            dialog.dismiss();
        }

        @Override
        public void onClick(View v) {
            if (loaded) {
                dialog.show();
            }
        }

        public void hide() {
            chooserView.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
        }

        public void selectGroup(int position) {
            selectedGroup = position;
            if (selectedGroup == -1) {
                clearSelection();
            } else {
                Group group = getItem(selectedGroup);
                groupName.setText(group.getName());
                loginButton.setVisibility(View.VISIBLE);
            }
        }

        private void clearSelection() {
            selectedGroup = -1;
            groupName.setText("");
            loginButton.setVisibility(View.GONE);
        }

        public Group getSelectedGroup() {
            return getItem(selectedGroup);
        }

        public void onSaveInstanceState(Bundle outState) {
            outState.putInt(KEY_DEPARTMENT_ID, departmentId);
            outState.putBoolean(KEY_GROUPS_LOADED, loaded);
            if (loaded) {
                outState.putSerializable(KEY_GROUPS, groups);
                outState.putInt(KEY_SELECTED_GROUP, selectedGroup);
            }
        }

        public void onRestoreInstanceState(Bundle savedInstanceState) {
            departmentId = savedInstanceState.getInt(KEY_DEPARTMENT_ID, -1);
            loaded = savedInstanceState.getBoolean(KEY_GROUPS_LOADED, false);
            if (departmentId != -1 & loaded) {
                //noinspection unchecked
                setGroups((ArrayList<Group>) savedInstanceState.getSerializable(KEY_GROUPS));
                selectGroup(savedInstanceState.getInt(KEY_SELECTED_GROUP, -1));
            }
        }
    }
}



