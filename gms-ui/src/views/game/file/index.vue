<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.file')">
      <a-row>
        <a-space>
          <a-input v-model="currentPath" placeholder="文件路径" style="width: 400px" />
          <a-button type="primary" @click="loadFileTree">加载</a-button>
        </a-space>
      </a-row>
      <a-table
        row-key="name"
        :loading="loading"
        :data="fileList"
        :pagination="false"
        :bordered="{ cell: true }"
        style="margin-top: 16px"
      >
        <template #columns>
          <a-table-column title="名称" data-index="name" align="center" />
          <a-table-column title="类型" :width="100" align="center">
            <template #cell="{ record }">
              <a-tag :color="record.directory ? 'blue' : 'green'">{{ record.directory ? '目录' : '文件' }}</a-tag>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { treeFile } from '@/api/fileTree';
  import useLoading from '@/hooks/loading';

  const { setLoading, loading } = useLoading(false);
  const currentPath = ref('/');
  const fileList = ref<any[]>([]);

  const loadFileTree = async () => {
    setLoading(true);
    try {
      const { data } = await treeFile({ currentKey: currentPath.value });
      fileList.value = data || [];
    } finally { setLoading(false); }
  };
</script>

<script lang="ts">
  export default { name: 'FileManager' };
</script>
