<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.drop')">
      <a-row>
        <a-col>
          <a-input-number v-model="condition.dropperId" placeholder="怪物ID" allow-clear />
          <a-input-number v-model="condition.itemId" placeholder="物品ID" allow-clear @keydown.enter="loadData" />
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
          <a-table-column title="怪物ID" data-index="dropperId" :width="100" align="center" />
          <a-table-column title="怪物" data-index="dropperName" :width="150" align="center" />
          <a-table-column title="物品ID" data-index="itemId" :width="100" align="center" />
          <a-table-column title="物品" data-index="itemName" :width="200" align="center" />
          <a-table-column title="最少" data-index="minimumQuantity" :width="80" align="center" />
          <a-table-column title="最多" data-index="maximumQuantity" :width="80" align="center" />
          <a-table-column title="爆率%" :width="100" align="right">
            <template #cell="{ record }">{{ (record.chance / 10000).toFixed(4) }}</template>
          </a-table-column>
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
  import { DropConditionState, getDrop } from '@/api/drop';
  import useLoading from '@/hooks/loading';

  const { setLoading, loading } = useLoading(false);
  const condition = ref<DropConditionState>({ pageNo: 1, pageSize: 20, onlyTotal: false, notPage: false });
  const total = ref(0);
  const tableData = ref<any[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getDrop(condition.value);
      tableData.value = data.records || [];
      total.value = data.totalRow || 0;
    } finally { setLoading(false); }
  };
  const resetClick = () => { condition.value = { pageNo: 1, pageSize: 20, onlyTotal: false, notPage: false }; loadData(); };
  const pageChange = (d: number) => { condition.value.pageNo = d; loadData(); };
  const pageSizeChange = (d: number) => { condition.value.pageNo = 1; condition.value.pageSize = d; loadData(); };

  loadData();
</script>

<script lang="ts">
  export default { name: 'DropData' };
</script>
