import { AxiosPromise } from 'axios';
import server from './server';

const AI_URI = 'v1/ai';

export function checkQuota(): AxiosPromise {
  return server.get(`/${AI_URI}/check-quota`);
}

export function generateTitle(content: string): AxiosPromise {
  return server.post(
    `/${AI_URI}/generate-title`,
    { content },
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );
}
