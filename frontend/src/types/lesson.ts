export type ActivityType = "EXPLANATION" | "EXPLORE" | "MULTIPLE_CHOICE" | "EDGE_SELECTION" | "NODE_SELECTION" | "CODE_COMPLETION";
export type ProgressStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";
export interface LessonSummary { id:string; title:string; description:string; totalActivities:number; completedActivities:number; status:ProgressStatus; }
export interface GraphNode { id:string; name?:string; x:number; y:number; }
export interface GraphEdge { id:string; source:string; target:string; weight:number; }
export interface ExploreStep { title:string; explanation:string; currentNode:string|null; visitedNodes:string[]; distances:Record<string,string>; consideredEdges:string[]; selectedEdges:string[]; }
export interface ActivityContent { graph?:{nodes:GraphNode[];edges:GraphEdge[];directed?:boolean}; steps?:{title:string;text:string}[]; exploreSteps?:ExploreStep[]; options?:{id:string;text:string}[]; maxSelections?:number; lines?:{text?:string;blank?:boolean}[]; tokens?:{id:string;text:string}[]; }
export interface ActivityView { id:number; position:number; type:ActivityType; title:string; instruction:string; content:ActivityContent|string; }
export interface LessonSession { lessonId:string; title:string; status:ProgressStatus; totalActivities:number; completedActivities:number; currentActivity:ActivityView|null; activityTitles:string[]; }
export interface Feedback { praiseMessage:string|null; clueMessage:string|null; explanation:string|null; }
export interface SubmitResponse { result:{correct:boolean;feedback:Feedback}; totalActivities:number; completedActivities:number; lessonCompleted:boolean; }
