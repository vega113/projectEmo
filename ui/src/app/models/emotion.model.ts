
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
  subEmotions: SubEmotionWrapper[];
}

export interface SubEmotionWrapper {
  subEmotion: SubEmotion;
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
  description?: string;
  suggestion?: string;
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

export interface LineChartData  {
  recordsCount: number;
  intensitySum: number;
}

export interface LineChartTrendDataRow {
  date: Date;
  emotionTypeAccumulated: { [key: string]: LineChartData };
  triggersAccumulated: { [key: string]: LineChartData };
}

export interface LineChartTrendDataSet {
  rows: LineChartTrendDataRow[];
  emotionTypes: string[];
  triggerTypes: string[];
  colors: { [key: string]: string };
}


export interface DayOfWeek {
  date: number;
  dateTime?: Date;
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

export interface DateRange {
  startDate: Date;
  endDate: Date;
}

export interface SunburstData {
  name: string;
  value?: number;
  children: SunburstData[];
  color?: string;
}

export interface DoughnutData {
  name: string;
  recordsCount: number;
  intensitySum: number;
  color?: string;
}

export interface EmotionTypesTriggersDoughnutData {
  emotionTypes: DoughnutData[];
  triggers: DoughnutData[];
}

export interface EmotionDetectionResult {
  emotionType: string;
  intensity: number;
  mainEmotionId?: string;
  subEmotionId?: string;
  triggers: Trigger[];
  tags: Tag[];
  description: string;
  suggestion: string;
}

export interface EmotionFromNoteResult {
  emotionDetection?: EmotionDetectionResult;
  note: Note;
}
