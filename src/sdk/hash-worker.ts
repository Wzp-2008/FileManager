import SparkMD5 from "spark-md5";

type WorkerTask = {
  id: number;
  blob: Blob;
};

type WorkerResult = {
  id: number;
  hash: string;
};

onmessage = async (event: MessageEvent<WorkerTask>) => {
  const { id, blob } = event.data;

  try {
    const buffer = await blob.arrayBuffer();
    const spark = new SparkMD5.ArrayBuffer();
    spark.append(buffer);
    const hash = spark.end();

    const result: WorkerResult = { id, hash };
    self.postMessage(result);
  } catch (error) {
    self.postMessage({
      id,
      error: error instanceof Error ? error.message : String(error),
    });
  }
};
