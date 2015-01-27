package ua.zp.rozklad.app.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.rest.GetDepartmentsMethod;
import ua.zp.rozklad.app.rest.GetGroupsMethod;
import ua.zp.rozklad.app.rest.RESTMethod;
import ua.zp.rozklad.app.rest.ResponseCallback;
import ua.zp.rozklad.app.rest.resource.Department;
import ua.zp.rozklad.app.rest.resource.Group;
import ua.zp.rozklad.app.account.GroupAuthenticator;

public class LoginActivity extends AccountAuthenticatorActivity
        implements View.OnClickListener {
    public static final String KEY_AUTH_TOKEN_TYPE = "AUTH_TOKEN_TYPE";
    public static final String KEY_GROUP_ID = "KEY_GROUP_ID";
    public static final String KEY_LAST_UPDATE = "KEY_LAST_UPDATE";

    private Spinner departments;
    private DepartmentAdapter departmentAdapter;

    private Spinner groups;
    private GroupAdapter groupAdapter;

    private ProgressBar progress;

    private LinearLayout departmentList;
    private LinearLayout groupList;

    private Button login;

    private Group selectedGroup = null;

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accountManager = AccountManager.get(this);

        departments = (Spinner) findViewById(R.id.departments);
        departmentAdapter = new DepartmentAdapter(this);
        departments.setAdapter(departmentAdapter);
        departments.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Department department = departmentAdapter.getItem(position);

                GetGroupsMethod method = new GetGroupsMethod();
                method.prepare(RESTMethod.Filter.BY_DEPARTMENT_ID,
                        String.valueOf(department.getId()));
                method.execute(new ResponseCallback<ArrayList<Group>>() {
                    @Override
                    public void onResponse(ArrayList<Group> groups) {
                        if (groups.size() > 0) {
                            groupAdapter.setGroups(groups);
                            groupList.setVisibility(View.VISIBLE);
                        } else {
                            groupList.setVisibility(View.GONE);
                            login.setVisibility(View.GONE);
                        }
                        progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(int responseCode) {
                        onErrorResponse(responseCode);
                    }
                });
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                groupList.setVisibility(View.GONE);
                login.setVisibility(View.GONE);
            }
        });

        groups = (Spinner) findViewById(R.id.groups);
        groupAdapter = new GroupAdapter(this);
        groups.setAdapter(groupAdapter);
        groups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                login.setVisibility(View.GONE);
                selectedGroup = groupAdapter.getItem(position);
                login.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                login.setVisibility(View.GONE);
            }
        });

        progress = (ProgressBar) findViewById(R.id.login_progress);
        progress.setIndeterminate(true);
        progress.setProgress(0);

        departmentList = (LinearLayout) findViewById(R.id.department_list);
        groupList = (LinearLayout) findViewById(R.id.group_list);

        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);

        GetDepartmentsMethod method = new GetDepartmentsMethod();
        method.prepare(RESTMethod.Filter.NONE);
        method.execute(new ResponseCallback<ArrayList<Department>>() {
            @Override
            public void onResponse(ArrayList<Department> departments) {
                progress.setVisibility(View.GONE);
                departmentAdapter.setDepartments(departments);
                departmentList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(int responseCode) {
                onErrorResponse(responseCode);
            }
        });
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

    private void onErrorResponse(int responseCode) {
        Toast.makeText(getApplicationContext(), "Error: " + responseCode,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if (selectedGroup != null) {
                    finishLogin(selectedGroup);
                }
                break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public class DepartmentAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<Department> departments;

        public DepartmentAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            departments = new ArrayList<>();
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
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            Department department = departments.get(position);

            TextView name = (TextView) convertView.findViewById(android.R.id.text1);

            name.setText(department.getName());

            return convertView;
        }

        public void setDepartments(ArrayList<Department> departments) {
            this.departments = departments;
            notifyDataSetChanged();
        }
    }

    public class GroupAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<Group> groups;

        public GroupAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            groups = new ArrayList<>();
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
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            Group group = groups.get(position);

            TextView name = (TextView) convertView.findViewById(android.R.id.text1);

            name.setText(group.getName());

            return convertView;
        }

        public void setGroups(ArrayList<Group> groups) {
            this.groups = groups;
            notifyDataSetChanged();
        }
    }
}



