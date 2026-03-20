<script lang="ts" setup>
import type { FileObject, RawFile, User } from "../../sdk/entities";
import FileManagerSdk from "../../sdk";
import { Back } from "@element-plus/icons-vue";
import { computed, inject, onBeforeMount, ref, type Ref } from "vue";
import { canIDelete, humanitySize } from "../../sdk/utils.ts";
import { ElMessage, ElMessageBox } from "element-plus";
import { useSessionStorage } from "@vueuse/core";
import { useCompactMediaQuery } from "../../utils.ts";

const { file } = defineProps<{
  file: RawFile;
}>();
const sdk = inject("sdk") as FileManagerSdk;
const userInformation = inject("userInfo") as Ref<User | null>;
const pendingRestorePage = inject("pendingRestorePage") as Ref<number | null>;
const skipFileParentRestoreByHash = inject(
  "skipFileParentRestoreByHash",
) as Ref<boolean>;
const updatePages = inject("updatePages") as (pages: number[]) => void;
const fileDetailInformation = ref<FileObject>();
const ownerInformation = ref<User>();
const pageStack = useSessionStorage<number[]>("pages", []);
onBeforeMount(async () => {
  const response = await sdk.getFileDetail(file.id);
  fileDetailInformation.value = response.data;
  const owner = await sdk.getUser(file.owner);
  ownerInformation.value = owner.data;
});
const writeToClipboard = async (value: string, message: string) => {
  if (!navigator.clipboard?.writeText) {
    ElMessage.error("无法调用剪贴板，请检查是否使用 https 访问本站");
    return;
  }
  try {
    await navigator.clipboard.writeText(value);
    ElMessage.success(message);
  } catch (e) {
    ElMessage.error("复制失败：" + e);
  }
};
const copyLinks = () => {
  void writeToClipboard(window.location.href, "链接已复制");
};
const download = () => {
  sdk.openFullDownloadLink(fileDetailInformation.value!.id);
};
const deleteFile = () => {
  ElMessageBox.confirm(
    `你确定要删除文件<span style="word-break: break-all;font-weight: bold">${file.name}.${file.ext!!}</span>吗？`,
    "删除",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
      dangerouslyUseHTMLString: true,
    },
  ).then(() => {
    sdk.deleteFile(file.id, "FILE").then(() => {
      ElMessage.success("删除成功！");
      back();
    });
  });
};
const back = () => {
  if (pageStack.value.length > 0) {
    pendingRestorePage.value = pageStack.value[pageStack.value.length - 1];
    updatePages(pageStack.value.slice(0, -1));
  }
  skipFileParentRestoreByHash.value = true;
  const currentHash = location.hash || "#/";
  const lastSlashIndex = currentHash.lastIndexOf("/");
  location.hash =
    lastSlashIndex <= 1 ? "#/" : currentHash.slice(0, lastSlashIndex);
};
const isPhone = useCompactMediaQuery();
const copyHash = () => {
  if (!fileDetailInformation.value) {
    return;
  }
  void writeToClipboard(fileDetailInformation.value.hash, "SHA512 已复制");
};
const fullFileName = computed(() => {
  const detail = fileDetailInformation.value;
  if (!detail) {
    return "";
  }
  return detail.name + (detail.ext ? "." + detail.ext : "");
});
const fileTypeLabel = computed(() => {
  const detail = fileDetailInformation.value;
  if (!detail) {
    return "";
  }
  return detail.ext ? detail.ext.toUpperCase() : "未知类型";
});
const summaryPills = computed(() => {
  const detail = fileDetailInformation.value;
  if (!detail) {
    return [];
  }
  return [humanitySize(detail.size), fileTypeLabel.value];
});
const canDelete = computed(() =>
  canIDelete(userInformation?.value ?? null, {
    type: "FILE",
    owner: file.owner,
  }),
);
</script>

<template>
  <section
    v-loading="!fileDetailInformation || !ownerInformation"
    class="detail-shell">
    <article
      v-if="!!fileDetailInformation && !!ownerInformation"
      class="detail-card">
      <div class="detail-toolbar">
        <button class="back-button" type="button" @click="back">
          <el-icon size="30">
            <Back />
          </el-icon>
          <span>返回上一级</span>
        </button>
        <div class="toolbar-actions">
          <el-button
            :size="isPhone ? 'small' : 'default'"
            type="warning"
            @click="copyLinks">
            复制链接
          </el-button>
          <el-button
            :size="isPhone ? 'small' : 'default'"
            type="success"
            @click="download">
            下载
          </el-button>
        </div>
      </div>
      <div class="detail-hero">
        <div class="detail-summary">
          <div class="detail-badge">文件详情</div>
          <h2 :title="fullFileName" class="file-name">{{ fullFileName }}</h2>
          <p class="detail-caption">
            由 {{ ownerInformation.name }} 于
            {{ fileDetailInformation.uploadTime }} 上传
          </p>
          <div class="meta-pills">
            <span v-for="pill in summaryPills" :key="pill" class="meta-pill">
              {{ pill }}
            </span>
          </div>
        </div>
      </div>
      <div class="hash-panel">
        <div class="hash-panel-head">
          <div>
            <div class="hash-title">SHA512 校验值</div>
            <div class="hash-caption">可用于校验下载文件完整性</div>
          </div>
          <el-button
            plain
            size="small"
            class="hash-copy-button"
            @click="copyHash">
            复制
          </el-button>
        </div>
        <code class="hash-value">{{ fileDetailInformation.hash }}</code>
      </div>
      <div v-if="canDelete" class="actions">
        <el-button type="danger" @click="deleteFile">删除 </el-button>
      </div>
    </article>
  </section>
</template>

<style scoped>
.detail-shell {
  padding: 4px 0 24px;
}

.detail-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--el-border-color-lighter);
  box-shadow:
    0 10px 24px rgba(15, 23, 42, 0.08),
    0 1px 2px rgba(15, 23, 42, 0.04);
  border-radius: 24px;
  padding: 24px;
}

.detail-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-actions .el-button {
  margin: 0 !important;
}

.back-button {
  border: 0;
  background: transparent;
  color: var(--el-text-color-primary);
  border-radius: 999px;
  padding: 10px 14px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition:
    transform 250ms ease,
    background-color 250ms ease;
}

.back-button:hover {
  background-color: var(--el-color-primary-light-9);
}

.detail-hero {
  display: block;
}

.detail-summary {
  min-width: 0;
  max-width: 100%;
}

.detail-badge {
  display: inline-flex;
  align-items: center;
  padding: 0 12px;
  height: 32px;
  border-radius: 999px;
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-size: 0.95rem;
  font-weight: 600;
}

.file-name {
  margin: 18px 0 0;
  font-size: clamp(1.75rem, 2.6vw, 2.7rem);
  line-height: 1.15;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.detail-caption {
  margin: 12px 0 0;
  color: var(--el-text-color-regular);
  font-size: 1rem;
}

.meta-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  padding: 9px 14px;
  border-radius: 999px;
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-primary);
  font-size: 0.95rem;
}

.hash-panel {
  margin-top: 20px;
  border-radius: 22px;
  padding: 18px 20px;
  background: var(--el-color-info-light-9);
  border: 1px solid var(--el-border-color-lighter);
  color: var(--el-text-color-primary);
}

.hash-panel-head {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 16px;
}

.hash-title {
  font-size: 1rem;
  font-weight: 600;
}

.hash-caption {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
}

.hash-copy-button {
  flex: none;
}

.hash-value {
  margin-top: 16px;
  display: block;
  font-family: "SFMono-Regular", "Consolas", "Liberation Mono", monospace;
  line-height: 1.75;
  font-size: 0.95rem;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.actions {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 12px;
}

.actions .el-button {
  margin: 0 !important;
}

@media screen and (max-width: 960px) {
  .actions {
    justify-content: stretch;
  }

  .actions .el-button {
    flex: 1 1 160px;
  }
}

@media screen and (max-width: 576px) {
  .detail-shell {
    padding: 0;
  }

  .detail-card {
    padding: 14px 0 24px;
    background: transparent;
    box-shadow: none;
    border-radius: 0;
  }

  .detail-toolbar,
  .detail-summary,
  .hash-panel,
  .actions {
    margin-inline: 12px;
  }

  .detail-toolbar {
    margin-bottom: 12px;
  }

  .back-button {
    padding: 8px 10px;
  }

  .toolbar-actions {
    gap: 8px;
  }

  .detail-badge {
    height: 28px;
    padding: 0 10px;
    font-size: 0.82rem;
  }

  .file-name {
    margin-top: 10px;
    font-size: 1.05rem;
    line-height: 1.3;
  }

  .detail-caption {
    margin-top: 8px;
    font-size: 0.95rem;
  }

  .meta-pills {
    margin-top: 12px;
    gap: 8px;
  }

  .meta-pill {
    padding: 8px 12px;
    font-size: 0.9rem;
  }

  .hash-panel-head {
    gap: 10px;
  }

  .hash-panel {
    margin-top: 18px;
    padding: 16px;
  }

  .hash-copy-button {
    min-width: 84px;
    margin-left: auto;
  }

  .hash-value {
    margin-top: 12px;
    font-size: 0.84rem;
    line-height: 1.65;
  }

  .actions {
    justify-content: flex-end;
    margin-top: 18px;
    gap: 10px;
  }

  .actions .el-button {
    flex: 0 0 auto;
  }
}

@media screen and (max-width: 420px) {
  .detail-toolbar {
    align-items: flex-start;
  }

  .toolbar-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-actions .el-button {
    width: 100%;
  }
}
</style>
