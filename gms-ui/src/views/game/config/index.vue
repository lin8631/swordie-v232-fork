<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.config')">
      <a-space direction="vertical" align="start">
        <a-form-item :label="$t('config.search.type.label')">
          <a-radio-group v-model="condition.type" type="button" @change="loadConfigs">
            <a-radio v-for="type in types" :key="type" :value="type">
              {{ type }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item>
          <a-col :offset="0">
            <a-input v-model="condition.filter" placeholder="搜索" @keydown.enter="loadConfigs" />
            <a-button type="primary" @click="loadConfigs">{{ $t('button.search') }}</a-button>
            <a-button @click="resetSearch">{{ $t('button.reset') }}</a-button>
            <a-button type="primary" status="success" @click="addClick">{{ $t('button.add') }}</a-button>
          </a-col>
        </a-form-item>
      </a-space>
      <a-table
        row-key="id"
        :loading="loading"
        :data="configList"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="60" align="center" />
          <a-table-column title="Key" data-index="configCode" :width="200" align="center" />
          <a-table-column title="Value" data-index="configValue" :width="150" align="center" />
          <a-table-column title="描述" data-index="configDesc" align="center" />
          <a-table-column title="操作" :width="100" align="center">
            <template #cell="{ record }">
              <a-button type="text" size="mini" @click="uptClick(record)">{{ $t('button.edit') }}</a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 20px"
        :total="total"
        :page-size="condition.pageSize"
        :current="condition.pageNo"
        show-total show-jumper show-page-size
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue';
  import { getConfigList, getConfigTypeList } from '@/api/config';
  import useLoading from '@/hooks/loading';

  const types = ref<string[]>(['all']);
  const condition = reactive({ type: 'all', subType: 'All', filter: '', pageNo: 1, pageSize: 20 });
  const configList = ref<any[]>([]);
  const total = ref(0);
  const { loading, setLoading } = useLoading(false);

  const loadConfigs = async () => {
    setLoading(true);
    try {
      const { data } = await getConfigList(condition as any);
      configList.value = data.records || [];
      total.value = data.totalRow || 0;
    } finally { setLoading(false); }
  };

  const pageChange = (d: number) => { condition.pageNo = d; loadConfigs(); };
  const pageSizeChange = (d: number) => { condition.pageNo = 1; condition.pageSize = d; loadConfigs(); };
  const resetSearch = () => { condition.type = 'all'; condition.filter = ''; condition.pageNo = 1; loadConfigs(); };
  const addClick = () => {};
  const uptClick = (record: any) => {};

  getConfigTypeList().then(({ data }) => { types.value = ['all', ...(data.types || [])]; });
  loadConfigs();
</script>

<script lang="ts">
  export default { name: 'Config' };
</script>
