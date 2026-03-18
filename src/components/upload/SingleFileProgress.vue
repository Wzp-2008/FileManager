<script lang="ts" setup>
import type {UploadUserFile} from "element-plus";
import {CircleCheck, CircleClose, Close, Document,} from "@element-plus/icons-vue";

const emit = defineEmits<{
  (e: "removed", data: UploadUserFile): void;
}>();
const {data} = defineProps<{ data: UploadUserFile }>();
</script>

<template>
  <div class="container">
    <div class="file">
      <div class="file-name">
        <el-icon class="file-icon">
          <Document/>
        </el-icon>
        {{ data.name }}
      </div>
      <el-icon
          v-if="data.status === 'success'"
          class="success-icon"
          color="green">
        <CircleCheck/>
      </el-icon>
      <el-icon
          v-else-if="data.status === 'fail'"
          class="success-icon"
          color="red">
        <CircleClose/>
      </el-icon>
      <el-icon
          v-if="data.status === 'uploading'"
          class="remove-icon"
          @click="emit('removed', data)">
        <Close/>
      </el-icon>
    </div>
    <el-progress
        v-if="data.status === 'uploading' || data.status === 'ready'"
        :percentage="data.percentage"
        :stroke-width="4"/>
  </div>
</template>

<style scoped>
.container {
  padding: 10px;
}

.file {
  display: flex;
  justify-content: space-between;
  cursor: pointer;
  padding: 2px;
  transition: all 0.2s;
}

.file:hover {
  background-color: var(--el-fill-color-lighter);
  color: var(--el-color-primary);
}

.remove-icon {
  display: none;
}

.file-icon {
  color: var(--el-text-color-primary) !important;
}

.remove-icon {
  transition: all 0.2s;
  color: var(--el-text-color-primary) !important;
}

.remove-icon:hover {
  color: var(--el-color-primary) !important;
}

.file:hover .remove-icon {
  display: inline-flex;
}

.file:hover .success-icon {
  display: none;
}

.file-name {
  word-break: break-all;
}
</style>
