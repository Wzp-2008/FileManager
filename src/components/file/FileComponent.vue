<script lang="ts" setup>
import type {FileObject, RawFile, User} from "../../sdk/entities";
import FileManagerSdk from "../../sdk";
import {Back, Document} from "@element-plus/icons-vue";
import {inject, onBeforeMount, ref} from "vue";
import {canIDelete, humanitySize} from "../../sdk/utils.ts";
import {ElMessage, ElMessageBox} from "element-plus";
import {useCompactMediaQuery, useFullSha512ValueQuery} from "../../utils.ts";

const {file} = defineProps<{
  file: RawFile;
}>();
const sdk = inject("sdk") as FileManagerSdk;
const userInformation = inject("userInfo") as User | null;
const fileDetailInformation = ref<FileObject>();
const ownerInformation = ref<User>();
onBeforeMount(async () => {
  const response = await sdk.getFileDetail(file.id);
  fileDetailInformation.value = response.data;
  const owner = await sdk.getUser(file.owner);
  ownerInformation.value = owner.data;
});
const copyLinks = () => {
  navigator.clipboard.writeText(window.location.href);
  ElMessage.success("复制成功！");
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
  location.hash = location.hash.slice(0, location.hash.lastIndexOf("/"));
};
const isPhone = useCompactMediaQuery();
const isFullHash = useFullSha512ValueQuery();
const copyHash = () => {
  if (navigator.clipboard?.writeText) {
    navigator.clipboard
        .writeText(fileDetailInformation.value!.hash)
        .then(() => ElMessage.success("复制成功"))
        .catch((e) => ElMessage.error("复制失败:" + e));
  } else {
    ElMessage.error("无法调用剪切板，请检查是否使用https部署/访问本站")
  }
};
</script>

<template>
  <div
      v-loading="!fileDetailInformation || !ownerInformation"
      class="container">
    <div v-if="!!fileDetailInformation && !!ownerInformation">
      <div class="back-icon-div">
        <div>
          <el-icon class="back-icon" size="48" @click="back">
            <Back/>
          </el-icon>
        </div>
      </div>
      <el-icon size="200">
        <Document/>
      </el-icon>
      <div class="file-name">
        {{
          fileDetailInformation.name +
          (fileDetailInformation.ext ? "." + fileDetailInformation.ext : "")
        }}
      </div>
      <div>{{ humanitySize(fileDetailInformation.size) }}</div>
      <div>
        由{{ ownerInformation.name }}在{{
          fileDetailInformation.uploadTime
        }}上传
      </div>
      <div v-if="!isPhone">
        <div v-if="isFullHash">
          文件SHA512校验值：{{ fileDetailInformation.hash }}
        </div>
        <div v-else @click="copyHash">
          SHA512（点击复制）：{{ fileDetailInformation.hash.substring(0, 6) }} ......
          {{ fileDetailInformation.hash.substring(fileDetailInformation.hash.length - 6) }}
        </div>
      </div>
    </div>
  </div>
  <div class="actions">
    <el-button type="warning" @click="copyLinks">复制链接</el-button>
    <el-button type="success" @click="download">下载</el-button>
    <el-button
        v-if="canIDelete(userInformation, { type: 'FILE', owner: file.owner })"
        type="danger"
        @click="deleteFile"
    >删除
    </el-button>
  </div>
</template>

<style scoped>
.container {
  background-color: var(--el-color-info-light-9);
  box-shadow: var(--el-color-info-light-3) 0 0 8px;
  border-radius: 14px;
  padding: 10px 20px;
}

.container > div {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
}

.container > div > div {
  display: flex;
  justify-content: center;
  align-items: center;
}

.actions {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}

.back-icon {
  cursor: pointer;
  border-radius: 10px;
  transition: transform 250ms ease-out,
  background-color 100ms linear;
}

.back-icon:hover {
  background-color: rgba(0, 0, 0, 0.25);
  transform: scale(1.05);
}

.back-icon-div {
  justify-content: flex-start !important;
  width: 100%;
}

@media screen and (max-width: 576px) {
  .container {
    padding: 0;
    background-color: transparent;
    border-radius: 0;
    box-shadow: none;
  }
}

.file-name {
  padding: 0 10px;
  word-break: break-all;
  text-align: center;
}
</style>
