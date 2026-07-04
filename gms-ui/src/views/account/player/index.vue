<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.account.player')">
      <a-row>
        <a-col>
          <a-input-number v-model="searchForm.id" placeholder="ID" allow-clear />
          <a-input v-model="searchForm.name" placeholder="角色名" allow-clear />
          <a-input-number v-model="searchForm.map" placeholder="地图ID" allow-clear />
          <a-space>
            <a-button type="primary" @click="searchClick">查询</a-button>
            <a-button @click="resetClick">重置</a-button>
          </a-space>
        </a-col>
      </a-row>
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
          <a-table-column title="角色名" data-index="name" :width="150" align="center" />
          <a-table-column title="等级" data-index="level" :width="80" align="center" />
          <a-table-column title="职业" data-index="job" :width="150" align="center" />
          <a-table-column title="地图ID" data-index="map" :width="100" align="center" />
          <a-table-column title="频道" data-index="channel" :width="60" align="center" />
          <a-table-column title="世界" data-index="world" :width="60" align="center" />
          <a-table-column title="操作" :width="100" align="center">
            <template #cell="{ record }">
              <a-button type="text" size="mini" @click="giveClick(record)">发放</a-button>
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
      <a-modal v-model:visible="giveModalVisible" title="发放资源" @ok="handleGiveOk" @cancel="handleGiveCancel">
        <a-form :model="giveForm" layout="vertical">
          <a-form-item label="角色名" required>
            <a-input v-model="giveForm.player" placeholder="角色名" :disabled="!!giveTarget" />
          </a-form-item>
          <a-form-item label="类型" required>
            <a-select v-model="giveForm.type">
              <a-option v-for="gt in giveTypes" :key="gt.value" :value="gt.value" :label="gt.label" />
            </a-select>
          </a-form-item>
          <a-form-item v-if="giveForm.type === 1" label="道具ID" required>
            <a-input-number v-model="giveForm.id" :min="1" style="width:100%" />
          </a-form-item>
          <a-form-item label="数量">
            <a-input-number v-model="giveForm.quantity" :min="1" :max="99999" style="width:100%" />
          </a-form-item>
        </a-form>
      </a-modal>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import { getPlayerList, givePlayerSrc, GiveForm } from '@/api/player';
  import { Message } from '@arco-design/web-vue';

  const { loading, setLoading } = useLoading(false);
  const tableData = ref<any[]>([]);
  const total = ref(0);
  const pageNo = ref(1);
  const pageSize = ref(20);
  const searchForm = ref({ id: undefined as number | undefined, name: '', map: undefined as number | undefined });

  const giveModalVisible = ref(false);
  const giveTarget = ref<any>(null);
  const giveForm = ref<GiveForm>({
    player: '',
    type: 1,
    id: 0,
    quantity: 1,
  });
  const giveTypes = [
    { value: 1, label: '道具' },
    { value: 2, label: '枫叶点' },
    { value: 3, label: '抵用券' },
    { value: 4, label: '点数' },
    { value: 6, label: '经验' },
  ];

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getPlayerList(pageNo.value, pageSize.value, searchForm.value.id, searchForm.value.name, searchForm.value.map);
      tableData.value = data.records || [];
      total.value = data.total || 0;
    } finally {
      setLoading(false);
    }
  };

  const searchClick = () => { pageNo.value = 1; loadData(); };
  const resetClick = () => {
    searchForm.value = { id: undefined, name: '', map: undefined };
    pageNo.value = 1;
    loadData();
  };
  const pageChange = (page: number) => { pageNo.value = page; loadData(); };
  const pageSizeChange = (size: number) => { pageSize.value = size; pageNo.value = 1; loadData(); };

  const giveClick = (record: any) => {
    giveTarget.value = record;
    giveForm.value = {
      player: record.name,
      type: 1,
      id: 0,
      quantity: 1,
    };
    giveModalVisible.value = true;
  };

  const handleGiveOk = async () => {
    if (!giveForm.value.player) {
      Message.error('请选择角色');
      return;
    }
    if (giveForm.value.type === 1 && !giveForm.value.id) {
      Message.error('请输入道具ID');
      return;
    }
    try {
      setLoading(true);
      await givePlayerSrc(giveForm.value);
      Message.success('发放成功');
      giveModalVisible.value = false;
    } catch {
      Message.error('发放失败');
    } finally {
      setLoading(false);
    }
  };

  const handleGiveCancel = () => {
    giveModalVisible.value = false;
  };

  loadData();
</script>

<script lang="ts">
  export default { name: 'PlayerList' };
</script>
