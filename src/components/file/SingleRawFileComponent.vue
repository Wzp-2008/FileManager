<script lang="ts" setup>
import {Folder} from "@element-plus/icons-vue";
import type {NamedRawFile, User} from "../../sdk/entities";
import {canIDelete, humanitySize} from "../../sdk/utils.ts";
import {ElMessageBox} from "element-plus";
import {getClass} from "file-icons-js";
import mime from "mime-types";
import {inject} from "vue";
import type FileManagerSdk from "../../sdk";

const {row} = defineProps<{
  row: NamedRawFile;
}>();
const userInformation = inject("userInfo") as User | null;
const sdk = inject("sdk") as FileManagerSdk;
const emit = defineEmits<{ (e: "remove", self: NamedRawFile): void }>();
const downloadThis = () => {
  if (row.type === "FILE") {
    sdk.openFullDownloadLink(row.id);
  }
};
const removeThis = () => {
  ElMessageBox.confirm(
      `你确定要删除${row.type === "FILE" ? "文件" : "文件夹"}<span style="word-break: break-all;font-weight: bold">${row.name}${row.type === "FILE" ? "." + row.ext : ""}</span>吗？`,
      "删除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        dangerouslyUseHTMLString: true,
      },
  ).then(() => {
    emit("remove", row);
  });
};

const getIcon = () => {
  // 通过 MIME 类型获取扩展名
  const extension = mime.extension(row.mime);

  if (!extension) {
    return getClass(row.name + "." + row.ext!!) || "default-icon";
  }

  // 使用 file-icons-js 获取图标类名
  const iconClass = getClass(`dummy.${extension}`);

  return iconClass || "default-icon";
};
</script>

<template>
  <div class="file-entry">
    <div class="file-name">
      <el-icon class="file-icon">
        <Folder v-if="row.type === 'FOLDER'"/>
        <!--        <Document v-if="row.type === 'FILE'" />-->
        <div v-else :class="getIcon()" style="transform: translateX(2px)"></div>
      </el-icon>
      <span class="file-name-full-text"
      ><span class="file-name-text">{{ row.name }}</span
      ><span v-if="row.type === 'FILE' && row.ext">.{{ row.ext }}</span></span
      >
    </div>
    <div class="phone-remove" style="display: flex; align-items: center">
      {{ row.type === "FILE" ? humanitySize(row.size) : "-" }}
    </div>
    <div style="display: flex; align-items: center; min-width: 150px">
      {{ row.time }}
    </div>
    <div class="uploader">
      {{ row.ownerName }}
    </div>
    <div
        v-if="row.name !== '..' && row.owner !== -1"
        class="file-action compact-remove">
      <el-button
          v-if="canIDelete(userInformation, row)"
          type="danger"
          @click.stop="removeThis"
      >删除
      </el-button>
      <el-button
          v-if="row.type === 'FILE'"
          type="success"
          @click.stop="downloadThis"
      >下载
      </el-button>
      <el-button v-if="row.type === 'FILE'">详情</el-button>
    </div>
  </div>
</template>

<style scoped>
.file-icon {
  margin-right: 10px;
}

.file-entry {
  display: grid;
  grid-template-columns: 2fr 0.5fr 0.8fr 0.5fr 1fr;
  padding: 10px;
  margin-top: 10px;
  border-radius: 3px;
  transition: all 0.3s;
}

.file-name {
  display: flex;
  align-items: center;
  overflow: hidden;
}

.file-name-full-text {
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 10px;
}

.file-entry:first-child {
  margin-top: 0;
}

.file-entry:hover {
  /*box-shadow: gray 0 0 10px;*/
  transform: scale(1.005);
  cursor: pointer;
  background-color: var(--el-color-info-light-7);
}

.file-action {
  display: flex;
  justify-content: end;
  flex-wrap: wrap;
  gap: 10px;
  min-width: 202px;
}

.file-action .el-button {
  margin: 0 !important;
}

@media screen and (max-width: 970px) {
  .compact-remove {
    display: none !important;
  }
}

@media screen and (min-width: 576px) and (max-width: 970px) {
  .file-entry {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media screen and (max-width: 576px) {
  .file-entry {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .phone-remove {
    display: none !important;
  }
}

.uploader {
  display: flex;
  align-items: center;
  text-overflow: ellipsis;
}

/* Hidden Uploader */
@media screen and (max-width: 1300px) {
  .uploader {
    display: none;
  }
}

@media screen and (min-width: 970px) and (max-width: 1300px) {
  .file-entry {
    grid-template-columns: 2fr 0.5fr 0.8fr 1fr;
  }
}

.file-icon > div {
  width: 1em;
  height: 1em;
  font-style: normal;
}
</style>
