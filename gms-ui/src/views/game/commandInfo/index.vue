<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.command')">
      <a-table
        row-key="id"
        :loading="loading"
        :data="tableData"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column title="命令" data-index="syntax" :width="200" align="center" />
          <a-table-column title="等级" data-index="level" :width="80" align="center" />
          <a-table-column title="描述" data-index="description" align="center" />
          <a-table-column title="启用" data-index="enabled" :width="80" align="center">
            <template #cell="{ record }">
              <a-tag :color="record.enabled ? 'green' : 'red'">{{ record.enabled ? '是' : '否' }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="类名" data-index="clazz" :width="300" align="center" />
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { getCommandList } from '@/api/command';
  import useLoading from '@/hooks/loading';

  const { setLoading, loading } = useLoading(false);
  const tableData = ref<any[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getCommandList({});
      tableData.value = data.records || data || [];
    } finally { setLoading(false); }
  };
  loadData();
</script>

<script lang="ts">
  export default { name: 'CommandInfo' };
</script>
