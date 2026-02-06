import { reactive } from 'vue';
import { defineStore } from 'pinia';
import { SetupStoreId } from '@/enum';

interface NoticeItem {
  title?: string;
  read: boolean;
  message: any;
  time: string;
}

export const useNoticeStore = defineStore(SetupStoreId.Notice, () => {
  const state = reactive({
    notices: [] as NoticeItem[]
  });

  const addNotice = (notice: NoticeItem) => {
    state.notices.push(notice);
  };

  const removeNotice = (notice: NoticeItem) => {
    const index = state.notices.findIndex(n => n.time === notice.time && n.message === notice.message);
    if (index !== -1) {
      state.notices.splice(index, 1);
    }
  };

  const readNotice = (notice: NoticeItem) => {
    const index = state.notices.findIndex(n => n.time === notice.time && n.message === notice.message);
    if (index !== -1) {
      state.notices[index].read = true;
    }
  };

  // 实现全部已读
  const readAll = () => {
    state.notices.forEach((item: any) => {
      item.read = true;
    });
  };

  const clearNotice = () => {
    state.notices = [];
  };

  return {
    state,
    addNotice,
    removeNotice,
    readNotice,
    readAll,
    clearNotice
  };
});

export default useNoticeStore;
