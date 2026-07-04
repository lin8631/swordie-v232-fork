<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.gachapon')">
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
          <a-table-column title="名称" data-index="name" :width="200" align="center" />
          <a-table-column title="权重" data-index="weight" :width="80" align="center" />
          <a-table-column title="公开" data-index="isPublic" :width="80" align="center">
            <template #cell="{ record }">
              <a-tag :color="record.isPublic ? 'green' : 'gray'">{{ record.isPublic ? '是' : '否' }}</a-tag>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { getPools } from '@/api/gachapon';
  import useLoading from '@/hooks/loading';

  const { setLoading, loading } = useLoading(false);
  const tableData = ref<any[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getPools({ pageNo: 1, pageSize: 100 });
      tableData.value = data.records || [];
    } finally { setLoading(false); }
  };
  loadData();
</script>

<script lang="ts">
  export default { name: 'Gachapon' };
</script>
