import { create } from 'zustand'

interface ProfileState {
  profile: any | null
  isDirty: boolean
  setProfile: (profile: any) => void
  updateField: (field: string, value: any) => void
  markClean: () => void
}

export const useProfileStore = create<ProfileState>((set) => ({
  profile: null,
  isDirty: false,

  setProfile: (profile) => set({ profile, isDirty: false }),

  updateField: (field, value) =>
    set((state) => ({
      profile: { ...state.profile, [field]: value },
      isDirty: true,
    })),

  markClean: () => set({ isDirty: false }),
}))
