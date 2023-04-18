export interface FileDetailsInfo {
    id: number;
    fid: number;
    label: string;
    children: FileDetailsInfo[]
}
export const genDetails = (fid: number, data: any, id: number = 0): FileDetailsInfo[] => {
    const result: FileDetailsInfo[] = [];
    for (let key in data){
        if (typeof data[key] === "string"){
            const show = `${key}: ${data[key]}`;
            result.push({id: id, label: show, children: [], fid: fid});
        }else{
            const init = genDetails(fid, data[key], id + 1);
            result.push({id: id, label: key, children: init, fid: fid});
            id += init.length;
        }
        id ++;
    }
    return result;
}