<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.cashShop')">
      <a-row style="margin-bottom: 16px">
        <a-col :span="8">
          <a-select v-model="selectedCategory" :placeholder="$t('button.select')" @change="loadCommodities">
            <a-option v-for="cat in subCategoryList" :key="cat.id" :value="cat.id" :label="cat.name" />
          </a-select>
        </a-col>
        <a-col :span="4" :offset="1">
          <a-input-number v-model="searchItemId" placeholder="道具ID" :min="0" style="width:100%" />
        </a-col>
        <a-col :span="4" :offset="1">
          <a-select v-model="searchOnSale" placeholder="出售状态" allow-clear>
            <a-option :value="1">上架</a-option>
            <a-option :value="0">下架</a-option>
          </a-select>
        </a-col>
        <a-col :span="4" :offset="1">
          <a-space>
            <a-button type="primary" @click="loadCommodities">搜索</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-table
        row-key="id"
        :loading="loading"
        :data="commodityList"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="60" align="center" />
          <a-table-column title="道具ID" data-index="itemID" :width="80" align="center" />
          <a-table-column title="价格" data-index="price" :width="80" align="center" />
          <a-table-column title="原价" data-index="oldPrice" :width="80" align="center" />
          <a-table-column title="数量" data-index="bundleQuantity" :width="60" align="center" />
          <a-table-column title="天数" data-index="availableDays" :width="60" align="center" />
          <a-table-column title="上架" data-index="onSale" :width="60" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.onSale" color="green">是</a-tag>
              <a-tag v-else color="gray">否</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="MP购买" data-index="buyableWithMaplePoints" :width="80" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.buyableWithMaplePoints" color="blue">可</a-tag>
              <a-tag v-else color="gray">否</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="120" align="center">
            <template #cell="{ record }">
              <a-popconfirm
                :content="record.onSale ? '确定下架？' : '确定上架？'"
                @ok="toggleSale(record)"
              >
                <a-button type="text" size="mini" :status="record.onSale ? 'danger' : 'success'">
                  {{ record.onSale ? '下架' : '上架' }}
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { getAllCategoryList, getCommodityByCategory, onSale, offSale } from '@/api/cashShop';
  import useLoading from '@/hooks/loading';
  import { Message } from '@arco-design/web-vue';

  const { loading, setLoading } = useLoading(false);
  const topCategoryList = ref<any[]>([]);
  const subCategoryList = ref<any[]>([]);
  const commodityList = ref<any[]>([]);
  const selectedCategory = ref<number | undefined>(undefined);
  const searchItemId = ref<number | undefined>(undefined);
  const searchOnSale = ref<number | undefined>(undefined);

  onMounted(async () => {
    await loadCategories();
  });

  const loadCategories = async () => {
    const { data } = await getAllCategoryList();
    topCategoryList.value = data.filter((c: any) => c.parent === 0);
    const subs = data.filter((c: any) => c.parent > 0);
    if (subs.length > 0) {
      subCategoryList.value = subs;
      selectedCategory.value = subs[0].id;
      await loadCommodities();
    }
  };

  const loadCommodities = async () => {
    if (!selectedCategory.value && !searchItemId.value) return;
    setLoading(true);
    try {
      const { data } = await getCommodityByCategory({
        id: 0,
        subId: selectedCategory.value || 0,
        pageNo: 1,
        itemId: searchItemId.value,
        onSale: searchOnSale.value,
      });
      commodityList.value = data.records || [];
    } finally {
      setLoading(false);
    }
  };

  const resetSearch = () => {
    searchItemId.value = undefined;
    searchOnSale.value = undefined;
    selectedCategory.value = subCategoryList.value[0]?.id;
    loadCommodities();
  };

  const toggleSale = async (record: any) => {
    try {
      setLoading(true);
      if (record.onSale) {
        await offSale({ sn: record.id });
      } else {
        await onSale({ sn: record.id });
      }
      Message.success('操作成功');
      await loadCommodities();
    } catch {
      Message.error('操作失败');
    } finally {
      setLoading(false);
    }
  };
</script>

<script lang="ts">
  export default { name: 'CashShop' };
</script>
