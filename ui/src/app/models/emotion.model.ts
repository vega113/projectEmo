
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
  id?: string;
  emotionName?: string;
  emotionType?: string;
  description?: string;
}

export interface SubEmotion {
  subEmotionId?: string;
  subEmotionName?: string;
  parentEmotionId?: string;
}

export interface EmotionRecord {
  id?: number;
  emotionType: string;
  userId?: number;
  emotion: Emotion,
  intensity: number;
  subEmotions: SubEmotion[];
  triggers: Trigger[];
  notes: Note[];
  tags: Tag[];
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
  id?: number;
  title?: string;
  text: string;
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

export interface EmotionRecordDay {
  date: Date;
  records: EmotionRecord[];
}

export interface EmotionRecordWeek {
  week: number;
  days: EmotionRecordDay[];
}

export interface EmotionRecordMonth {
  month: Date;
  weeks: EmotionRecordWeek[];
}

export interface DayOfWeek {
  date: number;
  records: EmotionRecord[];
  averageIntensity: number;
  dayColor: string;
}

export interface Week {
  days: DayOfWeek[];
}

export interface NoteTemplate {
  id?: string;
  label: string;
  value: string;
}
