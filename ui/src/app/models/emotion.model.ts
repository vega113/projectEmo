
export interface User {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}
export interface EmotionData {
  emotionTypes: EmotionTypesWithEmotions[];
  triggers: Trigger[];
}

export interface EmotionTypesWithEmotions {
  emotionType: string;
  emotions: EmotionWithSubEmotions[];
}

export interface EmotionWithSubEmotions {
  emotion: Emotion;
  subEmotions: SubEmotionWithActions[];
}

export interface SubEmotionWithActions {
  subEmotion: SubEmotion;
  suggestedActions: SuggestedAction[];
}

export interface User {
  userId?: number;
  username: string;
  password: string;
  firstName?: string;
  lastName?: string;
  email: string;
  isPasswordHashed?: boolean;
  created?: string;
}

export interface Emotion {
  id: string;
  emotionName: string;
  emotionType: string;
}

export interface SubEmotion {
  subEmotionId?: string;
  subEmotionName?: string;
  parentEmotionId?: string;
}

export interface EmotionRecord {
  id?: number;
  userId: number;
  emotionId: string;
  intensity: number;
  subEmotions: SubEmotion[];
  triggers: Trigger[];
  created?: string;
}

export interface Trigger {
  triggerId?: number;
  triggerName?: string;
  parentId?: number;
  createdByUser?: number;
  description?: string;
  created?: string;
}

export interface Note {
  noteId?: number;
  title?: string;
  noteText: string;
  noteUserId: number;
  created?: string;
}

export interface Tag {
  tagId?: number;
  tagName: string;
  created?: string;
}

export interface SuggestedAction {
  id?: string;
  name: string;
  created?: string;
}
