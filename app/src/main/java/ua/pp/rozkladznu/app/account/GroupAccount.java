package ua.pp.rozkladznu.app.account;

import android.accounts.Account;
import android.accounts.AccountManager;

import static java.lang.Integer.parseInt;

/**
 * @author Vojko Vladimir
 */
public class GroupAccount {

    private Account account;
    private String groupName;
    private String departmentName;
    private int groupId;
    private int subgroupCount;
    private int subgroup;

    public GroupAccount(AccountManager manager, Account account) {
        this.account = account;
        groupName = account.name;
        departmentName = manager.getUserData(account, GroupAuthenticator.KEY_DEPARTMENT_NAME);
        groupId = parseInt(manager.getUserData(account, GroupAuthenticator.KEY_GROUP_ID));
        subgroupCount = parseInt(manager.getUserData(account, GroupAuthenticator.KEY_SUBGROUP_COUNT));
        subgroup = parseInt(manager.getUserData(account, GroupAuthenticator.KEY_SUBGROUP));
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getSubgroupCount() {
        return subgroupCount;
    }

    public int getSubgroup() {
        return subgroup;
    }

    public Account getBaseAccount() {
        return account;
    }

    public void setSubgroup(int subgroup) {
        this.subgroup = subgroup;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupAccount) {
            GroupAccount compared = (GroupAccount) obj;
            return account.name.equals(compared.getBaseAccount().name);
        }

        return false;
    }
}
