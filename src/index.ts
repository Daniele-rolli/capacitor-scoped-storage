import { registerPlugin } from '@capacitor/core';
import type { ScopedStoragePlugin } from './definitions';

export const ScopedStorage = registerPlugin<ScopedStoragePlugin>('ScopedStorage');

export * from './definitions';
