import { defineStore } from 'pinia';
import {
  login as userLogin,
  logout as userLogout,
  getUserInfo,
  LoginData,
} from '@/api/user';
import { setToken, clearToken } from '@/utils/auth';
import { removeRouteListener } from '@/utils/route-listener';
import { UserState } from './types';
import useAppStore from '../app';

const useUserStore = defineStore('user', {
  state: (): UserState => ({
    id: undefined,
    name: undefined,
    pin: undefined,
    pic: undefined,
    loggedin: undefined,
    lastlogin: undefined,
    createdat: undefined,
    birthday: undefined,
    banned: undefined,
    banreason: undefined,
    macs: undefined,
    nxCredit: undefined,
    maplePoint: undefined,
    nxPrepaid: undefined,
    characterslots: undefined,
    gender: undefined,
    tempban: undefined,
    greason: undefined,
    tos: undefined,
    sitelogged: undefined,
    webadmin: undefined,
    nick: undefined,
    mute: undefined,
    email: undefined,
    ip: undefined,
    rewardpoints: undefined,
    votepoints: undefined,
    hwid: undefined,
    language: undefined,
    role: '',
    avatar: undefined,
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state };
    },
  },

  actions: {
    switchRoles() {
      return new Promise((resolve) => {
        this.role = this.role === 'user' ? 'admin' : 'user';
        resolve(this.role);
      });
    },
    setInfo(partial: Partial<UserState>) {
      partial.role = partial.webadmin ? 'admin' : 'user';
      this.$patch(partial);
    },

    resetInfo() {
      this.$reset();
    },

    async info() {
      const res = await getUserInfo();
      this.setInfo(res.data);
    },

    async login(loginForm: LoginData) {
      try {
        const res = await userLogin(loginForm);
        setToken(res.data.token);
      } catch (err) {
        clearToken();
        throw err;
      }
    },
    logoutCallBack() {
      const appStore = useAppStore();
      this.resetInfo();
      clearToken();
      removeRouteListener();
      appStore.clearServerMenu();
    },
    async logout() {
      try {
        await userLogout();
      } finally {
        this.logoutCallBack();
      }
    },
  },
});

export default useUserStore;
