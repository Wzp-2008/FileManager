<script lang="ts" setup>
import {Bottom, Search, Top} from "@element-plus/icons-vue";
import {StorageSerializers, useLocalStorage} from "@vueuse/core";
import {isEqual} from "lodash-es";
import {computed, inject, ref, watch} from "vue";
import FileManagerSdk, {ROOT} from "../../sdk";
import type {NamedRawFile, RawFile, SortField,} from "../../sdk/entities";
import {type SortDefinition, useLessHeightQuery, useMobileMediaQuery, useMoreHeightQuery} from "../../utils.ts";
import SingleRawFileComponent from "./SingleRawFileComponent.vue";
import FolderPagination from "./FolderPagination.vue";

const {folder} = defineProps<{
  folder: RawFile;
}>();
const sdk = inject("sdk") as FileManagerSdk;
const emit = defineEmits<{
  (e: "parentChange", newParent: NamedRawFile): void;
}>();
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
const jumpInto = (file: NamedRawFile) => {
  if (!currentPageLoaded.value) return;
  searchContent.value = "";
  currentDisplaySearchContent.value = "";
  emit("parentChange", file);
  currentPageLoaded.value = false;
};
const jumpIntoParent = async () => {
  currentPageLoaded.value = false;
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
watch([page, () => folder, currentSort, currentDisplaySearchContent], load, {
  immediate: true,
  deep: true,
});
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
defineExpose({refresh: load});
</script>

<template>
  <div>
    <div class="file-list-actions">
      <div class="search-box">
        <el-input
            v-model="searchContent"
            :prefix-icon="Search"
            :size="isMobile ? 'default' : 'small'"
            placeholder="在此处输入搜索内容"
            @change="currentDisplaySearchContent = searchContent"/>
        <el-button
            v-show="!isMobile"
            :icon="Search"
            aria-label="搜索"
            type="primary"
            @click="currentDisplaySearchContent = searchContent"/>
      </div>
      <FolderPagination
          v-model="page"
          :page-size="pageSize"
          :total="total"
          class="pc-pagination"/>
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
            class="compact-remove action-header compact-remove"
            style="text-align: right">
          操作
        </div>
      </div>
      <SingleRawFileComponent
          v-if="folder.id !== -1 && currentDisplaySearchContent === ''"
          :row="parentRow"
          @click="jumpIntoParent()"/>
      <SingleRawFileComponent
          v-for="row in currentPageData"
          :row="row"
          @click="jumpInto(row)"
          @remove="deleteFile"/>
    </div>
    <div class="mobile-footer">
      <FolderPagination v-model="page" :page-size="pageSize" :total="total"/>
    </div>
  </div>
</template>

<style scoped>
.file-list {
  background-color: var(--el-color-info-light-9);
  box-shadow: var(--el-color-info-light-3) 0 0 8px;
  border-radius: 14px;
  padding: 10px 20px;
}

.table-head {
  display: grid;
  grid-template-columns: 2fr 0.5fr 0.8fr 0.5fr 1fr;
  padding: 10px;
}

.table-head > div:hover {
  cursor: pointer;
}

.file-list-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
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
  min-width: 240px;
  width: 15%;
  display: flex;
  gap: 2px;
}

@media screen and (max-width: 970px) {
  .file-list {
    padding: 6px;
    border-radius: 0;
    box-shadow: none;
    background-color: transparent;
  }

  .compact-remove {
    display: none;
  }
}

@media screen and (min-width: 576px) and (max-width: 970px) {
  .table-head {
    grid-template-columns: 1fr 1fr 1fr;
    padding: 10px 6px;
  }
}

/* Hidden Uploader */
@media screen and (max-width: 1300px) {
  .sort-field-uploader {
    display: none;
  }
}

@media screen and (min-width: 970px) and (max-width: 1300px) {
  .table-head {
    grid-template-columns: 2fr 0.5fr 0.8fr 1fr;
  }
}

@media screen and (max-width: 576px) {
  .table-head {
    grid-template-columns: 1fr 1fr;
    padding: 10px 6px;
  }

  .pagination {
    margin-right: 10px;
  }

  .file-list-actions {
    justify-content: center;
    margin-bottom: 0;
  }

  .search-box {
    padding: 0 20px;
    width: 100%;
  }

  .phone-remove {
    display: none;
  }
}

.mobile-footer {
  display: none;
}

@media screen and (max-width: 720px) {
  .mobile-footer {
    display: flex;
    justify-content: center;
  }

  .pc-pagination {
    display: none;
  }
}
</style>
