<script setup lang="ts">
  import {onMounted, ref} from "vue";
  import {Search, UploadFilled, Moon, Sunny} from "@element-plus/icons-vue"
  import {FileObject} from "./entities/FileObject";
  import {useDark, useToggle} from "@vueuse/core";
  import {getFileCount, getFiles} from "./requester/Requester";
  import {ElMessage} from "element-plus";

  const searchContent = ref<string>("");
  const searchPlaceHolder = ref<string>("请输入要搜索的内容");
  const searchType = ref<number>(0);
  const searchContentType = ref<string>("text");
  const nowShowData = ref<Array<FileObject>>([]);
  const isAdmin = ref<boolean>(false);
  const isDark = useDark();
  const toggleDark = useToggle(isDark);
  const colorTheme = ref<boolean>(isDark.value);
  const totalFileCount = ref<number>(0);
  const nowPage = ref<number>(1);

  const onSearchTypeChange = (now: number) => {
    switch (now){
      case 0:
        searchPlaceHolder.value = "请输入要搜索的内容的名称";
        searchContentType.value = "text";
        break;
      case 1:
        searchPlaceHolder.value = "请输入要搜索的文件的id";
        searchContentType.value = "number";
        break;
      case 2:
        searchContentType.value = "请输入要搜索的文件的md5";
        searchContentType.value = "text";
        break;
    }
    searchContent.value = "";
  }
  const handleShowDetail = (e: FileObject) => {
    console.log(e);
  }
  const handleColorThemeChange = () => {
    toggleDark();
  }
  const handleChangePage = () => {
    getFiles(nowPage.value).then((response) => {
      nowShowData.value = response.data;
    }).catch((_) => {
      ElMessage.error("无法获取文件数据，请刷新重试！");
    })
  }
  onMounted(() => {
    getFileCount().then((response) => {
      totalFileCount.value = response.data;
      handleChangePage();
    }).catch((_) => {
      ElMessage.error("无法获取文件总数，请刷新重试！")
    })
  })
</script>

<template>
  <el-container>
    <el-header style="height: 1%; display: flex;flex-direction: row">
      <el-switch
        v-model="colorTheme"
        class="ml-2"
        style="--el-switch-on-color: var(--el-border-color-dark); --el-switch-off-color: var(--el-border-color-light)"
        :active-icon="Moon"
        :inactive-icon="Sunny"
        @change="handleColorThemeChange"
      />
      <div class="search-box">
        <el-input
            :type="searchContentType"
            v-model="searchContent"
            :placeholder="searchPlaceHolder">
          <template #prepend>
            <el-select v-model="searchType" placeholder="Select" style="width: 80px" @change="onSearchTypeChange">
              <el-option label="名字" :value="0" />
              <el-option label="ID" :value="1" />
              <el-option label="MD5" :value="2" />
            </el-select>
          </template>
          <template #append>
            <el-button :icon="Search" />
          </template>
        </el-input>
      </div>
    </el-header>
    <el-main style="padding-top: 0;padding-bottom: 0;">
      <div class="main-box">
        <div class="search-table-box">
          <div class="table-box">
            <el-table :data="nowShowData" style="width: 100%">
              <el-table-column fixed prop="id" label="ID" width="80" />
              <el-table-column prop="fileName" label="文件名" width="600" />
              <el-table-column prop="fileFormat" label="文件格式" width="180" />
              <el-table-column prop="fileSize" label="文件大小" width="120" />
              <el-table-column prop="md5" label="MD5" width="340" />
              <el-table-column prop="uploader" label="上传者" width="120" />
              <el-table-column prop="uploadTime" label="上传时间" width="200" />
              <el-table-column fixed="right" label="操作" width="180">
                <template #default>
                  <el-button link type="primary" size="small" @click="handleShowDetail">详细信息</el-button>
                  <el-button link type="primary" size="small">下载</el-button>
                  <el-button link type="primary" size="small" v-if="isAdmin">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
                hide-on-single-page
                :page-size="20"
                :pager-count="11"
                layout="prev, pager, next"
                :total="totalFileCount"
                v-model:current-page="nowPage"
                @currentChange="handleChangePage"
            />
          </div>
        </div>
        <div class="uploader-preview-box">
          <div class="uploader-box">
            <el-upload
                drag
                action="https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15"
                multiple>
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                把文件拖到这里或者 <em>点击这里</em> 上传
              </div>
            </el-upload>
          </div>
          <div class="preview-box">

          </div>
        </div>
      </div>
    </el-main>
  </el-container>
</template>

<style scoped>
.main-box {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: row;
}
.search-table-box {
  display: flex;
  flex-direction: column;
  width: 72%;
  height: 100%;
}
.search-box {
  width: 30%;
  height: 10%;
  margin-left: 10px;
}
.table-box {
  margin-top: 10px;
  height: 90%;
  width: 100%;
}
.uploader-preview-box {
  height: 100%;
  width: 28%;
  margin-left: 10px;
  display: flex;
  flex-direction: column;
}
.uploader-box {
  width: 100%;
  height: 30%;
}
.preview-box {
  width: 100%;
  height: 70%;
}

</style>
