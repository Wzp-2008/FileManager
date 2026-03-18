<script lang="ts" setup>
import {ElMessage, type UploadUserFile} from "element-plus";
import FileManagerSdk from "../../sdk";
import type {FileObject, FolderObject} from "../../sdk/entities";
import {inject, reactive, ref} from "vue";
import {useEventListener} from "@vueuse/core";
import SingleFileProgress from "./SingleFileProgress.vue";
import {
  type BaseEntry,
  type BaseFileUploadTask,
  createUploadTask,
  FolderUploadTask,
  type ResponseFile,
  type ResponseFolder,
  useDataTransferItemHandler,
  useFileSelect,
  useFolderSelect,
} from "../../file.ts";
import {useMobileMediaQuery} from "../../utils.ts";

type MixedResponse = ResponseFile | ResponseFolder;
type TaskMixed = BaseFileUploadTask<MixedResponse>;

const customFiles = ref<TaskMixed[]>([]);
const isShow = defineModel<boolean>();
const {currentFolderId} = defineProps<{
  currentFolderId: number;
}>();
const sdk = inject("sdk") as FileManagerSdk;
const emit = defineEmits<{
  (e: "uploadedFile", file: FileObject | FolderObject | null): void;
}>();
const showMask = ref<boolean>(false);
const dataTransferHandler = useDataTransferItemHandler();
const isFileDataTransferItem = (item: DataTransferItem) => {
  return item.kind === "file";
};
const getFileDataTransfer = (dataTransfer: DataTransfer | null) => {
  if (!dataTransfer) return [];
  if (dataTransfer.items.length === 0) return [];
  return [...dataTransfer.items].filter(isFileDataTransferItem);
};
const onUploadComplete = (task: TaskMixed) => {
  setTimeout(() => {
    customFiles.value.splice(customFiles.value.indexOf(task), 1);
  }, 5000);
};
const onUploadSuccess = (task: TaskMixed) => {
  emit("uploadedFile", null);
  if (task instanceof FolderUploadTask) {
    if (task.abortController.signal.aborted) {
      ElMessage.warning(
          `文件夹${task.name}的上传被取消，但是已经上传部分文件，如需要删除请联系管理员处理！`,
      );
      return;
    }
  }
  ElMessage.success(`${task.name}上传成功！`);
};
const onUploadError = (task: TaskMixed) => {
  if (task instanceof FolderUploadTask) {
    ElMessage.error(`${task.name}上传失败！`);
  }
};
const uploadEntries = async (
    entries: BaseEntry[],
): Promise<MixedResponse[]> => {
  const tasks = entries.map((e) => reactive(createUploadTask(e)));
  customFiles.value.push(...tasks);
  isShow.value = true;
  const result = await Promise.allSettled(
      tasks.map(async (e) => {
        ElMessage.info(`开始上传${e.name}`);
        return e
            .start(sdk, currentFolderId)
            .then((data) => {
              onUploadSuccess(e);
              return data;
            })
            .catch((err) => {
              onUploadError(e);
              console.log(err);
              throw err;
            })
            .finally(() => onUploadComplete(e));
      }),
  );
  return result.filter((e) => e.status === "fulfilled").map((e) => e.value);
};
useEventListener("drop", (e) => {
  e.stopPropagation();
  e.preventDefault();
  showMask.value = false;
  const fileDataTransfer = getFileDataTransfer(e.dataTransfer);
  if (fileDataTransfer.length === 0) return;
  const result = Promise.allSettled(
      fileDataTransfer.map((e) => dataTransferHandler(e)),
  );
  result
      .then((res) =>
          res
              .filter((e) => e.status === "fulfilled")
              .map((e: PromiseFulfilledResult<BaseEntry>) => e.value),
      )
      .then(uploadEntries);
});
useEventListener("dragover", (e) => {
  e.preventDefault();
  if (showMask.value) return;
  const fileDataTransfer = getFileDataTransfer(e.dataTransfer);
  if (fileDataTransfer.length === 0) return;
  showMask.value = true;
});
useEventListener("dragleave", (e) => {
  e.preventDefault();
  const to = e.relatedTarget;
  if (!to) {
    showMask.value = false;
  }
});
const onFileRemoved = (removedFile: UploadUserFile) => {
  const task = removedFile as TaskMixed;
  task.cancel();
  ElMessage.warning(`文件${task.name}的上传已被取消！`);
  customFiles.value.splice(customFiles.value.indexOf(task), 1);
};

const getFiles = useFileSelect();
const getFolders = useFolderSelect();

const selectFile = () => {
  getFiles().then(uploadEntries);
};
const selectFolder = () => {
  getFolders().then(uploadEntries);
};
const isPhone = useMobileMediaQuery();
</script>

<template>
  <el-drawer
      v-model="isShow"
      :size="isPhone ? '80%' : '30%'"
      direction="rtl"
      title="上传列表">
    <el-button type="primary" @click="selectFile">上传文件</el-button>
    <el-button type="warning" @click="selectFolder"
    >上传文件夹（测试）
    </el-button>
    <div v-for="data of customFiles">
      <SingleFileProgress :data="data" @removed="onFileRemoved"/>
    </div>
    <div>
      <span style="color: gray; font-size: 0.75em"
      >请上传文件名小于255长度的文件<br>不支持上传没有文件名只有扩展名（如`.abc`）的文件</span
      >
    </div>
  </el-drawer>
  <teleport v-if="showMask" to="html">
    <div class="mask">
      <div>将文件或文件夹拖放到这里以上传</div>
      <div class="tips">
        请注意，文件夹上传功能正在测试，可能出现bug，如有请报告给wzp谢谢
      </div>
    </div>
  </teleport>
</template>

<style scoped>
.mask {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.75);
  pointer-events: none;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 5rem;
  color: var(--el-color-white);
}

.tips {
  font-size: 1.2rem;
  color: var(--el-color-warning);
  font-weight: bold;
}
</style>
