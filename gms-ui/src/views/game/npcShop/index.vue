<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.npcShop')">
      <a-row>
        <a-col>
          <a-input-number v-model="shopFilter.npcId" placeholder="NPC ID" @keydown.enter="loadClick" />
          <a-input-number v-model="shopFilter.itemId" placeholder="物品 ID" @keydown.enter="loadClick" />
          <a-space>
            <a-button type="primary" status="success" @click="loadClick">搜索</a-button>
            <a-button @click="resetClick">重置</a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-table
        row-key="id"
        :loading="loading"
        :data="shopList"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column title="商店ID" data-index="shopId" :width="100" align="center" />
          <a-table-column title="NPC ID" data-index="npcId" :width="100" align="center" />
          <a-table-column title="NPC" data-index="npcName" :width="200" align="center" />
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 20px"
        :total="total" :page-size="shopFilter.pageSize" :current="shopFilter.pageNo"
        show-total show-jumper show-page-size
        @change="pageChange" @page-size-change="pageSizeChange"
      />
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import { getShopFilter, getShopList } from '@/api/npcShop';

  const { loading, setLoading } = useLoading(false);
  const total = ref(0);
  const shopFilter = ref<getShopFilter>({ pageNo: 1, pageSize: 20, onlyTotal: false, notPage: false });
  const shopList = ref<any[]>([]);

  const loadClick = async () => {
    setLoading(true);
    try {
      shopFilter.value.pageNo = 1;
      const { data } = await getShopList(shopFilter.value);
      shopList.value = data.records || [];
      total.value = data.totalRow || 0;
    } finally { setLoading(false); }
  };
  const resetClick = () => {
    shopFilter.value = { pageNo: 1, pageSize: 20, onlyTotal: false, notPage: false };
    loadClick();
  };
  const pageChange = (d: number) => { shopFilter.value.pageNo = d; loadClick(); };
  const pageSizeChange = (d: number) => { shopFilter.value.pageNo = 1; shopFilter.value.pageSize = d; loadClick(); };

  loadClick();
</script>

<script lang="ts">
  export default { name: 'NpcShop' };
</script>
