<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.autoban')">
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
          <a-table-column title="类型" data-index="type" :width="120" align="center" />
          <a-table-column title="名称" data-index="name" :width="200" align="center" />
          <a-table-column title="描述" data-index="description" align="center" />
          <a-table-column title="分数" data-index="points" :width="80" align="center" />
          <a-table-column title="过期时间" data-index="expireTimeSeconds" :width="120" align="center" />
          <a-table-column title="禁用" data-index="disabled" :width="80" align="center">
            <template #cell="{ record }">
              <a-tag :color="record.disabled ? 'red' : 'green'">{{ record.disabled ? '是' : '否' }}</a-tag>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { getAutobanConfigList } from '@/api/autoban';
  import useLoading from '@/hooks/loading';

  const { setLoading, loading } = useLoading(false);
  const tableData = ref<any[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getAutobanConfigList();
      tableData.value = data || [];
    } finally { setLoading(false); }
  };
  loadData();
</script>

<script lang="ts">
  export default { name: 'Autoban' };
</script>
