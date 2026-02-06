import { watch } from 'vue';
import { useEventSource } from '@vueuse/core';
import useNoticeStore from '@/store/modules/notice';
import { $t } from '@/locales';
import { localStg } from './storage';

/**
 * 初始化 SSE
 *
 * @param url - SSE 地址
 */
export const initSSE = (url: string) => {
  const token = localStg.get('token');
  if (import.meta.env.VITE_APP_SSE === 'N' || !token) {
    return;
  }
  const sseUrl = `${url}?Authorization=Bearer ${token}&clientid=${import.meta.env.VITE_APP_CLIENT_ID}`;
  const { data, error } = useEventSource(sseUrl, [], {
    autoReconnect: {
      retries: 5,
      delay: 5000,
      onFailed() {
        // eslint-disable-next-line no-console
        console.warn('Failed to connect to SSE after 5 attempts.');
      }
    }
  });

  let lastErrorTime = 0;
  watch(error, () => {
    if (!error.value || error.value?.isTrusted) {
      return;
    }
    // 防止短时间内重复处理相同错误
    const now = Date.now();
    if (now - lastErrorTime < 1000) {
      return;
    }
    lastErrorTime = now;
    // eslint-disable-next-line no-console
    console.error('SSE connection error:\n', error.value);
  });

  watch(data, () => {
    if (!data.value) return;
    useNoticeStore().addNotice({
      message: data.value,
      read: false,
      time: new Date().toLocaleString()
    });
    let content = data.value;
    const noticeType = content.match(/\[dict\.(.*?)\]/)?.[1];
    if (noticeType) {
      content = content.replace(`dict.${noticeType}`, $t(`dict.${noticeType}` as App.I18n.I18nKey));
    }
    window.$notification?.create({
      title: '消息',
      content,
      type: 'success',
      duration: 3000
    });
    data.value = null;
  });
};
