<script lang="ts" setup>
import type {FormInstance, FormRules} from "element-plus";
import {ElMessage} from "element-plus";
import {computed, inject, ref, watch} from "vue";
import type {User} from "../sdk/entities";
import type {UserRegisterRequest} from "../sdk/request";
import {getFingerprint} from "../sdk/utils.ts";
import type {DialogStatus} from "./LoginRegisterDialogTypes";
import type FileManagerSdk from "../sdk";

const model = defineModel<DialogStatus>();
const sdk = inject("sdk") as FileManagerSdk;
const emit = defineEmits<{
  (e: "login", user: User): void;
}>();
const isShowDialog = computed(() =>
    model.value ? model.value !== "HIDDEN" : false,
);
const isLoginOrRegister = computed(() =>
    model.value ? model.value === "LOGIN" : false,
);
const dialogVisible = ref<boolean>(false);
watch(isShowDialog, (v) => {
  dialogVisible.value = v;
});
watch(dialogVisible, (v) => {
  if (v) return;
  model.value = "HIDDEN";
});

const changeType = () => {
  if (!model.value) return;
  if (model.value === "LOGIN") {
    model.value = "REGISTER";
    return;
  }
  model.value = "LOGIN";
};
const userInformationForm = ref<UserRegisterRequest>({
  username: "",
  password: "",
  auth: "user",
  inviteCode: "",
});
const onDialogClose = () => {
  userInformationForm.value = {
    username: "",
    password: "",
    auth: "user",
    inviteCode: "",
  };
};
const saveFingerprint = async () => {
  await sdk.saveFingerprint(await getFingerprint());
};
const onLoginSuccess = (user: User) => {
  saveFingerprint();
  emit("login", user);
};
const loginOrRegister = () => {
  if (!mainForm.value) return;
  mainForm.value.validate((isValid) => {
    if (!model.value || !isValid) return;
    if (model.value === "LOGIN") {
      sdk
          .login(userInformationForm.value)
          .then((res) => {
            ElMessage.success("登录成功！");
            model.value = "HIDDEN";
            onLoginSuccess(res.data);
          })
          .catch((err) => ElMessage.error(err));
    } else if (model.value === "REGISTER") {
      sdk
          .register(userInformationForm.value)
          .then((res) => {
            ElMessage.success("注册成功！");
            model.value = "HIDDEN";
            onLoginSuccess(res.data);
          })
          .catch((err) => ElMessage.error(err));
    }
  });
};
const validateRules = computed<FormRules<UserRegisterRequest>>(() => {
  return {
    username: {
      required: true,
      message: "请输入用户名",
      trigger: "blur",
    },
    password: {
      required: true,
      message: "请输入密码",
      trigger: "blur",
    },
    auth: {
      required: !isLoginOrRegister.value,
      message: "请选择用户类型",
      trigger: "change",
    },
    inviteCode: {
      required:
          !isLoginOrRegister.value && userInformationForm.value.auth === "admin",
      len: 8,
      message: "请输入正确的管理员用户邀请码",
      trigger: "change",
    },
  };
});
const mainForm = ref<FormInstance>();
const rememberDevice = ref<boolean>(false);
</script>

<template>
  <el-dialog
      v-model="dialogVisible"
      :title="modelValue === 'LOGIN' ? '登录' : '注册'"
      append-to-body
      class="login-dialog"
      width="500"
      @close="onDialogClose"
  >
    <el-form
        ref="mainForm"
        :model="userInformationForm"
        :rules="validateRules"
        class="main-form"
        label-width="auto"
        @submit="loginOrRegister"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
            v-model="userInformationForm.username"
            @keydown.enter="loginOrRegister"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
            v-model="userInformationForm.password"
            type="password"
            @keydown.enter="loginOrRegister"
        />
      </el-form-item>
      <div v-if="!isLoginOrRegister">
        <el-form-item label="类型" prop="auth">
          <el-select v-model="userInformationForm.auth">
            <el-option label="普通用户" value="user"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
            v-if="userInformationForm.auth === 'admin'"
            label="邀请码"
            prop="inviteCode"
        >
          <el-input v-model="userInformationForm.inviteCode" type="password"/>
        </el-form-item>
      </div>
      <el-checkbox v-model="rememberDevice" label="记住设备（实验性）"/>
    </el-form>
    <template #footer>
      <div class="dialog-footer dialog-buttons">
        <el-button class="cancel-button" @click="model = 'HIDDEN'"
        >取消
        </el-button>
        <div class="switch-login-button-group">
          <el-button type="primary" @click="loginOrRegister">
            <span v-if="isLoginOrRegister">登录</span>
            <span v-else>注册</span>
          </el-button>
          <el-button
              :type="isLoginOrRegister ? 'success' : 'warning'"
              @click="changeType"
          ><span v-if="isLoginOrRegister">切换到注册</span
          ><span v-else>切换到登录</span></el-button
          >
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<style>
.dialog-buttons {
  display: flex;
  justify-content: space-between;
}

.login-dialog {
  transition: all 0.5s;
}

@media screen and (max-width: 576px) {
  .login-dialog {
    width: 100vw !important;
    height: 100vh !important;
    margin: 0 !important;
  }

  .dialog-buttons {
    flex-direction: column;
    justify-content: center;
    align-items: stretch;
  }

  .cancel-button {
    order: 2;
    margin-top: 10px;
  }

  .switch-login-button-group {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: stretch;
    order: 1;
  }

  .switch-login-button-group > button {
    margin-left: 0 !important;
    margin-top: 10px;
  }

  .main-form {
    display: flex;
    justify-content: center;
    flex-direction: column;
    max-width: none !important;
  }
}

.main-form {
  max-width: 600px;
}
</style>
