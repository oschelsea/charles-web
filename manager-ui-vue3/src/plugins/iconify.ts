import { addAPIProvider, addCollection } from '@iconify/vue';
import ic from '@iconify/json/json/ic.json';
import lineMd from '@iconify/json/json/line-md.json';
import heroicons from '@iconify/json/json/heroicons.json';
import materialSymbols from '@iconify/json/json/material-symbols.json';
import majesticons from '@iconify/json/json/majesticons.json';
import mdi from '@iconify/json/json/mdi.json';

/** Setup the iconify offline */
export function setupIconifyOffline() {
  const { VITE_ICONIFY_URL } = import.meta.env;

  if (VITE_ICONIFY_URL) {
    addAPIProvider('', { resources: [VITE_ICONIFY_URL] });
  }

  addCollection(ic as any);
  addCollection(lineMd as any);
  addCollection(heroicons as any);
  addCollection(materialSymbols as any);
  addCollection(majesticons as any);
  addCollection(mdi as any);
}
