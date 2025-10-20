import { AxiosPromise } from 'axios';
import server from '../../../../../services/server';

const FILE_URI = 'v1/files';

export function upload(srcId: string, fileType: string, file: Blob): AxiosPromise {
  const formData = new FormData();
  formData.append('file', file);
  return server.post(`/${FILE_URI}/upload?srcId=${srcId}&fileType=${fileType}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function batchUpload(srcId: string, fileType: string, files: File[], publicToAll: boolean = false): AxiosPromise {
  const formData = new FormData();
  files.forEach((f: File) => {
    formData.append('files', f, f.name);
  });
  return server.post(`/${FILE_URI}/batch-upload?srcId=${srcId}&fileType=${fileType}&publicToAll=${publicToAll}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function listBySource(srcId: string, fileType: string, page = 1, size = 50): AxiosPromise {
  return server.get(`/${FILE_URI}/by-source?srcId=${srcId}&fileType=${fileType}&page=${page}&size=${size}`);
}

export function deleteById(id: number | string): AxiosPromise {
  return server.delete(`/${FILE_URI}/${id}`);
}
