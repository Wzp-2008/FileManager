<script setup lang="ts">
  import {onMounted, ref} from "vue";
  import {Search, UploadFilled, Moon, Sunny, UserFilled} from "@element-plus/icons-vue"
  import {FileObject} from "./entities/FileObject";
  import {FileDetailsInfo, genDetails} from './entities/FileDetailsInfo'
  import {Awaitable, useDark, useToggle} from "@vueuse/core";
  import {
    baseUrl,
    deleteFile,
    getFileCount, getFileDetails,
    getFiles,
    login,
    register,
    searchFiles,
    upload
  } from "./requester/Requester";
  import {ElMessage, UploadFile, UploadFiles, UploadRawFile, UploadRequestOptions} from "element-plus";

  const searchContent = ref<string>("");
  const searchPlaceHolder = ref<string>("请输入要搜索的内容");
  const searchType = ref<"NAME" | "ID" | "MD5">("NAME");
  const searchContentType = ref<string>("text");
  const nowShowData = ref<Array<FileObject>>([]);
  const isAdmin = ref<boolean>(false);
  const isDark = useDark();
  const toggleDark = useToggle(isDark);
  const colorTheme = ref<boolean>(isDark.value);
  const totalFileCount = ref<number>(0);
  const nowPage = ref<number>(1);
  const isSearching = ref<boolean>(false);
  const isShowDialog = ref<boolean>(false);
  const usernameInput = ref<string>("");
  const passwordInput = ref<string>("");
  const usernameShow = ref<string>("请登录！");
  const isShowDetailsInfo = ref<boolean>(false);
  const detailsFileName = ref<string>("");
  const fileDetailsInfo = ref<FileDetailsInfo[]>([]);
  const abortControllers = new Map<string, AbortController>();
  const onSearchTypeChange = (now: string) => {
    switch (now){
      case "NAME":
        searchPlaceHolder.value = "请输入要搜索的内容的名称";
        searchContentType.value = "text";
        break;
      case "ID":
        searchPlaceHolder.value = "请输入要搜索的文件的id";
        searchContentType.value = "number";
        break;
      case "MD5":
        searchPlaceHolder.value = "请输入要搜索的文件的md5";
        searchContentType.value = "text";
        break;
      case "FORMAT":
        searchPlaceHolder.value = "请输入要搜索的文件的类型";
        searchContentType.value = "text";
        break;
    }
    if (isSearching.value){
      searchContent.value = "";
      isSearching.value = false;
      nowPage.value = 1;
      handleChangePage();
    }
  }
  const handleShowDetail = (id: number, fileName: string) => {
    getFileDetails(id).then((response) => {
      fileDetailsInfo.value = genDetails(id, response.data);
      detailsFileName.value = fileName;
      isShowDetailsInfo.value = true;
    }).catch((_) => {
      ElMessage.error("无法获取文件详细信息！")
    })

  }
  const handleDownload = (id: number) => {
    window.open(baseUrl + "/api/file?id=" + id);
  }
  const handleColorThemeChange = () => {
    toggleDark();
  }
  const handleChangePage = () => {
    if (!isSearching.value){
      getFileCount().then((response) => {
        totalFileCount.value = response.data;
        getFiles(nowPage.value).then((response) => {
          nowShowData.value = response.data;
        }).catch((_) => {
          ElMessage.error("无法获取文件数据，请刷新重试！");
        });
      }).catch((_) => {
        ElMessage.error("无法获取文件总数，请刷新重试！")
      });
    }else{
      searchFiles(searchContent.value, searchType.value, nowPage.value).then((response) => {
        totalFileCount.value = response.data.count;
        nowShowData.value = response.data.data;
      }).catch((_) => {
        ElMessage.error("无法获取文件数据，请刷新重试！");
      });
    }
  }
  const handleSearch = () => {
    nowPage.value = 1;
    isSearching.value = searchContent.value !== "";
    handleChangePage();
  }

  const getUsername = () => {
    const username = sessionStorage.getItem("username");
    if (username){
      if (username === "wzp"){
        isAdmin.value = true;
      }
      return username;
    }
    return "请登录！";
  }
  const handleLogin = () => {
    login(usernameInput.value, passwordInput.value).then((response) => {
      if (response.data.data !== null){
        ElMessage.success("登录成功！");
        window.sessionStorage.setItem("username", usernameInput.value);
        usernameShow.value = usernameInput.value;
        isShowDialog.value = false;
      }else{
        ElMessage.error("登录失败，用户名或密码错误！");
      }
    });
  }
  const handleRegister = () => {
    register(usernameInput.value, passwordInput.value).then((response) => {
      if (response.data.data !== null){
        ElMessage.success("注册成功！");
        window.sessionStorage.setItem("username", usernameInput.value);
        usernameShow.value = usernameInput.value;
        isShowDialog.value = false;
      }
    });
  }
  const needLogin = () => {
    return window.sessionStorage.getItem("token") == null;
  }
  const handleUpload = (options: UploadRequestOptions): XMLHttpRequest | Promise<unknown> => {
    const abortController = new AbortController();
    abortControllers.set(options.file.name, abortController);
    let promise = upload(options, abortController);
    promise.then((response) => {
      options.onSuccess(response);
    }).catch((error) => {
      options.onError(error)
    }).finally(() => {
      abortControllers.delete(options.filename);
    });
    return promise;
  }
  const handleUploadRemove = (uploadFile: UploadFile, _: UploadFiles): Awaitable<boolean> => {
    const name = uploadFile.raw?.name as string;
    const controller = abortControllers.get(name);
    if (controller){
      controller.abort("user");
      ElMessage.success("成功取消！")
      return true;
    }
    return false;
  }
  const handleUploadSuccess = (__: any, uploadFile: UploadFile, _: UploadFiles): void => {
    ElMessage.success("文件" + uploadFile.raw?.name + "上传成功！");
  }
  const handleBeforeUpload = (_: UploadRawFile): boolean => {
    if (needLogin()){
      ElMessage.error("未登录，无法上传！");
      return false;
    }
    return true;
  }
  const handleDelete = (id: number) => {
    deleteFile(id).then((response) => {
      if (response.data) {
        ElMessage.success("删除成功！");
      }else{
        ElMessage.error("删除失败！");
      }
    })
  }
  const handleTranscoding = (data: FileDetailsInfo) => {
    console.log(data.fid);
  }
  onMounted(() => {
    nowPage.value = 1;
    handleChangePage();
    usernameShow.value = getUsername();
  })
</script>

<template>
  <el-dialog v-if="needLogin()" v-model="isShowDialog" title="登录或注册" width="30%" draggable>
    <el-input v-model="usernameInput" placeholder="请输入用户名">
      <template #prepend>用户名</template>
    </el-input>
    <el-input style="margin-top: 10px" v-model="passwordInput" placeholder="请输入密码" show-password>
      <template #prepend>密码</template>
    </el-input>
    <template #footer>
      <el-button @click="isShowDialog = false;">取消</el-button>
      <el-button type="warning" @click="handleLogin">登录</el-button>
      <el-button type="danger" @click="handleRegister">注册</el-button>
    </template>
  </el-dialog>
  <el-dialog
      v-model="isShowDetailsInfo"
      :title="detailsFileName"
      width="30%"
      align-center
  >
    <el-tree
        :data="fileDetailsInfo"
        node-key="id"
        :expand-on-click-node="false"
    >
      <template #default="{ node, data }">
        <span style="width: 90%">{{ node.label }}</span>
        <el-button style="width: 10%; height: 100%;" type="primary" text @click="handleTranscoding(data)" v-if="node.label.startsWith('编码格式: ')">转码</el-button>
      </template>
    </el-tree>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="isShowDetailsInfo = false">退出</el-button>
      </span>
    </template>
  </el-dialog>
  <el-container>
    <el-header style="height: 1%; display: flex;flex-direction: row">
      <div class="header left">
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
              <el-select v-model="searchType" placeholder="Select" style="width: 100px" @change="onSearchTypeChange">
                <el-option label="名字" value="NAME" />
                <el-option label="ID" value="ID" />
                <el-option label="MD5" value="MD5" />
                <el-option label="文件类型" value="FORMAT" />
              </el-select>
            </template>
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>
      </div>
      <div class="header right" @click="isShowDialog = true;">
        <el-avatar :icon="UserFilled" />
        <div style="text-align: center; height: 100%; margin-left: 10px; margin-top: auto; margin-bottom: auto;" v-text="usernameShow"></div>
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
              <el-table-column prop="uploader" label="上传者" width="120" />
              <el-table-column fixed="right" label="操作" width="180">
                <template #default="scope">
                  <el-button link type="primary" size="small" @click="handleShowDetail(scope.row.id, scope.row.fileName);">详细信息</el-button>
                  <el-button link type="primary" size="small" @click="handleDownload(scope.row.id);">下载</el-button>
                  <el-button link type="primary" size="small" @click="handleDelete(scope.row.id);" v-if="isAdmin">删除</el-button>
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
                :http-request="handleUpload"
                :before-remove="handleUploadRemove"
                :on-success="handleUploadSuccess"
                :before-upload="handleBeforeUpload"
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
  margin-top: 10px;
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
.header {
  height: 100%;
  display: flex;
}
.left {
  width: 70%;
  justify-content: start;
}
.right {
  width: 30%;
  justify-content: end;
}
.right:hover {
  cursor: pointer;
}

</style>
