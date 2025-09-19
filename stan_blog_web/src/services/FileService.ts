import server from './server';

export interface FileResourceDTO {
  id: number;
  originalFilename: string;
  storedFilename: string;
  sizeInBytes: number;
  contentType: string;
  ownerId: number;
  publicToAll: boolean;
  downloadUrl: string;
  createTime: string;
}

export async function uploadPublicImage(file: File, publicToAll = true): Promise<FileResourceDTO> {
  const formData = new FormData();
  formData.append('file', file);
  const resp = await server.post<FileResourceDTO>(`/v1/files/upload`, formData, {
    params: { publicToAll },
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return resp.data;
}

export function buildFileViewUrl(id: number): string {
  const base = server.defaults.baseURL?.toString().replace(/\/$/, '') ?? '';
  return `${base}/v1/files/${id}/view`;
}
