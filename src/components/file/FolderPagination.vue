<template>
  <el-pagination
      v-model:current-page="currentPage"
      :layout="layout"
      :page-size="pageSize"
      :total="total"
      background
      class="pagination"
      size="small"
  >
    <template v-if="total != 0" #default>共{{ total }}个文件</template>
  </el-pagination>
</template>
<style scoped></style>
<script lang="ts" setup>
import {computed} from "vue";
import {useMobileMediaQuery} from "../../utils";

defineProps<{ pageSize: number; total: number }>();
const currentPage = defineModel<number>();
const isMobile = useMobileMediaQuery();
const layout = computed(() =>
    isMobile.value ? "prev, pager, next" : "slot, prev, pager, next",
);
</script>
