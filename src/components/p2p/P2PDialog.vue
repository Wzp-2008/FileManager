<script setup lang="ts">
import { inject, onBeforeMount, type Ref, ref } from "vue";
import FileManagerSdk from "../../sdk";
import { P2PSingingChannel, transferData } from "../../sdk/p2p.ts";
import type { ChannelEvent } from "../../sdk/network";
import type { ChannelInitCommand } from "../../sdk/entities";
import { ElMessage } from "element-plus";
import { humanitySize } from "../../sdk/utils.ts";

const show: Ref<boolean> = defineModel({ required: true });
const sdk = inject<FileManagerSdk>("sdk") as FileManagerSdk;
const createdChannelId = ref<string>();
const createRTCConnection = () => {
  return new RTCPeerConnection({
    iceServers: [
      // 国内公共STUN
      {
        urls: [
          "stun:stun.miwifi.com:3478",
          "stun:stun.qq.com:3478",
          "stun:stun.chat.bilibili.com:3478",
        ],
      },
      // Google STUN 备用
      {
        urls: ["stun:stun.l.google.com:19302", "stun:stun1.l.google.com:19302"],
      },
      // 自建 美国 COTURN
      {
        urls: ["stuns:ama.wzpmc.email:5349", "stun:ama.wzpmc.email:3478"],
      },
      {
        urls: [
          // TLS
          "turns:ama.wzpmc.email:5349?transport=tcp",
          "turns:ama.wzpmc.email:5349?transport=udp",
          // No TLS
          "turn:ama.wzpmc.email:3478?transport=tcp",
          "turn:ama.wzpmc.email:3478?transport=udp",
        ],
        username: "testuser",
        credential: "testpass",
      },
    ],
  });
};
const progress = ref<string[]>([]);
const onFileSubmit = async (file: File) => {
  const channelResp = await sdk.createChannel(file.name, file.size);
  const data = channelResp.data;
  const channelId = data.channelId;
  const senderKey = data.senderKey;
  createdChannelId.value = channelId;
  const singingChannel = new P2PSingingChannel(channelId);
  await singingChannel.auth(senderKey);
  singingChannel.on(
    "user_add",
    async ({ data: userId }: ChannelEvent<string>) => {
      const connection = createRTCConnection();
      const rtcDataChannel = connection.createDataChannel("test");
      rtcDataChannel.onopen = () => {
        console.log("data channel open");
        ElMessage.info("Connection Made");
        const newId = progress.value.length;
        transferData(file, rtcDataChannel, (p) => {
          progress.value[newId] = (p * 100).toFixed(1);
        }).then(() => {
          ElMessage.success("Transfer done...");
          progress.value.splice(newId, 1);
        });
      };
      ElMessage.info("create data channel");
      singingChannel.onUser(
        "candidate",
        userId,
        async (evt: ChannelEvent<RTCIceCandidateInit>) => {
          if (evt.data) {
            await connection.addIceCandidate(evt.data);
          }
        },
      );
      singingChannel.onUser(
        "answer",
        userId,
        async (evt: ChannelEvent<RTCSessionDescriptionInit>) => {
          await connection.setRemoteDescription(
            new RTCSessionDescription(evt.data),
          );
        },
      );
      ElMessage.info(`user ${userId} connected, start rtc connection`);
      connection.onicecandidate = (ev) => {
        singingChannel.sendTo(userId, "candidate", ev.candidate);
      };
      const localDescription = await connection.createOffer();
      await connection.setLocalDescription(localDescription);
      singingChannel.sendTo(userId, "offer", localDescription);
    },
  );
  singingChannel.onClose(() => {
    ElMessage.error("连接断开！");
  });
};
const onInputChange = (event: Event) => {
  onFileSubmit((event.target as HTMLInputElement).files![0]);
};
const tunId = ref<string | null>();
const fileReceive = ref<{ filename: string; size: number }>();
const connectionMade = ref<boolean>(false);
const receivedSize = ref<number>(-1);
onBeforeMount(() => {
  tunId.value = new URLSearchParams(location.search).get("tun");
  if (tunId.value) {
    const channel = new P2PSingingChannel(tunId.value);
    channel.onClose(() => {
      location.search = "";
    });
    channel.on("init", (evt: ChannelEvent<ChannelInitCommand>) => {
      const { sender, filename, size } = evt.data;
      fileReceive.value = { filename, size };
      const connection = createRTCConnection();
      connection.onicecandidate = (ev) => {
        channel.sendTo(sender, "candidate", ev.candidate);
      };
      channel.onUser(
        "offer",
        sender,
        async (evt: ChannelEvent<RTCSessionDescriptionInit>) => {
          await connection.setRemoteDescription(
            new RTCSessionDescription(evt.data),
          );
          const localDescription = await connection.createAnswer();
          await connection.setLocalDescription(localDescription);
          channel.sendTo(sender, "answer", localDescription);
        },
      );
      channel.onUser(
        "candidate",
        sender,
        async (evt: ChannelEvent<RTCIceCandidateInit>) => {
          if (evt.data) {
            await connection
              .addIceCandidate(evt.data)
              .catch((e) => console.error(e));
          }
        },
      );
      connection.ondatachannel = (ev) => {
        connectionMade.value = true;
        receivedSize.value = 0;
        ev.channel.binaryType = "blob";
        ev.channel.onmessage = (ev) => {
          const d = ev.data as Blob;
          receivedSize.value += d.size;
        };
      };
    });
  }
});
</script>

<template>
  <el-dialog
    v-model="show"
    append-to-body
    :title="tunId ? `P2P文件接收 - ${tunId}` : 'P2P文件发送'">
    <div v-if="!tunId">
      <input type="file" @change="onInputChange" v-if="!tunId" />
      <div>{{ createdChannelId }}</div>
      <el-progress
        v-for="prog in progress"
        :text-inside="true"
        :stroke-width="24"
        :percentage="prog"
        status="warning" />
    </div>
    <div v-else>
      <el-text v-if="connectionMade" type="success">连接已建立</el-text>
      <el-text v-else type="warning">建立连接中...</el-text>
      <div v-if="fileReceive">
        文件名：{{ fileReceive.filename }}<br />
        文件大小：{{ humanitySize(fileReceive.size) }}
        <el-progress
          :text-inside="true"
          :stroke-width="24"
          :percentage="((receivedSize / fileReceive.size) * 100).toFixed(1)"
          status="success" />
      </div>
    </div>
  </el-dialog>
</template>

<style scoped></style>
