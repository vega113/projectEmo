
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
  colors: { [key: string]: string };
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
  subEmotionId?: string;
  triggers: Trigger[];
  triggerId?: number;
  notes: Note[];
  tags: Tag[];
  isAi?: boolean;
  isDeleted?: boolean;
  created?: string;
}

// export interface UserTodoPaginatedResp {
//   todos: UserTodo[];
//   length: number;
//   hasMore: boolean;
// }
//
// export interface UserTodoPaginatedReq {
//   lastId?: number;
//   pageSize: number;
// }

export interface UserTodo {
  id?: number;
  title: string;
  description?: string;
  color?: string;
  isDone: boolean;
  isArchived: boolean;
  isDeleted: boolean;
  isRead: boolean;
  isAi: boolean;
  created?: string;
}

export interface NoteTodo {
  id?: number;
  title: string;
  description: string;
  isAccepted: boolean;
  isAi: boolean;
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
  todos?: NoteTodo[];
  created?: string;
  emotionRecordId?: number;
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
  todos: NoteTodo[];
  textTitle?: string;
  description: string;
  suggestion: string;
  status?: number;
}

export interface EmotionFromNoteResult {
  emotionDetection?: EmotionDetectionResult;
  emotionRecord?: EmotionRecord;
  note: Note;
  status?: number;
}

export interface NoteTodoUpdate {
  id: number;
  isAccepted: boolean;
}

export interface TranscribedText {
  text: string;
}
