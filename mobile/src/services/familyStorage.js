import * as SecureStore from 'expo-secure-store';
import { ACTIVE_FAMILY_ID_KEY } from '../constants/family';

export async function saveActiveFamilyId(familyId) {
  if (familyId) {
    await SecureStore.setItemAsync(ACTIVE_FAMILY_ID_KEY, familyId);
    return;
  }
  await SecureStore.deleteItemAsync(ACTIVE_FAMILY_ID_KEY);
}

export async function getActiveFamilyId() {
  return SecureStore.getItemAsync(ACTIVE_FAMILY_ID_KEY);
}

export async function clearActiveFamilyId() {
  await SecureStore.deleteItemAsync(ACTIVE_FAMILY_ID_KEY);
}
