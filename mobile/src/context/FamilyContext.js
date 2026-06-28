import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { createFamily, listFamilies } from '../services/api';
import { getActiveFamilyId, saveActiveFamilyId } from '../services/familyStorage';

const FamilyContext = createContext(null);

export function FamilyProvider({ children, enabled, userId }) {
  const [families, setFamilies] = useState([]);
  const [activeFamily, setActiveFamily] = useState(null);
  const [isLoading, setIsLoading] = useState(Boolean(enabled));
  const [hasLoadError, setHasLoadError] = useState(false);
  const [error, setError] = useState('');

  const selectFamily = useCallback(async (family) => {
    setActiveFamily(family);
    await saveActiveFamilyId(family?.id ?? null);
  }, []);

  const refreshFamilies = useCallback(async () => {
    if (!enabled || !userId) {
      setFamilies([]);
      setActiveFamily(null);
      setIsLoading(false);
      setHasLoadError(false);
      return;
    }

    setIsLoading(true);
    setError('');
    setHasLoadError(false);

    try {
      const nextFamilies = await listFamilies();
      setFamilies(nextFamilies);

      const storedFamilyId = await getActiveFamilyId();
      const storedFamily = nextFamilies.find((family) => family.id === storedFamilyId);

      if (storedFamilyId && !storedFamily) {
        await saveActiveFamilyId(null);
      }

      const fallbackFamily = storedFamily ?? nextFamilies[0] ?? null;
      setActiveFamily(fallbackFamily);

      if (fallbackFamily && fallbackFamily.id !== storedFamilyId) {
        await saveActiveFamilyId(fallbackFamily.id);
      }
    } catch (loadError) {
      setHasLoadError(true);
      setError(loadError.message ?? '가족 정보를 불러오지 못했습니다.');
      setFamilies([]);
      setActiveFamily(null);
    } finally {
      setIsLoading(false);
    }
  }, [enabled, userId]);

  useEffect(() => {
    refreshFamilies();
  }, [refreshFamilies]);

  const bootstrapFamily = useCallback(
    async (name) => {
      const family = await createFamily(name);
      setFamilies((current) => [...current, family]);
      await selectFamily(family);
      setHasLoadError(false);
      return family;
    },
    [selectFamily],
  );

  const value = useMemo(
    () => ({
      families,
      activeFamily,
      isLoading,
      error,
      hasLoadError,
      needsSetup: !isLoading && !hasLoadError && families.length === 0,
      refreshFamilies,
      bootstrapFamily,
      selectFamily,
      setError,
    }),
    [
      families,
      activeFamily,
      isLoading,
      error,
      hasLoadError,
      refreshFamilies,
      bootstrapFamily,
      selectFamily,
    ],
  );

  return <FamilyContext.Provider value={value}>{children}</FamilyContext.Provider>;
}

export function useFamily() {
  const context = useContext(FamilyContext);
  if (!context) {
    throw new Error('useFamily must be used within FamilyProvider');
  }
  return context;
}
