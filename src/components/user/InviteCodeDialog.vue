<template>
  <el-dialog v-model="show" append-to-body title="获取邀请码">
    <div v-if="loading" class="loading">
      <el-icon class="is-loading" size="large">
        <Loading />
      </el-icon>
      <el-text size="large">正在获取</el-text>
    </div>
    <div v-else class="invite-code-container">
      <div class="invite-code-title">邀请码有效期5分钟</div>
      <div class="invite-code">{{ code }}</div>
      <el-button type="success" @click="copy">点击复制</el-button>
    </div>
  </el-dialog>
</template>
<style scoped>
.loading {
  display: flex;
  align-items: center;
  gap: 0.5em;
}

.invite-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  user-select: none;
}

.invite-code {
  font-weight: bold;
  font-size: 1.5rem;
  color: black;
}

.invite-code-title {
  font-size: 1rem;
}

.el-button {
  margin-top: 0.5em;
}
</style>
<script lang="ts" setup>
import { Loading } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { inject, ref, watch } from "vue";
import type FileManagerSdk from "../../sdk";

const sdk = inject("sdk") as FileManagerSdk;
const show = defineModel({ default: false });
const loading = ref(false);
const code = ref("");
const load = async () => {
  loading.value = true;
  try {
    const resp = await sdk.getInviteCode().catch((e) => {
      ElMessage.error(e);
    });
    if (!resp) return;
    code.value = resp.data;
  } finally {
    loading.value = false;
  }
};
watch(
  show,
  (v) => {
    if (!v) return;
    load();
  },
  { immediate: true },
);
const copy = () =>
  navigator.clipboard
    .writeText(code.value)
    .then(() => ElMessage.success("复制成功"))
    .catch((e) => ElMessage.error("复制失败:" + e));
</script>
