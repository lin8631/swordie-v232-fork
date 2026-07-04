<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.account.list')">
      <a-row>
        <a-col>
          <a-input-number v-model="searchForm.id" placeholder="ID" allow-clear />
          <a-input v-model="searchForm.name" placeholder="账号" allow-clear />
          <a-space>
            <a-button type="primary" @click="searchClick">查询</a-button>
            <a-button @click="resetClick">重置</a-button>
            <a-button type="primary" status="success" @click="addClick">新增</a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-modal v-model:visible="addModalVisible" :title="$t('button.add')" @ok="handleAddOk" @cancel="handleAddCancel">
        <a-form :model="addForm" layout="vertical">
          <a-form-item label="账号名" required>
            <a-input v-model="addForm.name" placeholder="用户名" />
          </a-form-item>
          <a-form-item label="密码" required>
            <a-input-password v-model="addForm.password" placeholder="密码" />
          </a-form-item>
          <a-form-item label="确认密码" required>
            <a-input-password v-model="addForm.checkPassword" placeholder="确认密码" />
          </a-form-item>
          <a-form-item label="邮箱">
            <a-input v-model="addForm.email" placeholder="邮箱" />
          </a-form-item>
        </a-form>
      </a-modal>

      <a-modal v-model:visible="editModalVisible" :title="$t('button.edit')" @ok="handleEditOk" @cancel="handleEditCancel">
        <a-form :model="editForm" layout="vertical">
          <a-form-item label="邮箱">
            <a-input v-model="editForm.email" placeholder="邮箱" />
          </a-form-item>
          <a-form-item label="新密码">
            <a-input-password v-model="editForm.newPwd" placeholder="留空不修改" />
          </a-form-item>
          <a-form-item label="确认密码">
            <a-input-password v-model="editForm.newPwdCheck" placeholder="留空不修改" />
          </a-form-item>
          <a-form-item label="NX Credit">
            <a-input-number v-model="editForm.donationPoints" :min="0" style="width:100%" />
          </a-form-item>
          <a-form-item label="角色槽位">
            <a-input-number v-model="editForm.characterSlots" :min="1" :max="48" style="width:100%" />
          </a-form-item>
          <a-form-item label="账号类型">
            <a-select v-model="editForm.accountType">
              <a-option value="Player">Player</a-option>
              <a-option value="Tester">Tester</a-option>
              <a-option value="GM">GM</a-option>
              <a-option value="SuperGM">SuperGM</a-option>
              <a-option value="Admin">Admin</a-option>
            </a-select>
          </a-form-item>
        </a-form>
      </a-modal>

      <a-table
        row-key="id"
        :loading="loading"
        :data="tableData"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="80" align="center" />
          <a-table-column title="账号" data-index="name" :width="150" align="center" />
          <a-table-column title="类型" data-index="accountType" :width="100" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.accountType === 'Admin'" color="red">Admin</a-tag>
              <a-tag v-else-if="record.accountType === 'GM' || record.accountType === 'SuperGM'" color="blue">{{ record.accountType }}</a-tag>
              <a-tag v-else color="gray">{{ record.accountType }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="creationDate" :width="180" align="center" />
          <a-table-column title="封禁" data-index="banExpireDate" :width="80" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.banExpireDate" color="red">是</a-tag>
              <a-tag v-else color="green">否</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="邮箱" data-index="email" :width="200" align="center" />
          <a-table-column title="积分" data-index="votePoints" :width="80" align="center" />
          <a-table-column title="捐赠点" data-index="donationPoints" :width="80" align="center" />
          <a-table-column title="操作" :width="250" align="center">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="mini" @click="editClick(record)">编辑</a-button>
                <a-popconfirm
                  content="确定要封禁此账号吗？"
                  @ok="banClick(record.id)"
                >
                  <a-button v-if="!record.banExpireDate" type="text" size="mini" status="danger">封禁</a-button>
                </a-popconfirm>
                <a-popconfirm
                  content="确定要解封此账号吗？"
                  @ok="unbanClick(record.id)"
                >
                  <a-button v-if="record.banExpireDate" type="text" size="mini" status="success">解封</a-button>
                </a-popconfirm>
                <a-popconfirm
                  content="确定要删除此账号吗？"
                  @ok="deleteClick(record.id)"
                >
                  <a-button type="text" size="mini" status="danger">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 20px"
        :total="total"
        :page-size="pageSize"
        :current="pageNo"
        show-total
        show-jumper
        show-page-size
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import {
    getAccountList,
    deleteAccount,
    banAccount,
    unbanAccount,
    resetLoggedIn,
    addAccount,
    updateAccountByGM,
  } from '@/api/account';
  import { Message } from '@arco-design/web-vue';

  const { loading, setLoading } = useLoading(false);
  const tableData = ref<any[]>([]);
  const total = ref(0);
  const pageNo = ref(1);
  const pageSize = ref(20);
  const searchForm = ref({ id: undefined as number | undefined, name: '' });

  const addModalVisible = ref(false);
  const addForm = ref({ name: '', password: '', checkPassword: '', email: '' });
  const editModalVisible = ref(false);
  const editForm = ref({ id: 0, email: '', newPwd: '', newPwdCheck: '', donationPoints: 0, characterSlots: 12, accountType: 'Player' });

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getAccountList(
        pageNo.value, pageSize.value,
        searchForm.value.id, searchForm.value.name
      );
      tableData.value = data.records || [];
      total.value = data.total || 0;
    } finally {
      setLoading(false);
    }
  };

  const searchClick = () => { pageNo.value = 1; loadData(); };
  const resetClick = () => {
    searchForm.value = { id: undefined, name: '' };
    pageNo.value = 1;
    loadData();
  };
  const pageChange = (page: number) => { pageNo.value = page; loadData(); };
  const pageSizeChange = (size: number) => { pageSize.value = size; pageNo.value = 1; loadData(); };
  const addClick = () => {
    addForm.value = { name: '', password: '', checkPassword: '', email: '' };
    addModalVisible.value = true;
  };
  const editClick = (record: any) => {
    editForm.value = {
      id: record.id,
      email: record.email || '',
      newPwd: '',
      newPwdCheck: '',
      donationPoints: record.accountType === 'Admin' ? 0 : 0,
      characterSlots: 12,
      accountType: record.accountType || 'Player',
    };
    editModalVisible.value = true;
  };
  const handleAddOk = async () => {
    if (!addForm.value.name || !addForm.value.password) {
      Message.error('用户名和密码不能为空');
      return;
    }
    if (addForm.value.password !== addForm.value.checkPassword) {
      Message.error('两次密码不一致');
      return;
    }
    try {
      setLoading(true);
      await addAccount({
        name: addForm.value.name,
        password: addForm.value.password,
        email: addForm.value.email,
      });
      Message.success('创建成功');
      addModalVisible.value = false;
      loadData();
    } catch {
      Message.error('创建失败');
    } finally {
      setLoading(false);
    }
  };
  const handleAddCancel = () => { addModalVisible.value = false; };
  const handleEditOk = async () => {
    try {
      setLoading(true);
      const payload: any = {};
      if (editForm.value.email) payload.email = editForm.value.email;
      if (editForm.value.newPwd) {
        if (editForm.value.newPwd !== editForm.value.newPwdCheck) {
          Message.error('两次密码不一致');
          return;
        }
        payload.newPwd = editForm.value.newPwd;
      }
      payload.donationPoints = editForm.value.donationPoints;
      payload.characterSlots = editForm.value.characterSlots;
      payload.accountType = editForm.value.accountType;
      await updateAccountByGM(editForm.value.id, payload);
      Message.success('更新成功');
      editModalVisible.value = false;
      loadData();
    } catch {
      Message.error('更新失败');
    } finally {
      setLoading(false);
    }
  };
  const handleEditCancel = () => { editModalVisible.value = false; };
  const banClick = async (id: number) => {
    await banAccount(id);
    Message.success('封禁成功');
    loadData();
  };
  const unbanClick = async (id: number) => {
    await unbanAccount(id);
    Message.success('解封成功');
    loadData();
  };
  const deleteClick = async (id: number) => {
    await deleteAccount(id);
    Message.success('删除成功');
    loadData();
  };

  loadData();
</script>

<script lang="ts">
  export default { name: 'AccountList' };
</script>
