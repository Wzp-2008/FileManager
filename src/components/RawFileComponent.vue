<script lang="ts" setup>
import {useEventListener} from "@vueuse/core";
import {ElMessage} from "element-plus";
import {computed, defineAsyncComponent, inject, onBeforeMount, ref, useTemplateRef} from "vue";
import FileManagerSdk, {ROOT} from "../sdk";
import type {NamedRawFile, RawFile} from "../sdk/entities";
import FolderComponent from "./file/FolderComponent.vue";

const FileComponent = defineAsyncComponent(() => import("./file/FileComponent.vue"))
const sdk = inject("sdk") as FileManagerSdk;
const currentFileObject = defineModel<RawFile>();
const pathList = ref<string[]>([]);
const currentPath = computed(() => "/" + pathList.value.join("/"));
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
    const {data} = await sdk.getPathById(
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
        :file="currentFileObject!"/>
    <FolderComponent
        v-else
        ref="folder"
        :folder="currentFileObject!"
        @parent-change="onParentChange"/>
  </div>
</template>
