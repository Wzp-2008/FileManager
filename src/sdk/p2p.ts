import type { ChannelEvent, Result } from "./network";
import { ElMessage } from "element-plus";
import { onBeforeUnmount } from "vue";

type P2PMessageReceiver = (
  message: Result<any> | ChannelEvent<any>,
) => true | void;
type P2PEventHandler = (event: ChannelEvent<any>) => void | Promise<void>;
export class P2PSingingChannel {
  socket: WebSocket;
  receivers: P2PMessageReceiver[] = [];
  eventHandlers: Record<string, P2PEventHandler[]> = {};
  constructor(id: string) {
    this.onMessage((msg) => {
      if ("cmd" in msg) return;
      if (msg.status != 200) {
        this.socket.dispatchEvent(
          new ErrorEvent("ServerReturnError", { error: msg, message: msg.msg }),
        );
        ElMessage.error(`P2P错误：${msg.msg}`);
        sessionStorage.setItem("p2p-last-error", msg.msg);
        return true;
      }
    });
    this.socket = new WebSocket(`/api/channel?id=${id}`);
    this.socket.addEventListener("message", (e) => {
      const value = JSON.parse(e.data) as
        | Result<unknown>
        | ChannelEvent<unknown>;
      for (let receiver of this.receivers) {
        if (receiver(value)) {
          break;
        }
      }
      if ("cmd" in value) {
        const handlers = this.eventHandlers[value.cmd];
        if (handlers) {
          for (let receiver of handlers) {
            receiver(value);
          }
        }
      }
    });
    onBeforeUnmount(() => {
      this.socket.close();
    });
  }
  private onMessage(receiver: P2PMessageReceiver) {
    this.receivers.push(receiver);
  }
  on(cmd: string, handler: P2PEventHandler) {
    const cur = this.eventHandlers[cmd] || [];
    cur.push(handler);
    this.eventHandlers[cmd] = cur;
  }
  onUser(cmd: string, userId: string, handler: P2PEventHandler) {
    this.on(cmd, (evt) => {
      if (evt.from == userId) {
        handler(evt);
      }
    });
  }
  onClose(receiver: () => void) {
    this.socket.addEventListener("close", receiver);
  }
  sendTo(target: string, cmd: string, data: any) {
    this.socket.send(
      JSON.stringify({ type: "to", to: target, data: { cmd, data } }),
    );
  }

  async auth(key: string) {
    await this.wait();
    this.socket.send(JSON.stringify({ type: "auth", key }));
    return new Promise((res, rej) => {
      this.socket.addEventListener(
        "message",
        (e) => {
          if (JSON.parse(e.data).status == 200) {
            res(null);
            return;
          }
          rej();
        },
        { once: true },
      );
    });
  }

  wait() {
    if (!this.socket.CONNECTING) {
      return new Promise((resolve, reject) => {
        this.socket.addEventListener("open", resolve, { once: true });
        this.socket.addEventListener("close", reject, { once: true });
        this.socket.addEventListener("error", reject, { once: true });
        this.socket.addEventListener("message", (e) => {
          console.log(e);
        });
      });
    }
    return Promise.resolve();
  }
}
const PIECE_SIZE = 256 * 1024; // 256K
const CHANNEL_BUFFERED_THRESHOLD = 8 * 1024 * 1024; // 8M;
export const transferData = async (
  blob: Blob,
  channel: RTCDataChannel,
  onProgress: (pro: number) => void,
) => {
  const count = blob.size / PIECE_SIZE + 1;
  for (let i = 0; i < count; i++) {
    channel.send(blob.slice(i * PIECE_SIZE, (i + 1) * PIECE_SIZE));
    onProgress(i / (count - 1));
    if (channel.bufferedAmount >= CHANNEL_BUFFERED_THRESHOLD) {
      await new Promise((res) => {
        channel.addEventListener(
          "bufferedamountlow",
          () => {
            res(0);
          },
          { once: true },
        );
      });
    }
  }
};
