<template>
  <el-pagination
    v-model:current-page="currentPage"
    :layout="layout"
    :pager-count="pagerCount"
    :page-size="pageSize"
    :total="total"
    background
    class="pagination"
    size="small" />
</template>
<style scoped>
.pagination {
  --pagination-button-size: 38px;
  --pagination-button-radius: 10px;
  --el-pagination-button-height: var(--pagination-button-size);
  display: flex;
  align-items: center;
}

.pagination :deep(.btn-prev),
.pagination :deep(.btn-next),
.pagination :deep(.el-pager li) {
  min-width: var(--pagination-button-size);
  height: var(--pagination-button-size);
  line-height: var(--pagination-button-size);
  border-radius: var(--pagination-button-radius);
}

@media screen and (max-width: 900px) {
  .pagination {
    --pagination-button-size: 34px;
    --pagination-button-radius: 9px;
  }
}

@media screen and (max-width: 576px) {
  .pagination {
    --pagination-button-size: 30px;
    --pagination-button-radius: 8px;
  }
}
</style>
<script lang="ts" setup>
import { computed } from "vue";
import { useMediaQuery } from "@vueuse/core";

defineProps<{ pageSize: number; total: number }>();
const currentPage = defineModel<number>();
const layout = "prev, pager, next";
const isCompactPager = useMediaQuery("(max-width: 900px)");
const pagerCount = computed(() => (isCompactPager.value ? 5 : 7));
</script>
