<script lang="ts" setup>
import {Bottom, Search, Top} from "@element-plus/icons-vue";
import {StorageSerializers, useLocalStorage, useSessionStorage} from "@vueuse/core";
import {isEqual} from "lodash-es";
import {computed, inject, ref, type Ref, watch} from "vue";
import FileManagerSdk, {ROOT} from "../../sdk";
import type {NamedRawFile, RawFile, SortField,} from "../../sdk/entities";
import {type SortDefinition, useLessHeightQuery, useMobileMediaQuery, useMoreHeightQuery} from "../../utils.ts";
import SingleRawFileComponent from "./SingleRawFileComponent.vue";
import FolderPagination from "./FolderPagination.vue";

const {folder} = defineProps<{
  folder: RawFile;
}>();
const sdk = inject("sdk") as FileManagerSdk;
const pendingRestorePage = inject("pendingRestorePage") as Ref<number | null>;
const updatePages = inject("updatePages") as (pages: number[]) => void;
const emit = defineEmits<{
  (e: "parentChange", newParent: NamedRawFile): void;
}>();
const pageStack = useSessionStorage<number[]>("pages", []);
const searchContent = ref<string>("");
const currentDisplaySearchContent = ref<string>("");
const page = ref<number>(1);
const isLessHeight = useLessHeightQuery();
const isMoreHeight = useMoreHeightQuery();
const isMobile = useMobileMediaQuery();
const pageSize = ref<number>(isMobile.value ? isLessHeight.value ? 7 : isMoreHeight.value ? 12 : 10 : 10);
const getCurrentPage = async () => {
  const sortValue = currentSort.value;
  const {data} = await sdk.getFolderFilesPager(
      pageSize.value,
      page.value,
      folder.id,
      currentDisplaySearchContent.value,
      sortValue?.sort,
      sortValue?.reverse,
  );
  currentPageData.value = data.data;
  total.value = data.total;
};
const load = async (newValue: unknown = null, oldValue: unknown = {}) => {
  if (isEqual(newValue, oldValue)) return;
  currentPageLoaded.value = false;
  await getCurrentPage();
  currentPageLoaded.value = true;
};
const total = ref<number>(0);
const currentPageData = ref<NamedRawFile[]>();
const currentPageLoaded = ref<boolean>(false);
const recordCurrentPage = () => {
  updatePages([...pageStack.value, page.value]);
};
const popRecordedPage = (): number => {
  if (pageStack.value.length === 0) return 1;
  const restorePage = pageStack.value[pageStack.value.length - 1];
  updatePages(pageStack.value.slice(0, -1));
  return restorePage;
};
const prepareRestorePage = () => {
  pendingRestorePage.value = popRecordedPage();
};
const jumpInto = (file: NamedRawFile) => {
  if (!currentPageLoaded.value) return;
  recordCurrentPage();
  searchContent.value = "";
  currentDisplaySearchContent.value = "";
  emit("parentChange", file);
  currentPageLoaded.value = false;
};
const jumpIntoParent = async () => {
  currentPageLoaded.value = false;
  prepareRestorePage();
  if (folder.parent == -1) {
    emit("parentChange", ROOT);
    return;
  }
  const detail = await sdk.getFolderInformation(folder.parent);
  emit("parentChange", detail.data);
};
const parentRow = computed<NamedRawFile>(() => ({
  id: folder.parent,
  owner: -1,
  ownerName: "",
  type: "FOLDER",
  size: -1,
  time: "",
  mime: "folder",
  parent: folder.parent,
  name: "...",
}));
const deleteFile = async (file: NamedRawFile) => {
  currentPageLoaded.value = false;
  await sdk.deleteFile(file.id, file.type);
  await load();
};
const currentSort = useLocalStorage<SortDefinition | null>("sort", null, {
  serializer: StorageSerializers.object,
});
watch([page, currentSort, currentDisplaySearchContent], load, {
  deep: true,
});
watch(
    () => folder.id,
    (newId, oldId) => {
      if (newId === oldId) return;
      if (pendingRestorePage.value !== null) {
        const restorePage = pendingRestorePage.value;
        pendingRestorePage.value = null;
        if (page.value !== restorePage) {
          page.value = restorePage;
          return;
        }
      } else if (page.value !== 1) {
        page.value = 1;
        return;
      }
      void load(newId, oldId);
    },
    {immediate: true},
);
const changeSort = (sort: SortField) => {
  const {value} = currentSort;
  if (value && sort === value.sort) {
    if (value.reverse) {
      currentSort.value = null;
      return;
    }
    value.reverse = true;
    load();
    return;
  }
  currentSort.value = {sort, reverse: false};
};
const applySearch = () => {
  page.value = 1;
  currentDisplaySearchContent.value = searchContent.value.trim();
};
defineExpose({refresh: load});
</script>

<template>
  <div>
    <div class="file-list-actions">
      <div class="toolbar-left">
        <div class="search-box">
          <el-input
              v-model="searchContent"
              :prefix-icon="Search"
              :size="isMobile ? 'default' : 'small'"
              clearable
              placeholder="搜索文件名或扩展名"
              @change="applySearch"
              @clear="applySearch"
              @keyup.enter="applySearch">
            <template #append>
              <el-button
                  :icon="Search"
                  :size="isMobile ? 'default' : 'small'"
                  aria-label="搜索"
                  type="primary"
                  @click="applySearch">
                <span v-if="!isMobile">搜索</span>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
      <div class="toolbar-right pc-toolbar-right">
        <span class="result-count">共 {{ total }} 个文件</span>
        <FolderPagination
            v-model="page"
            :page-size="pageSize"
            :total="total"/>
      </div>
    </div>
    <div v-loading="!currentPageLoaded" class="file-list">
      <div class="table-head">
        <div class="sort-field-name" @click="changeSort('NAME')">
          文件名
          <el-icon v-if="currentSort?.sort === 'NAME'">
            <Bottom v-if="currentSort.reverse"/>
            <Top v-else/>
          </el-icon>
        </div>
        <div class="sort-field-name phone-remove" @click="changeSort('SIZE')">
          大小
          <el-icon>
            <el-icon v-if="currentSort?.sort === 'SIZE'">
              <Bottom v-if="currentSort.reverse"/>
              <Top v-else/>
            </el-icon>
          </el-icon>
        </div>
        <div
            class="sort-field-name"
            style="min-width: 150px"
            @click="changeSort('TIME')">
          上传时间
          <el-icon>
            <el-icon v-if="currentSort?.sort === 'TIME'">
              <Bottom v-if="currentSort.reverse"/>
              <Top v-else/>
            </el-icon>
          </el-icon>
        </div>
        <div
            class="sort-field-name sort-field-uploader"
            @click="changeSort('UPLOADER')">
          上传者
          <el-icon>
            <el-icon v-if="currentSort?.sort === 'UPLOADER'">
              <Bottom v-if="currentSort.reverse"/>
              <Top v-else/>
            </el-icon>
          </el-icon>
        </div>
        <div
            class="compact-remove action-header"
            style="text-align: right">
          操作
        </div>
      </div>
      <SingleRawFileComponent
          v-if="folder.id !== -1 && currentDisplaySearchContent === ''"
          :row="parentRow"
          @open="jumpIntoParent()"/>
      <SingleRawFileComponent
          v-for="row in currentPageData"
          :key="`${row.type}-${row.id}`"
          :row="row"
          @open="jumpInto(row)"
          @remove="deleteFile"/>
    </div>
    <div class="mobile-footer">
      <FolderPagination v-model="page" :page-size="pageSize" :total="total"/>
    </div>
  </div>
</template>

<style scoped>
.file-list {
  --file-list-padding: 10px 20px;
  --file-list-radius: 14px;
  --file-list-background: var(--el-color-info-light-9);
  --file-list-shadow: var(--el-color-info-light-3) 0 0 8px;
  background-color: var(--file-list-background);
  box-shadow: var(--file-list-shadow);
  border-radius: var(--file-list-radius);
  padding: var(--file-list-padding);
}

.table-head {
  --table-columns: 2fr 0.5fr 0.8fr 0.5fr 1fr;
  --table-head-padding: 10px;
  display: grid;
  grid-template-columns: var(--table-columns);
  padding: var(--table-head-padding);
}

.table-head > div:hover {
  cursor: pointer;
}

.file-list-actions {
  --toolbar-gap: 16px;
  --toolbar-padding: 14px 18px;
  --toolbar-radius: 18px;
  --toolbar-margin-bottom: 18px;
  --toolbar-margin-inline: 0;
  --search-max-width: 480px;
  --toolbar-left-width: auto;
  --search-button-padding-inline: 18px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: var(--toolbar-gap);
  margin: 0 var(--toolbar-margin-inline) var(--toolbar-margin-bottom);
  padding: var(--toolbar-padding);
  border-radius: var(--toolbar-radius);
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid var(--el-border-color-lighter);
  box-shadow:
      0 10px 24px rgba(15, 23, 42, 0.06),
      0 1px 2px rgba(15, 23, 42, 0.04);
}

.toolbar-left {
  flex: 1 1 520px;
  min-width: 0;
  width: var(--toolbar-left-width);
}

.toolbar-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
  flex-wrap: wrap;
  min-width: 0;
  margin-left: auto;
}

.sort-field-name {
  display: flex;
  justify-content: start;
  align-items: center;
}

.sort-field-name > .el-icon {
  margin-top: 0.25em;
}

.action-header {
  cursor: default !important;
}

.search-box {
  width: min(100%, var(--search-max-width));
}

.search-box :deep(.el-input-group__append) {
  padding: 0;
  border-radius: 0 12px 12px 0;
  overflow: hidden;
  background: transparent;
}

.search-box :deep(.el-input) {
  --el-input-height: 42px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 12px 0 0 12px;
  padding-left: 12px;
}

.search-box :deep(.el-input-group__append .el-button) {
  margin: 0 !important;
  height: 100%;
  border-radius: 0 12px 12px 0;
  padding-inline: var(--search-button-padding-inline);
  font-weight: 600;
}

.result-count {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 999px;
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
  white-space: nowrap;
}

.mobile-footer {
  display: none;
  align-items: center;
  justify-content: center;
  margin: 12px 12px 0;
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid var(--el-border-color-lighter);
  box-shadow:
      0 8px 20px rgba(15, 23, 42, 0.05),
      0 1px 2px rgba(15, 23, 42, 0.04);
}

@media screen and (max-width: 1300px) {
  .table-head {
    --table-columns: 2fr 0.5fr 0.8fr 1fr;
  }

  .sort-field-uploader {
    display: none;
  }
}

@media screen and (max-width: 1280px) {
  .file-list-actions {
    --toolbar-gap: 12px;
    --search-max-width: 360px;
    --toolbar-left-width: min(100%, 360px);
    flex-wrap: nowrap;
  }

  .toolbar-left {
    flex: 0 1 auto;
  }

  .toolbar-right {
    gap: 12px;
  }

  .toolbar-right .result-count {
    display: none;
  }
}

@media screen and (max-width: 970px) {
  .file-list {
    --file-list-padding: 6px;
    --file-list-radius: 0;
    --file-list-background: transparent;
    --file-list-shadow: none;
  }

  .table-head {
    --table-columns: 1fr 1fr 1fr;
    --table-head-padding: 10px 6px;
  }

  .compact-remove {
    display: none;
  }
}

@media screen and (max-width: 820px) {
  .file-list-actions {
    --toolbar-padding: 12px 14px;
    --toolbar-radius: 16px;
    --toolbar-margin-bottom: 10px;
    --toolbar-margin-inline: 12px;
    --search-max-width: 100%;
    --toolbar-left-width: 100%;
    --search-button-padding-inline: 12px;
  }

  .toolbar-left {
    flex: 1 1 100%;
  }

  .pc-toolbar-right {
    display: none;
  }

  .mobile-footer {
    display: flex;
  }
}

@media screen and (max-width: 576px) {
  .file-list-actions {
    --toolbar-padding: 12px;
  }

  .table-head {
    --table-columns: 1fr 1fr;
  }

  .phone-remove {
    display: none;
  }
}
</style>
