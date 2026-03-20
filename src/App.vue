<script setup lang="ts">
import { Folder, Upload } from "@element-plus/icons-vue";
import { noop, StorageSerializers, useLocalStorage } from "@vueuse/core";
import type { DropdownInstance } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import { isEqual } from "lodash-es";
import {
  computed,
  onBeforeMount,
  provide,
  ref,
  useTemplateRef,
  watch,
} from "vue";
import InviteCodeDialog from "./components/InviteCodeDialog.vue";
import LoginRegisterDialog from "./components/LoginRegisterDialog.vue";
import type { DialogStatus } from "./components/LoginRegisterDialogTypes";
import RawFileComponent from "./components/RawFileComponent.vue";
import UploadDialog from "./components/upload/UploadDialog.vue";
import FileManagerSdk, { ROOT } from "./sdk";
import type { RawFile, User } from "./sdk/entities";
import { getFingerprint } from "./sdk/utils";
import { type SortDefinition, useMobileMediaQuery } from "./utils";
import UserSettingsDialog from "./components/UserSettingsDialog.vue";

const sdk = new FileManagerSdk();
provide("sdk", sdk);
const userInformation = ref<User | null>(null);
provide("userInfo", userInformation);
const isLogin = computed<boolean>(() => !!userInformation.value);
const isMobile = useMobileMediaQuery();
const currentFile = ref<RawFile>(ROOT);
const currentSort = useLocalStorage<SortDefinition | null>("sort", null, {
  serializer: StorageSerializers.object,
});
const showInviteCodeDialog = ref<boolean>(false);
const showUserSettingsDialog = ref<boolean>(false);
watch(
  currentSort,
  async (v, ov) => {
    if (isEqual(v, ov)) return;
    if (!userInformation.value) return;
    await sdk.updateUserPrefs(
      v
        ? {
            sortField: v.sort,
            sortReverse: v.reverse,
          }
        : {},
    );
  },
  { deep: true },
);
onBeforeMount(async () => {
  const userResponse = await sdk.getUserInformation().catch(noop);
  if (userResponse) {
    userInformation.value = userResponse.data;
    const { prefs } = userResponse.data;
    currentSort.value = prefs
      ? {
          sort: prefs.sortField,
          reverse: prefs.sortReverse,
        }
      : null;
  }
  if (userInformation.value) return;
  getFingerprint()
    .then((fingerprint) => sdk.tryLoginWithFingerprint(fingerprint))
    .then((data) => {
      if (!data) {
        return;
      }
      ElMessage.success("你已成功通过指纹登录！");
      userInformation.value = data.data;
    });
});
const dropDownMenu = ref<DropdownInstance>();
const dialogStatus = ref<DialogStatus>("HIDDEN");
const isShowUpload = ref<boolean>(false);
const onUserTextClick = () => {
  if (isLogin.value) return;
  dialogStatus.value = "LOGIN";
  if (isMobile.value) {
    dropDownMenu.value!.handleClose();
  }
};
const onLogin = (user: User) => {
  userInformation.value = user;
};
const logout = async () => {
  userInformation.value = null;
  sdk.logout(await getFingerprint());
  ElMessage.success("已退出登录！");
};
const fileRef = useTemplateRef("file");
const refreshFiles = () => {
  fileRef.value?.refresh();
};
const onFileUploaded = () => {
  refreshFiles();
};
const mkdir = () => {
  ElMessageBox.prompt("文件夹名称：", "创建文件夹", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
  })
    .then(({ value }) => {
      if (!value) {
        throw "文件夹名称不可为空！";
      }
      return sdk.mkdir(value, currentFile.value.id);
    })
    .then(
      () => {
        ElMessage.success("创建成功！");
        refreshFiles();
      },
      (e) => {
        if (e === "cancel") return;
        ElMessage.error(e);
      },
    );
};
const openSetting = () => {
  showUserSettingsDialog.value = true;
};
</script>

<template>
  <LoginRegisterDialog v-model="dialogStatus" @login="onLogin" />
  <InviteCodeDialog v-model="showInviteCodeDialog" />
  <UserSettingsDialog v-if="isLogin" v-model="showUserSettingsDialog" />
  <el-container>
    <el-header class="head">
      <h1 class="main-title">
        <span class="english-name">FileManager·</span>文件分享站
      </h1>
      <div class="head-right">
        <div
          v-if="currentFile.type === 'FOLDER' && isLogin && !isMobile"
          class="header-actions">
          <el-button type="primary" @click="isShowUpload = true">
            上传列表
          </el-button>
          <el-button type="success" @click="mkdir">新建文件夹</el-button>
        </div>
        <el-dropdown ref="dropDownMenu" hide-on-click>
          <div class="dropdown-text" @click="onUserTextClick">
            <el-avatar src="/UserAvatar.jpg" alt="Logo" />
            <span v-if="isLogin">{{ userInformation!.name }}</span>
            <span v-else>请登录</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <div v-if="isLogin">
                <el-dropdown-item @click="openSetting">设置</el-dropdown-item>
                <el-dropdown-item
                  divided
                  v-if="userInformation?.auth === 'admin'"
                  @click="showInviteCodeDialog = true">
                  获取邀请码
                </el-dropdown-item>
                <el-dropdown-item divided style="color: red" @click="logout"
                  >退出登录
                </el-dropdown-item>
              </div>
              <div v-else>
                <el-dropdown-item @click="dialogStatus = 'LOGIN'"
                  >登录
                </el-dropdown-item>
                <el-dropdown-item @click="dialogStatus = 'REGISTER'" divided
                  >注册
                </el-dropdown-item>
              </div>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main-container">
      <UploadDialog
        v-if="isLogin"
        v-model="isShowUpload"
        :current-folder-id="currentFile.id"
        @uploadedFile="onFileUploaded" />
      <template v-if="currentFile.type === 'FOLDER'">
        <teleport to="body" v-if="isMobile">
          <div class="mobile-actions">
            <template v-if="isLogin">
              <el-button
                type="primary"
                circle
                size="large"
                @click="isShowUpload = true">
                <el-icon size="large">
                  <Upload />
                </el-icon>
              </el-button>
              <el-button type="success" circle size="large" @click="mkdir">
                <el-icon size="large">
                  <Folder />
                </el-icon>
              </el-button>
            </template>
          </div>
        </teleport>
      </template>
      <RawFileComponent ref="file" v-model="currentFile" />
    </el-main>
  </el-container>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.head-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dropdown-text {
  outline: none;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
}

.dropdown-text > span {
  margin-left: 10px;
  font-size: 1.2rem;
}

.main-container {
  transition:
    padding-left,
    padding-right 500ms;
}

@media screen and (max-width: 720px) {
  .english-name {
    display: none;
  }

  .main-title {
    white-space: nowrap;
  }
}

@media screen and (max-width: 576px) {
  .main-container {
    padding: 0 !important;
  }

  .head {
    gap: 12px;
  }

  .head-right {
    gap: 8px;
  }
}

.main-title {
  transition: all 0.5s;
  font-size: 1.5rem;
}

.mobile-actions {
  position: fixed;
  bottom: 30px;
  right: 0;
  padding: 20px;
  display: flex;
  justify-content: end;
  align-items: center;
}
</style>
