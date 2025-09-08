import i18n, { use } from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import Backend from 'i18next-http-backend';
import { initReactI18next } from 'react-i18next';

use(LanguageDetector)
  .use(Backend)
  .use(initReactI18next)
  .init({
    backend: {
      loadPath: `${import.meta.env.BASE_URL?.replace(/\/$/, '')}/locales/{{lng}}/{{ns}}.json`,
    },
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false,
    },
    detection: {
      caches: ['localStorage', 'sessionStorage', 'cookie'],
    },
  });

export default i18n;

export const LANGUAGE = {
  EN: 'en',
  TC: 'tc',
  ZH: 'zh',
};
