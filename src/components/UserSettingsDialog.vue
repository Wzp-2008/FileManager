<script lang="ts" setup>
import FileManagerSdk from "../sdk";
import type { User } from "../sdk/entities";
import { inject, ref, type Ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

const dialogVisible = defineModel<boolean>();
const sdk = inject("sdk") as FileManagerSdk;
const userInformation = inject("userInfo") as Ref<User>;
const currentUsername = ref<string>(userInformation.value.name);
const currentPassword = ref<string>("********");
const isChangeUsername = ref<boolean>(false);
const completeChangeUsername = () => {
  sdk
    .changeUsername(currentUsername.value)
    .then(() => {
      userInformation.value.name = currentUsername.value;
      isChangeUsername.value = false;
      ElMessage.success("修改成功！");
    })
    .catch(() => {});
};
const isChangePassword = ref<boolean>(false);
const completeChangePassword = () => {
  if (currentPassword.value.length === 0) {
    ElMessage.error("密码不能为空！");
    return;
  }
  ElMessageBox.prompt("请输入你的旧密码：", "修改密码", {
    inputType: "password",
    inputPlaceholder: "旧密码",
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
    .then(({ value }) => {
      sdk
        .changePassword(value, currentPassword.value)
        .then(() => {
          ElMessage.success("修改密码成功！");
          isChangePassword.value = false;
          currentPassword.value = "********";
        })
        .catch((e) => {
          ElMessage.error(e);
        });
    })
    .catch(() => {});
};
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    append-to-body
    class="settings-dialog"
    title="用户设置"
    width="500">
    <div class="settings">
      <div>用户名：</div>
      <el-input
        v-model="currentUsername"
        :disabled="!isChangeUsername"
        placeholder="请输入用户名">
        <template #append>
          <el-button v-if="!isChangeUsername" @click="isChangeUsername = true"
            >修改
          </el-button>
          <el-button v-else @click="completeChangeUsername">确定</el-button>
        </template>
      </el-input>
      <div>密码：</div>
      <el-input
        v-model="currentPassword"
        :disabled="!isChangePassword"
        placeholder="请输入密码">
        <template #append>
          <el-button
            v-if="!isChangePassword"
            @click="
              currentPassword = '';
              isChangePassword = true;
            "
            >修改
          </el-button>
          <el-button v-else @click="completeChangePassword">确定</el-button>
        </template>
      </el-input>
    </div>
  </el-dialog>
</template>

<style>
.settings-dialog {
  transition: all 0.5s;
}

.settings {
  padding: 10px;
  display: grid;
  grid-template-columns: 0.2fr 1fr;
  gap: 10px;
}

.settings > div {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media screen and (max-width: 576px) {
  .settings-dialog {
    width: 100vw !important;
    height: 100vh !important;
    margin: 0 !important;
  }
}
</style>
