<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.inventory')">
      <a-row>
        <a-col>
          <a-input-number v-model="condition.characterId" placeholder="角色ID" allow-clear />
          <a-input v-model="condition.characterName" placeholder="角色名" allow-clear />
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
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
          <a-table-column title="角色ID" data-index="characterId" :width="100" align="center" />
          <a-table-column title="物品ID" data-index="itemId" :width="100" align="center" />
          <a-table-column title="数量" data-index="quantity" :width="80" align="center" />
          <a-table-column title="位置" data-index="position" :width="80" align="center" />
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 20px"
        :total="total" :page-size="condition.pageSize" :current="condition.pageNo"
        show-total show-jumper show-page-size
        @change="pageChange" @page-size-change="pageSizeChange"
      />
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { InventoryCondition, getInventoryList } from '@/api/inventory';
  import useLoading from '@/hooks/loading';

  const { setLoading, loading } = useLoading(false);
  const condition = ref<InventoryCondition>({ pageNo: 1, pageSize: 20 });
  const total = ref(0);
  const tableData = ref<any[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getInventoryList(condition.value);
      tableData.value = data.records || [];
      total.value = data.totalRow || 0;
    } finally { setLoading(false); }
  };
  const resetClick = () => { condition.value = { pageNo: 1, pageSize: 20 }; loadData(); };
  const pageChange = (d: number) => { condition.value.pageNo = d; loadData(); };
  const pageSizeChange = (d: number) => { condition.value.pageNo = 1; condition.value.pageSize = d; loadData(); };

  loadData();
</script>

<script lang="ts">
  export default { name: 'Inventory' };
</script>
