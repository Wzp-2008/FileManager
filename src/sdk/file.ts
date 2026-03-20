import ChunkMd5Worker from "./hash-worker.ts?worker";

export type ChunkMd5Result = {
  index: number;
  start: number;
  end: number;
  hash: string;
};

type WorkerResponse = {
  id: number;
  hash?: string;
  error?: string;
};

type Task = {
  id: number;
  index: number;
  start: number;
  end: number;
  blob: Blob;
};

function createWorker() {
  return new ChunkMd5Worker();
}

export async function computeChunkMd5sWithWorker(
  file: File | Blob,
  onProgress: (progress: number) => void = () => {},
  cancelSignal: AbortSignal | null = null,
): Promise<[ChunkMd5Result[], Blob[]]> {
  const chunkSize = 2 * 1024 * 1024;
  const concurrency = Math.max(1, (navigator.hardwareConcurrency || 4) - 1);
  const total = file.size;
  const chunkCount = Math.ceil(total / chunkSize);
  const results: ChunkMd5Result[] = new Array(chunkCount);

  const tasks: Task[] = [];
  for (let index = 0; index < chunkCount; index++) {
    const start = index * chunkSize;
    const end = Math.min(start + chunkSize, total);
    tasks.push({
      id: index,
      index,
      start,
      end,
      blob: file.slice(start, end),
    });
  }

  let nextTaskIndex = 0;

  return new Promise((resolve, reject) => {
    let stopped = false;
    const cleanup = () => {
      for (const worker of workers) {
        worker.terminate();
      }
    };
    cancelSignal?.addEventListener("abort", () => {
      stopped = true;
      cleanup();
      reject("aborted");
    });
    const workers: Worker[] = [];
    let finished = 0;

    const startNext = (worker: Worker) => {
      if (stopped) return;

      const task = tasks[nextTaskIndex++];
      if (!task) return;

      worker.postMessage({
        id: task.id,
        blob: task.blob,
      });
    };

    for (let i = 0; i < Math.min(concurrency, chunkCount); i++) {
      const worker = createWorker();
      workers.push(worker);

      worker.onmessage = (event: MessageEvent<WorkerResponse>) => {
        const { id, hash, error } = event.data;

        if (error) {
          stopped = true;
          cleanup();
          reject(new Error(error));
          return;
        }

        const task = tasks[id]!;
        results[id] = {
          index: task.index,
          start: task.start,
          end: task.end,
          hash: hash!,
        };

        finished++;

        onProgress(finished / chunkCount);

        if (finished >= chunkCount) {
          stopped = true;
          cleanup();
          resolve([results, tasks.map((e) => e.blob)]);
          return;
        }

        startNext(worker);
      };

      worker.onerror = (err) => {
        stopped = true;
        cleanup();
        reject(err);
      };

      startNext(worker);
    }
  });
}
