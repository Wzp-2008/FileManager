<script lang="ts" setup>
import { useEventListener, useSessionStorage } from "@vueuse/core";
import { ElMessage } from "element-plus";
import {
  computed,
  defineAsyncComponent,
  inject,
  onBeforeMount,
  provide,
  ref,
  type Ref,
  useTemplateRef,
} from "vue";
import FileManagerSdk, { ROOT } from "../sdk";
import type { NamedRawFile, RawFile } from "../sdk/entities";
import FileTableComponent from "./file/FileTableComponent.vue";

const FileComponent = defineAsyncComponent(
  () => import("./file/FileComponent.vue"),
);
const sdk = inject("sdk") as FileManagerSdk;
const currentFileObject = defineModel<RawFile>();
const pathList = ref<string[]>([]);
const currentPath = computed(() => "/" + pathList.value.join("/"));
const pendingRestorePage = ref<number | null>(null);
provide("pendingRestorePage", pendingRestorePage as Ref<number | null>);
const skipFileParentRestoreByHash = ref<boolean>(false);
provide(
  "skipFileParentRestoreByHash",
  skipFileParentRestoreByHash as Ref<boolean>,
);
const pageStack = useSessionStorage<number[]>("pages", []);
const updatePages = (pages: number[]) => {
  pageStack.value = pages;
  if (typeof window !== "undefined") {
    sessionStorage.setItem("pages", JSON.stringify(pages));
  }
};
provide("updatePages", updatePages as (pages: number[]) => void);
const pathLoad = ref<boolean>(false);
const hasError = ref<string>();
const folderRef = useTemplateRef("folder");
const updatePathByHash = async () => {
  pathLoad.value = false;
  location.hash ||= "#/";
  pathList.value = location.hash.replace("#", "").split("/").filter(Boolean);
  if (currentPath.value === "/") {
    currentFileObject.value = ROOT;
  } else {
    try {
      const file = await sdk.getFileByPath(
        decodeURIComponent(currentPath.value),
      );
      currentFileObject.value = file.data;
    } catch (err) {
      hasError.value = err as string;
      ElMessage.error(hasError.value);
      location.hash = "/";
    }
  }
  pathLoad.value = true;
};
onBeforeMount(updatePathByHash);
const whileParentChangeLoading = ref<boolean>(false);
const onParentChange = async (newFileObject: NamedRawFile) => {
  if (whileParentChangeLoading.value) return;
  whileParentChangeLoading.value = true;
  if (newFileObject.parent === currentFileObject.value!.id) {
    pathList.value.push(
      newFileObject.name +
        (newFileObject.type === "FILE" && newFileObject.parent
          ? "." + newFileObject.ext
          : ""),
    );
  } else if (currentFileObject.value!.parent === newFileObject.id) {
    pathList.value.pop();
  } else if (newFileObject.id == -1) {
    pathList.value = [];
  } else {
    const { data } = await sdk.getPathById(
      newFileObject.id,
      newFileObject.type,
    );
    pathList.value = data.split("/").filter(Boolean);
  }
  location.hash = "#" + currentPath.value;
  currentFileObject.value = newFileObject;
  whileParentChangeLoading.value = false;
};
useEventListener("hashchange", async () => {
  if (location.hash === "#" + currentPath.value) return;
  if (currentFileObject.value?.type === "FILE") {
    const targetPath =
      "/" + location.hash.replace("#", "").split("/").filter(Boolean).join("/");
    const parentPath =
      "/" + currentPath.value.split("/").filter(Boolean).slice(0, -1).join("/");
    if (targetPath === parentPath) {
      if (skipFileParentRestoreByHash.value) {
        skipFileParentRestoreByHash.value = false;
      } else if (pageStack.value.length > 0) {
        pendingRestorePage.value = pageStack.value[pageStack.value.length - 1];
        updatePages(pageStack.value.slice(0, -1));
      }
    }
  }
  await updatePathByHash();
});
const refresh = async () => {
  await folderRef.value?.refresh();
};
defineExpose({
  refresh,
});
</script>

<template>
  <div v-if="pathLoad">
    <FileComponent
      v-if="currentFileObject!.type === 'FILE'"
      :file="currentFileObject!" />
    <FileTableComponent
      v-else
      ref="folder"
      :folder="currentFileObject!"
      @parent-change="onParentChange" />
  </div>
</template>
